package com.aotemiao.artemis.symphony.orchestrator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aotemiao.artemis.symphony.config.ServiceConfig;
import com.aotemiao.artemis.symphony.core.model.CodexUpdateEvent;
import com.aotemiao.artemis.symphony.core.model.Issue;
import com.aotemiao.artemis.symphony.core.model.RetryEntry;
import com.aotemiao.artemis.symphony.core.model.WorkflowDefinition;
import com.aotemiao.artemis.symphony.persistence.RunHistoryEvent;
import com.aotemiao.artemis.symphony.persistence.RunHistoryMetrics;
import com.aotemiao.artemis.symphony.persistence.RunHistoryRecord;
import com.aotemiao.artemis.symphony.persistence.RunHistoryRepository;
import com.aotemiao.artemis.symphony.tracker.LinearTrackerClient;
import com.aotemiao.artemis.symphony.tracker.TrackerResult;
import com.aotemiao.artemis.symphony.workspace.WorkspaceManager;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OrchestratorRunHistoryTest {

    @Test
    void reconcileTerminalIssue_marksRunHistoryTerminated() throws Exception {
        RecordingTrackerClient tracker = new RecordingTrackerClient(terminalIssue());
        RecordingRunHistoryRepository runHistoryRepository = new RecordingRunHistoryRepository();
        Orchestrator orchestrator = createOrchestrator(tracker, runHistoryRepository);
        Issue runningIssue = activeIssue();
        RunningEntry entry =
                new RunningEntry(runningIssue.id(), runningIssue.identifier(), "run-1", runningIssue, 0, Instant.now());
        entry.lastCodexTimestamp = Instant.now();
        putRunning(orchestrator, entry);

        reconcileRunning(orchestrator);

        assertTrue(orchestrator.getRunning().isEmpty());
        assertEquals("run-1", runHistoryRepository.finishedRunId);
        assertEquals("terminated", runHistoryRepository.finishedStatus);
        assertEquals("tracker state is terminal: Done", runHistoryRepository.finishedReason);
    }

    @Test
    void onCodexUpdate_preservesTokenTotalsWhenLaterEventHasNoUsage() throws Exception {
        Orchestrator orchestrator =
                createOrchestrator(new RecordingTrackerClient(activeIssue()), RunHistoryRepository.NOOP);
        Issue runningIssue = activeIssue();
        RunningEntry entry = new RunningEntry(
                runningIssue.id(), runningIssue.identifier(), "run-usage", runningIssue, 0, Instant.now());
        putRunning(orchestrator, entry);

        invokeOnCodexUpdate(
                orchestrator,
                runningIssue.id(),
                new CodexUpdateEvent(
                        "thread/tokenUsage/updated",
                        Instant.parse("2026-06-09T02:00:00Z"),
                        null,
                        null,
                        Map.of(
                                "usage",
                                Map.of(
                                        "input_tokens", 11L,
                                        "output_tokens", 22L,
                                        "total_tokens", 33L))));
        invokeOnCodexUpdate(
                orchestrator,
                runningIssue.id(),
                new CodexUpdateEvent(
                        "turn_failed",
                        Instant.parse("2026-06-09T02:00:01Z"),
                        null,
                        null,
                        Map.of("session_id", "thread-1 - turn-1")));

        assertEquals(11L, entry.codexInputTokens);
        assertEquals(22L, entry.codexOutputTokens);
        assertEquals(33L, entry.codexTotalTokens);
        assertEquals("turn_failed", entry.lastCodexEvent);
    }

    @Test
    void onWorkerExit_completedDoesNotRecordFailureRetryScheduled() throws Exception {
        RecordingRunHistoryRepository runHistoryRepository = new RecordingRunHistoryRepository();
        Orchestrator orchestrator = createOrchestrator(new RecordingTrackerClient(activeIssue()), runHistoryRepository);
        Issue runningIssue = activeIssue();
        RunningEntry entry = new RunningEntry(
                runningIssue.id(), runningIssue.identifier(), "run-success", runningIssue, 0, Instant.now());
        putRunning(orchestrator, entry);

        invokeOnWorkerExit(orchestrator, runningIssue.id(), true, null);

        assertEquals("completed", runHistoryRepository.finishedStatus);
        assertEquals(0, runHistoryRepository.retryScheduledCount);
    }

    @Test
    void onWorkerExit_failedRecordsFailureRetryScheduled() throws Exception {
        RecordingRunHistoryRepository runHistoryRepository = new RecordingRunHistoryRepository();
        Orchestrator orchestrator = createOrchestrator(new RecordingTrackerClient(activeIssue()), runHistoryRepository);
        Issue runningIssue = activeIssue();
        RunningEntry entry = new RunningEntry(
                runningIssue.id(), runningIssue.identifier(), "run-failed", runningIssue, 0, Instant.now());
        putRunning(orchestrator, entry);

        invokeOnWorkerExit(orchestrator, runningIssue.id(), false, "codex turn failed");

        assertEquals("failed", runHistoryRepository.finishedStatus);
        assertEquals(1, runHistoryRepository.retryScheduledCount);
        assertEquals(2, runHistoryRepository.nextAttempt);
    }

    @Test
    void runOneTick_skipsIssuesWithPendingDispatchPlan() throws Exception {
        RecordingRunHistoryRepository runHistoryRepository = new RecordingRunHistoryRepository();
        Orchestrator orchestrator = createOrchestrator(new RecordingTrackerClient(activeIssue()), runHistoryRepository);

        RetryEntry retryEntry = invokeScheduleRetry(orchestrator, activeIssue());
        invokeRunOneTick(orchestrator);

        assertTrue(orchestrator.getRunning().isEmpty());
        RetryEntry pending = orchestrator.findRetryByIdentifier("ART-1");
        assertNotNull(pending);
        assertEquals(retryEntry.issueId(), pending.issueId());
        assertEquals("continuation", pending.kind());
        assertEquals(0, runHistoryRepository.runStartedCount);
    }

    @Test
    void dispatchIssue_skipsIssuesWithPendingDispatchPlan() throws Exception {
        RecordingRunHistoryRepository runHistoryRepository = new RecordingRunHistoryRepository();
        Orchestrator orchestrator = createOrchestrator(new RecordingTrackerClient(activeIssue()), runHistoryRepository);

        RetryEntry retryEntry = invokeScheduleRetry(orchestrator, activeIssue());
        boolean dispatched = invokeDispatchIssue(orchestrator, activeIssue());

        assertFalse(dispatched);
        assertTrue(orchestrator.getRunning().isEmpty());
        assertNotNull(orchestrator.findRetryByIdentifier("ART-1"));
        assertEquals(
                retryEntry.issueId(),
                orchestrator.findRetryByIdentifier("ART-1").issueId());
        assertEquals(0, runHistoryRepository.runStartedCount);
    }

    @Test
    void onWorkerExit_completedHighRiskImplementationSchedulesAdversarialReview() throws Exception {
        RecordingRunHistoryRepository runHistoryRepository = new RecordingRunHistoryRepository();
        Orchestrator orchestrator = createOrchestrator(
                new RecordingTrackerClient(activeIssue()),
                runHistoryRepository,
                Map.of(
                        "delivery",
                        Map.of("adversarial_review", Map.of("enabled", true, "issue_title_regex", "Run history"))));
        Issue runningIssue = activeIssue();
        RunningEntry entry = new RunningEntry(
                runningIssue.id(), runningIssue.identifier(), "run-implementation", runningIssue, 0, Instant.now());
        entry.dispatchKind = "implementation";
        putRunning(orchestrator, entry);

        invokeOnWorkerExit(orchestrator, runningIssue.id(), true, null);

        RetryEntry pending = orchestrator.findRetryByIdentifier("ART-1");
        assertNotNull(pending);
        assertEquals("adversarial_review", pending.kind());
        assertEquals("run-implementation", pending.parentRunId());
    }

    @Test
    void onWorkerExit_completedAdversarialReviewDoesNotScheduleAnotherReview() throws Exception {
        RecordingRunHistoryRepository runHistoryRepository = new RecordingRunHistoryRepository();
        Orchestrator orchestrator = createOrchestrator(
                new RecordingTrackerClient(activeIssue()),
                runHistoryRepository,
                Map.of(
                        "delivery",
                        Map.of("adversarial_review", Map.of("enabled", true, "issue_title_regex", "Run history"))));
        Issue runningIssue = activeIssue();
        RunningEntry entry = new RunningEntry(
                runningIssue.id(), runningIssue.identifier(), "run-review", runningIssue, 0, Instant.now());
        entry.dispatchKind = "adversarial_review";
        entry.parentRunId = "run-implementation";
        putRunning(orchestrator, entry);

        invokeOnWorkerExit(orchestrator, runningIssue.id(), true, null);

        assertNull(orchestrator.findRetryByIdentifier("ART-1"));
    }

    @SuppressWarnings("unchecked")
    private static void putRunning(Orchestrator orchestrator, RunningEntry entry) throws Exception {
        java.lang.reflect.Field field = Orchestrator.class.getDeclaredField("running");
        field.setAccessible(true);
        Map<String, RunningEntry> running = (Map<String, RunningEntry>) field.get(orchestrator);
        running.put(entry.issueId, entry);
    }

    private static void reconcileRunning(Orchestrator orchestrator) throws Exception {
        java.lang.reflect.Method method = Orchestrator.class.getDeclaredMethod("reconcileRunningIssues");
        method.setAccessible(true);
        method.invoke(orchestrator);
    }

    private static void invokeOnCodexUpdate(Orchestrator orchestrator, String issueId, CodexUpdateEvent event)
            throws Exception {
        java.lang.reflect.Method method =
                Orchestrator.class.getDeclaredMethod("onCodexUpdate", String.class, CodexUpdateEvent.class);
        method.setAccessible(true);
        method.invoke(orchestrator, issueId, event);
    }

    private static void invokeOnWorkerExit(
            Orchestrator orchestrator, String issueId, boolean normal, String failureReason) throws Exception {
        java.lang.reflect.Method method =
                Orchestrator.class.getDeclaredMethod("onWorkerExit", String.class, boolean.class, String.class);
        method.setAccessible(true);
        method.invoke(orchestrator, issueId, normal, failureReason);
    }

    private static void invokeRunOneTick(Orchestrator orchestrator) throws Exception {
        java.lang.reflect.Method method = Orchestrator.class.getDeclaredMethod("runOneTick");
        method.setAccessible(true);
        method.invoke(orchestrator);
    }

    private static boolean invokeDispatchIssue(Orchestrator orchestrator, Issue issue) throws Exception {
        java.lang.reflect.Method method =
                Orchestrator.class.getDeclaredMethod("dispatchIssue", Issue.class, Integer.class);
        method.setAccessible(true);
        return (boolean) method.invoke(orchestrator, issue, null);
    }

    private static RetryEntry invokeScheduleRetry(Orchestrator orchestrator, Issue issue) throws Exception {
        java.lang.reflect.Method method = Orchestrator.class.getDeclaredMethod(
                "scheduleRetryLocked",
                String.class,
                String.class,
                int.class,
                String.class,
                String.class,
                String.class,
                String.class);
        method.setAccessible(true);
        return (RetryEntry)
                method.invoke(orchestrator, issue.id(), issue.identifier(), 1, null, null, null, "continuation");
    }

    private static Orchestrator createOrchestrator(
            RecordingTrackerClient tracker, RunHistoryRepository runHistoryRepository) {
        return createOrchestrator(tracker, runHistoryRepository, Map.of());
    }

    private static Orchestrator createOrchestrator(
            RecordingTrackerClient tracker,
            RunHistoryRepository runHistoryRepository,
            Map<String, Object> extraConfig) {
        Map<String, Object> config = new java.util.LinkedHashMap<>();
        config.put("tracker", Map.of("kind", "linear", "api_key", "k", "project_slug", "artemis"));
        config.put("workspace", Map.of("root", "./symphony_workspaces"));
        config.put("reporting", Map.of("agent_runs", Map.of("enabled", false)));
        config.putAll(extraConfig);
        WorkflowDefinition definition = new WorkflowDefinition(Map.copyOf(config), "Prompt");
        SymphonyRuntimeHolder holder = new SymphonyRuntimeHolder(
                Path.of("WORKFLOW.md"),
                new SymphonyRuntimeSnapshot(definition, new ServiceConfig(definition), tracker));
        WorkspaceManager workspaceManager =
                new WorkspaceManager(() -> holder.get().config());
        AgentRunner agentRunner =
                new AgentRunner(() -> holder.get().config(), workspaceManager, holder.get()::trackerClient);
        return new Orchestrator(holder, workspaceManager, agentRunner, runHistoryRepository);
    }

    private static Issue activeIssue() {
        return new Issue(
                "issue-1",
                "ART-1",
                "Run history termination",
                null,
                1,
                "In Progress",
                null,
                null,
                List.of(),
                List.of(),
                Instant.parse("2026-06-09T00:00:00Z"),
                Instant.parse("2026-06-09T00:30:00Z"));
    }

    private static Issue terminalIssue() {
        Issue active = activeIssue();
        return new Issue(
                active.id(),
                active.identifier(),
                active.title(),
                active.description(),
                active.priority(),
                "Done",
                active.branchName(),
                active.url(),
                active.labels(),
                active.blockedBy(),
                active.createdAt(),
                Instant.parse("2026-06-09T01:30:00Z"));
    }

    private static final class RecordingTrackerClient extends LinearTrackerClient {
        private final Issue issue;

        private RecordingTrackerClient(Issue issue) {
            super("https://api.linear.app/graphql", "k");
            this.issue = issue;
        }

        @Override
        public TrackerResult<List<Issue>> fetchIssueStatesByIds(List<String> issueIds) {
            if (issueIds.contains(issue.id())) {
                return TrackerResult.success(List.of(issue));
            }
            return TrackerResult.success(List.of());
        }

        @Override
        public TrackerResult<List<Issue>> fetchCandidateIssues(String projectSlug, List<String> activeStates) {
            return TrackerResult.success(List.of(issue));
        }
    }

    private static final class RecordingRunHistoryRepository implements RunHistoryRepository {
        private String finishedRunId;
        private String finishedStatus;
        private String finishedReason;
        private int retryScheduledCount;
        private int nextAttempt;
        private int runStartedCount;

        @Override
        public void recordRunStarted(RunHistoryRecord record) {
            this.runStartedCount++;
        }

        @Override
        public void recordRuntimeInfo(String runId, String workerHost, String workspacePath) {}

        @Override
        public void recordCodexEvent(String runId, CodexUpdateEvent event) {}

        @Override
        public void recordRunFinished(String runId, String status, String failureReason, Instant finishedAt) {
            this.finishedRunId = runId;
            this.finishedStatus = status;
            this.finishedReason = failureReason;
        }

        @Override
        public void markRunningRunsInterrupted(Instant interruptedAt, String reason) {}

        @Override
        public void recordRetryScheduled(String runId, int nextAttempt, Instant dueAt, String error) {
            this.retryScheduledCount++;
            this.nextAttempt = nextAttempt;
        }

        @Override
        public List<RunHistoryRecord> listRecentRuns(int limit) {
            return List.of();
        }

        @Override
        public List<RunHistoryEvent> listRunEvents(String runId, int limit) {
            return List.of();
        }

        @Override
        public RunHistoryMetrics summarizeRecentRuns(int limit) {
            return RunHistoryRepository.NOOP.summarizeRecentRuns(limit);
        }
    }
}
