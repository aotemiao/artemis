package com.aotemiao.artemis.symphony.orchestrator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aotemiao.artemis.symphony.config.ServiceConfig;
import com.aotemiao.artemis.symphony.core.model.Issue;
import com.aotemiao.artemis.symphony.core.model.RetryEntry;
import com.aotemiao.artemis.symphony.core.model.WorkflowDefinition;
import com.aotemiao.artemis.symphony.tracker.LinearTrackerClient;
import com.aotemiao.artemis.symphony.tracker.TrackerResult;
import com.aotemiao.artemis.symphony.workspace.WorkspaceManager;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;

class OrchestratorLinearCommentTest {

    @Test
    void reportAttemptOutcome_postsCommentWhenEnabled() {
        RecordingTrackerClient tracker = new RecordingTrackerClient();
        Orchestrator orchestrator = createOrchestrator(true, null, tracker);

        Issue issue = new Issue(
                "issue-1",
                "ART-1",
                "进度汇报",
                "整理当前进度",
                1,
                "Todo",
                null,
                "https://linear.app/example/ART-1",
                List.of(),
                List.of(),
                Instant.parse("2026-03-25T01:00:00Z"),
                Instant.parse("2026-03-25T01:30:00Z"));
        RunningEntry entry =
                new RunningEntry("issue-1", "ART-1", issue, 0, Instant.now().minusSeconds(12));
        entry.turnCount = 2;
        entry.lastCodexEvent = "turn_completed";
        entry.lastCodexTimestamp = Instant.now().minusSeconds(1);
        entry.codexInputTokens = 11;
        entry.codexOutputTokens = 22;
        entry.codexTotalTokens = 33;

        orchestrator.reportAttemptOutcome(
                entry,
                true,
                new RetryEntry("issue-1", "ART-1", 1, System.currentTimeMillis() + 1000, null, null),
                null);

        assertEquals("issue-1", tracker.issueId);
        assertTrue(tracker.body.contains("ART-1"));
        assertTrue(tracker.body.contains("已完成本轮处理"));
        assertTrue(tracker.body.contains("turn_completed"));
    }

    @Test
    void reportAttemptOutcome_skipsCommentWhenDisabled() {
        RecordingTrackerClient tracker = new RecordingTrackerClient();
        Orchestrator orchestrator = createOrchestrator(false, null, tracker);

        Issue issue = new Issue(
                "issue-2",
                "ART-2",
                "普通任务",
                null,
                1,
                "Todo",
                null,
                null,
                List.of(),
                List.of(),
                Instant.now(),
                Instant.now());
        RunningEntry entry = new RunningEntry("issue-2", "ART-2", issue, 0, Instant.now());

        orchestrator.reportAttemptOutcome(entry, false, null, "codex turn failed");

        assertNull(tracker.issueId);
    }

    @Test
    void reportAttemptOutcome_skipsCommentWhenTitleDoesNotMatchFilter() {
        RecordingTrackerClient tracker = new RecordingTrackerClient();
        Orchestrator orchestrator = createOrchestrator(true, "^进度汇报$", tracker);

        Issue issue = new Issue(
                "issue-3",
                "ART-3",
                "普通任务",
                null,
                1,
                "Todo",
                null,
                null,
                List.of(),
                List.of(),
                Instant.now(),
                Instant.now());
        RunningEntry entry = new RunningEntry("issue-3", "ART-3", issue, 0, Instant.now());

        orchestrator.reportAttemptOutcome(entry, true, null, null);

        assertNull(tracker.issueId);
    }

    @Test
    void claimTodoIssueIfNeeded_updatesTrackerStateToInProgress() {
        RecordingTrackerClient tracker = new RecordingTrackerClient();
        Orchestrator orchestrator = createOrchestrator(false, null, tracker);

        Issue issue = new Issue(
                "issue-4",
                "ART-4",
                "项目进度汇报",
                null,
                1,
                "Todo",
                null,
                null,
                List.of(),
                List.of(),
                Instant.now(),
                Instant.now());
        tracker.putIssue(issue);

        Issue claimed = orchestrator.claimTodoIssueIfNeeded(issue);

        assertEquals("issue-4", tracker.updatedIssueId);
        assertEquals("In Progress", tracker.updatedStateName);
        assertEquals("In Progress", claimed.state());
    }

    private static Orchestrator createOrchestrator(
            boolean enabled, String issueTitleRegex, RecordingTrackerClient tracker) {
        Map<String, Object> linearCommentConfig = new java.util.LinkedHashMap<>();
        linearCommentConfig.put("enabled", enabled);
        if (issueTitleRegex != null) {
            linearCommentConfig.put("issue_title_regex", issueTitleRegex);
        }
        WorkflowDefinition definition = new WorkflowDefinition(
                Map.of(
                        "tracker",
                        Map.of(
                                "kind", "linear",
                                "api_key", "k",
                                "project_slug", "artemis"),
                        "workspace",
                        Map.of("root", "./symphony_workspaces"),
                        "reporting",
                        Map.of("linear_comments", Map.copyOf(linearCommentConfig))),
                "Prompt");
        SymphonyRuntimeHolder holder = new SymphonyRuntimeHolder(
                Path.of("WORKFLOW.md"),
                new SymphonyRuntimeSnapshot(definition, new ServiceConfig(definition), tracker));
        WorkspaceManager workspaceManager =
                new WorkspaceManager(() -> holder.get().config());
        AgentRunner agentRunner =
                new AgentRunner(() -> holder.get().config(), workspaceManager, holder.get()::trackerClient);
        return new Orchestrator(holder, workspaceManager, agentRunner);
    }

    private static final class RecordingTrackerClient extends LinearTrackerClient {
        private String issueId;
        private String body;
        private String updatedIssueId;
        private String updatedStateName;
        private final Map<String, Issue> issuesById = new ConcurrentHashMap<>();

        private RecordingTrackerClient() {
            super("https://api.linear.app/graphql", "k");
        }

        private void putIssue(Issue issue) {
            issuesById.put(issue.id(), issue);
        }

        @Override
        public TrackerResult<String> createIssueComment(String issueId, String body) {
            this.issueId = issueId;
            this.body = body;
            return TrackerResult.success("comment-1");
        }

        @Override
        public TrackerResult<Void> updateIssueState(String issueId, String stateName) {
            this.updatedIssueId = issueId;
            this.updatedStateName = stateName;
            Issue issue = issuesById.get(issueId);
            if (issue != null) {
                issuesById.put(
                        issueId,
                        new Issue(
                                issue.id(),
                                issue.identifier(),
                                issue.title(),
                                issue.description(),
                                issue.priority(),
                                stateName,
                                issue.branchName(),
                                issue.url(),
                                issue.labels(),
                                issue.blockedBy(),
                                issue.createdAt(),
                                Instant.now()));
            }
            return TrackerResult.success(null);
        }

        @Override
        public TrackerResult<List<Issue>> fetchIssueStatesByIds(List<String> issueIds) {
            return TrackerResult.success(issueIds.stream()
                    .map(issuesById::get)
                    .filter(java.util.Objects::nonNull)
                    .toList());
        }
    }
}
