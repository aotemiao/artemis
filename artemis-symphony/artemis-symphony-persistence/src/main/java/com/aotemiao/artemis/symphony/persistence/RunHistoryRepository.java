package com.aotemiao.artemis.symphony.persistence;

import com.aotemiao.artemis.symphony.core.model.CodexUpdateEvent;
import com.aotemiao.artemis.symphony.core.model.Issue;
import java.time.Instant;
import java.util.List;

public interface RunHistoryRepository {

    RunHistoryRepository NOOP = new NoopRunHistoryRepository();

    void recordRunStarted(RunHistoryRecord record);

    void recordRuntimeInfo(String runId, String workerHost, String workspacePath);

    void recordCodexEvent(String runId, CodexUpdateEvent event);

    void recordRunFinished(String runId, String status, String failureReason, Instant finishedAt);

    void markRunningRunsInterrupted(Instant interruptedAt, String reason);

    void recordRetryScheduled(String runId, int nextAttempt, Instant dueAt, String error);

    List<RunHistoryRecord> listRecentRuns(int limit);

    List<RunHistoryEvent> listRunEvents(String runId, int limit);

    RunHistoryMetrics summarizeRecentRuns(int limit);

    static RunHistoryRecord started(String runId, Issue issue, int attempt, Instant startedAt) {
        return new RunHistoryRecord(
                runId,
                issue != null ? issue.id() : "",
                issue != null ? issue.identifier() : "",
                issue != null ? issue.title() : "",
                issue != null ? issue.state() : "",
                "running",
                attempt,
                "",
                "",
                "",
                "",
                "",
                0L,
                0L,
                0L,
                "",
                startedAt,
                startedAt,
                null);
    }

    final class NoopRunHistoryRepository implements RunHistoryRepository {

        private NoopRunHistoryRepository() {}

        @Override
        public void recordRunStarted(RunHistoryRecord record) {}

        @Override
        public void recordRuntimeInfo(String runId, String workerHost, String workspacePath) {}

        @Override
        public void recordCodexEvent(String runId, CodexUpdateEvent event) {}

        @Override
        public void recordRunFinished(String runId, String status, String failureReason, Instant finishedAt) {}

        @Override
        public void markRunningRunsInterrupted(Instant interruptedAt, String reason) {}

        @Override
        public void recordRetryScheduled(String runId, int nextAttempt, Instant dueAt, String error) {}

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
            return new RunHistoryMetrics(
                    limit <= 0 ? 50 : Math.min(limit, 500),
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0L,
                    0L,
                    0L,
                    0.0,
                    null,
                    null,
                    java.util.Map.of(),
                    java.util.Map.of());
        }
    }
}
