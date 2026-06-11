package com.aotemiao.artemis.symphony.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aotemiao.artemis.symphony.core.model.CodexUpdateEvent;
import com.aotemiao.artemis.symphony.core.model.Issue;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SqliteRunHistoryRepositoryTest {

    @TempDir
    java.nio.file.Path tempDir;

    @Test
    void recordsRunLifecycleAndEvents() {
        SqliteRunHistoryRepository repository = new SqliteRunHistoryRepository(tempDir.resolve("symphony-runs.sqlite"));
        Issue issue = new Issue(
                "issue-1",
                "ART-1",
                "SQLite run history",
                "record one attempt",
                1,
                "In Progress",
                null,
                "https://linear.app/example/ART-1",
                null,
                List.of("agent"),
                List.of(),
                true,
                Instant.parse("2026-06-03T01:00:00Z"),
                Instant.parse("2026-06-03T01:30:00Z"));
        Instant startedAt = Instant.parse("2026-06-03T02:00:00Z");

        repository.recordRunStarted(RunHistoryRepository.started("run-1", issue, 0, startedAt));
        repository.recordRunStarted(RunHistoryRepository.started("run-1", issue, 0, startedAt));
        repository.recordRuntimeInfo("run-1", "worker-1", "/tmp/symphony/ART-1");
        repository.recordCodexEvent(
                "run-1",
                new CodexUpdateEvent(
                        "session_started",
                        Instant.parse("2026-06-03T02:00:05Z"),
                        "4321",
                        Map.of("input_tokens", 11, "output_tokens", 22, "total_tokens", 33),
                        Map.of(
                                "session_id",
                                "session-1",
                                "thread_id",
                                "thread-1",
                                "worker_host",
                                "worker-1",
                                "codex_app_server_pid",
                                "4321",
                                "rate_limits",
                                Map.of("primary", Map.of("usedPercent", 1)))));
        repository.recordRunFinished("run-1", "failed", "codex turn failed", Instant.parse("2026-06-03T02:01:00Z"));

        List<RunHistoryRecord> runs = repository.listRecentRuns(10);
        assertEquals(1, runs.size());
        RunHistoryRecord run = runs.get(0);
        assertEquals("run-1", run.runId());
        assertEquals("ART-1", run.issueIdentifier());
        assertEquals("failed", run.status());
        assertEquals("worker-1", run.workerHost());
        assertEquals("/tmp/symphony/ART-1", run.workspacePath());
        assertEquals("thread-1", run.threadId());
        assertEquals("session-1", run.sessionId());
        assertEquals("4321", run.codexAppServerPid());
        assertEquals(33L, run.totalTokens());
        assertEquals("codex turn failed", run.failureReason());

        List<RunHistoryEvent> events = repository.listRunEvents("run-1", 10);
        assertEquals(3, events.size());
        assertEquals("run_started", events.get(0).eventType());
        assertTrue(events.get(0).payload().contains("\"issue_identifier\":\"ART-1\""));
        assertEquals("session_started", events.get(1).eventType());
        assertTrue(events.get(1).payload().contains("\"rate_limits\""));
        assertEquals("run_failed", events.get(2).eventType());

        RunHistoryMetrics metrics = repository.summarizeRecentRuns(10);
        assertEquals(1, metrics.totalRuns());
        assertEquals(1, metrics.failedRuns());
        assertEquals(33L, metrics.totalTokens());
        assertEquals(Map.of("failed", 1), metrics.statusCounts());
        assertEquals(Map.of("codex_runtime", 1), metrics.failureCategoryCounts());
    }

    @Test
    void boundsListLimits() {
        SqliteRunHistoryRepository repository = new SqliteRunHistoryRepository(tempDir.resolve("bounded-runs.sqlite"));
        for (int i = 0; i < 3; i++) {
            Issue issue = new Issue(
                    "issue-" + i,
                    "ART-" + i,
                    "Run " + i,
                    null,
                    1,
                    "Todo",
                    null,
                    null,
                    null,
                    List.of(),
                    List.of(),
                    true,
                    Instant.now(),
                    Instant.now());
            repository.recordRunStarted(RunHistoryRepository.started("run-" + i, issue, 0, Instant.now()));
        }

        assertEquals(2, repository.listRecentRuns(2).size());
        assertEquals(3, repository.listRecentRuns(0).size());
        assertEquals(List.of(), repository.listRunEvents("", 10));
    }

    @Test
    void interruptsStaleRunningRuns() {
        SqliteRunHistoryRepository repository =
                new SqliteRunHistoryRepository(tempDir.resolve("interrupted-runs.sqlite"));
        Instant startedAt = Instant.parse("2026-06-03T03:00:00Z");
        repository.recordRunStarted(
                RunHistoryRepository.started("run-running-1", issue("issue-running-1", "ART-101"), 0, startedAt));
        repository.recordRunStarted(RunHistoryRepository.started(
                "run-running-2", issue("issue-running-2", "ART-102"), 1, startedAt.plusSeconds(1)));
        repository.recordRunStarted(RunHistoryRepository.started(
                "run-finished", issue("issue-finished", "ART-103"), 0, startedAt.plusSeconds(2)));
        repository.recordRunFinished("run-finished", "succeeded", "", Instant.parse("2026-06-03T03:05:00Z"));

        repository.markRunningRunsInterrupted(Instant.parse("2026-06-03T04:00:00Z"), "startup recovery");

        Map<String, RunHistoryRecord> runsById = repository.listRecentRuns(10).stream()
                .collect(java.util.stream.Collectors.toMap(RunHistoryRecord::runId, run -> run));
        assertEquals("interrupted", runsById.get("run-running-1").status());
        assertEquals("startup recovery", runsById.get("run-running-1").failureReason());
        assertEquals(
                Instant.parse("2026-06-03T04:00:00Z"),
                runsById.get("run-running-1").finishedAt());
        assertEquals("interrupted", runsById.get("run-running-2").status());
        assertEquals("succeeded", runsById.get("run-finished").status());

        List<String> interruptedEvents = repository.listRunEvents("run-running-1", 10).stream()
                .map(RunHistoryEvent::eventType)
                .toList();
        assertTrue(interruptedEvents.contains("run_started"));
        assertTrue(interruptedEvents.contains("run_interrupted"));
        assertEquals(
                List.of("run_started", "run_succeeded"),
                repository.listRunEvents("run-finished", 10).stream()
                        .map(RunHistoryEvent::eventType)
                        .toList());
    }

    @Test
    void summarizeRecentRuns_aggregatesBoundedRuns() {
        SqliteRunHistoryRepository repository = new SqliteRunHistoryRepository(tempDir.resolve("metrics-runs.sqlite"));
        Instant base = Instant.parse("2026-06-03T04:00:00Z");
        repository.recordRunStarted(RunHistoryRepository.started("run-completed", issue("issue-1", "ART-1"), 0, base));
        repository.recordRetryScheduled("run-completed", 2, base.plusSeconds(25), "codex turn failed");
        repository.recordCodexEvent(
                "run-completed",
                new CodexUpdateEvent(
                        "thread/tokenUsage/updated",
                        base.plusSeconds(10),
                        "thread-1 - turn-1",
                        Map.of("input_tokens", 8L, "output_tokens", 13L, "total_tokens", 21L),
                        Map.of("usage", Map.of("total_tokens", 21L))));
        repository.recordRunFinished("run-completed", "completed", "", base.plusSeconds(20));
        repository.recordRunStarted(
                RunHistoryRepository.started("run-terminated", issue("issue-2", "ART-2"), 1, base.plusSeconds(30)));
        repository.recordRunFinished("run-terminated", "terminated", "not active", base.plusSeconds(35));
        repository.recordRunStarted(
                RunHistoryRepository.started("run-continuation", issue("issue-3", "ART-3"), 1, base.plusSeconds(40)));
        repository.recordRunFinished("run-continuation", "completed", "", base.plusSeconds(45));

        RunHistoryMetrics metrics = repository.summarizeRecentRuns(100);

        assertEquals(100, metrics.limit());
        assertEquals(3, metrics.totalRuns());
        assertEquals(2, metrics.completedRuns());
        assertEquals(1, metrics.terminatedRuns());
        assertEquals(1, metrics.retriedRuns());
        assertEquals(21L, metrics.totalTokens());
        assertEquals(Map.of("completed", 2, "terminated", 1), metrics.statusCounts());
        assertEquals(Map.of("terminated", 1), metrics.failureCategoryCounts());
        assertEquals(base, metrics.earliestStartedAt());
        assertEquals(base.plusSeconds(45), metrics.latestUpdatedAt());
        assertTrue(metrics.averageDurationSeconds() > 0.0);
    }

    private static Issue issue(String id, String identifier) {
        return new Issue(
                id,
                identifier,
                "Run history recovery",
                null,
                1,
                "In Progress",
                null,
                null,
                null,
                List.of("agent"),
                List.of(),
                true,
                Instant.parse("2026-06-03T01:00:00Z"),
                Instant.parse("2026-06-03T01:30:00Z"));
    }
}
