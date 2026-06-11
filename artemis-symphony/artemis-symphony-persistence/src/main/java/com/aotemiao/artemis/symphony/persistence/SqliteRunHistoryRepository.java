package com.aotemiao.artemis.symphony.persistence;

import com.aotemiao.artemis.symphony.core.model.CodexUpdateEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SqliteRunHistoryRepository implements RunHistoryRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqliteRunHistoryRepository.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int MAX_PAYLOAD_CHARS = 8_000;

    private final Path databasePath;
    private final String jdbcUrl;

    public SqliteRunHistoryRepository(Path databasePath) {
        if (databasePath == null) {
            throw new IllegalArgumentException("databasePath is required");
        }
        this.databasePath = databasePath.toAbsolutePath().normalize();
        this.jdbcUrl = "jdbc:sqlite:" + this.databasePath;
        initialize();
    }

    public Path getDatabasePath() {
        return databasePath;
    }

    @Override
    public synchronized void recordRunStarted(RunHistoryRecord record) {
        if (record == null || isBlank(record.runId())) {
            return;
        }
        bestEffort("recordRunStarted", () -> {
            try (Connection connection = openConnection();
                    PreparedStatement statement = connection.prepareStatement("""
                            INSERT INTO symphony_runs (
                              run_id, issue_id, issue_identifier, issue_title, tracker_state, status,
                              attempt, worker_host, workspace_path, thread_id, session_id, codex_app_server_pid,
                              input_tokens, output_tokens, total_tokens, failure_reason,
                              started_at, updated_at, finished_at
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            ON CONFLICT(run_id) DO UPDATE SET
                              issue_id = excluded.issue_id,
                              issue_identifier = excluded.issue_identifier,
                              issue_title = excluded.issue_title,
                              tracker_state = excluded.tracker_state,
                              status = excluded.status,
                              attempt = excluded.attempt,
                              worker_host = COALESCE(NULLIF(excluded.worker_host, ''), worker_host),
                              workspace_path = COALESCE(NULLIF(excluded.workspace_path, ''), workspace_path),
                              updated_at = excluded.updated_at
                            """)) {
                bindRun(statement, record);
                statement.executeUpdate();
            }
            appendRunStartedEventIfAbsent(record);
        });
    }

    @Override
    public synchronized void recordRuntimeInfo(String runId, String workerHost, String workspacePath) {
        if (isBlank(runId)) {
            return;
        }
        bestEffort("recordRuntimeInfo", () -> {
            try (Connection connection = openConnection();
                    PreparedStatement statement = connection.prepareStatement("""
                            UPDATE symphony_runs
                            SET worker_host = COALESCE(NULLIF(?, ''), worker_host),
                                workspace_path = COALESCE(NULLIF(?, ''), workspace_path),
                                updated_at = ?
                            WHERE run_id = ?
                            """)) {
                statement.setString(1, nullToEmpty(workerHost));
                statement.setString(2, nullToEmpty(workspacePath));
                statement.setString(3, Instant.now().toString());
                statement.setString(4, runId);
                statement.executeUpdate();
            }
        });
    }

    @Override
    public synchronized void recordCodexEvent(String runId, CodexUpdateEvent event) {
        if (isBlank(runId) || event == null) {
            return;
        }
        bestEffort("recordCodexEvent", () -> {
            appendEvent(runId, event.timestamp(), event.event(), sessionId(event), payloadJson(event));
            updateRunFromEvent(runId, event);
        });
    }

    @Override
    public synchronized void recordRunFinished(String runId, String status, String failureReason, Instant finishedAt) {
        if (isBlank(runId)) {
            return;
        }
        Instant resolvedFinishedAt = finishedAt != null ? finishedAt : Instant.now();
        bestEffort("recordRunFinished", () -> {
            try (Connection connection = openConnection();
                    PreparedStatement statement = connection.prepareStatement("""
                            UPDATE symphony_runs
                            SET status = ?,
                                failure_reason = ?,
                                finished_at = ?,
                                updated_at = ?
                            WHERE run_id = ?
                            """)) {
                statement.setString(1, blankToDefault(status, "finished"));
                statement.setString(2, nullToEmpty(failureReason));
                statement.setString(3, resolvedFinishedAt.toString());
                statement.setString(4, resolvedFinishedAt.toString());
                statement.setString(5, runId);
                statement.executeUpdate();
            }
            appendEvent(
                    runId,
                    resolvedFinishedAt,
                    "run_" + blankToDefault(status, "finished"),
                    "",
                    json(Map.of("failure_reason", nullToEmpty(failureReason))));
        });
    }

    @Override
    public synchronized void markRunningRunsInterrupted(Instant interruptedAt, String reason) {
        Instant resolvedInterruptedAt = interruptedAt != null ? interruptedAt : Instant.now();
        String resolvedReason = blankToDefault(reason, "Symphony restarted before run finished");
        bestEffort("markRunningRunsInterrupted", () -> {
            try (Connection connection = openConnection()) {
                connection.setAutoCommit(false);
                List<String> runIds = new ArrayList<>();
                try (PreparedStatement select = connection.prepareStatement("""
                        SELECT run_id
                        FROM symphony_runs
                        WHERE status = 'running'
                          AND finished_at IS NULL
                        """)) {
                    try (ResultSet resultSet = select.executeQuery()) {
                        while (resultSet.next()) {
                            runIds.add(resultSet.getString("run_id"));
                        }
                    }
                }
                if (runIds.isEmpty()) {
                    connection.commit();
                    return;
                }
                List<String> interruptedRunIds = new ArrayList<>();
                try (PreparedStatement update = connection.prepareStatement("""
                        UPDATE symphony_runs
                        SET status = 'interrupted',
                            failure_reason = ?,
                            finished_at = ?,
                            updated_at = ?
                        WHERE status = 'running'
                          AND finished_at IS NULL
                          AND run_id = ?
                        """)) {
                    for (String runId : runIds) {
                        update.setString(1, resolvedReason);
                        update.setString(2, resolvedInterruptedAt.toString());
                        update.setString(3, resolvedInterruptedAt.toString());
                        update.setString(4, runId);
                        int updated = update.executeUpdate();
                        if (updated > 0) {
                            interruptedRunIds.add(runId);
                        }
                    }
                }
                try (PreparedStatement insert = connection.prepareStatement("""
                        INSERT INTO symphony_run_events (run_id, event_time, event_type, session_id, payload)
                        VALUES (?, ?, ?, ?, ?)
                        """)) {
                    String payload = json(Map.of("failure_reason", resolvedReason));
                    for (String runId : interruptedRunIds) {
                        insert.setString(1, runId);
                        insert.setString(2, resolvedInterruptedAt.toString());
                        insert.setString(3, "run_interrupted");
                        insert.setString(4, "");
                        insert.setString(5, truncate(payload, MAX_PAYLOAD_CHARS));
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
                connection.commit();
                LOGGER.info(
                        "action=symphony_history_recover_running outcome=interrupted count={}",
                        interruptedRunIds.size());
            }
        });
    }

    @Override
    public synchronized void recordRetryScheduled(String runId, int nextAttempt, Instant dueAt, String error) {
        if (isBlank(runId)) {
            return;
        }
        Instant now = Instant.now();
        bestEffort(
                "recordRetryScheduled",
                () -> appendEvent(
                        runId,
                        now,
                        "retry_scheduled",
                        "",
                        json(Map.of(
                                "next_attempt",
                                nextAttempt,
                                "due_at",
                                dueAt != null ? dueAt.toString() : "",
                                "error",
                                nullToEmpty(error)))));
    }

    @Override
    public synchronized List<RunHistoryRecord> listRecentRuns(int limit) {
        int boundedLimit = boundedLimit(limit);
        List<RunHistoryRecord> records = new ArrayList<>();
        bestEffort("listRecentRuns", () -> {
            try (Connection connection = openConnection();
                    PreparedStatement statement = connection.prepareStatement("""
                            SELECT run_id, issue_id, issue_identifier, issue_title, tracker_state, status,
                                   attempt, worker_host, workspace_path, thread_id, session_id, codex_app_server_pid,
                                   input_tokens, output_tokens, total_tokens, failure_reason,
                                   started_at, updated_at, finished_at
                            FROM symphony_runs
                            ORDER BY updated_at DESC, started_at DESC
                            LIMIT ?
                            """)) {
                statement.setInt(1, boundedLimit);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        records.add(mapRun(resultSet));
                    }
                }
            }
        });
        return List.copyOf(records);
    }

    @Override
    public synchronized List<RunHistoryEvent> listRunEvents(String runId, int limit) {
        if (isBlank(runId)) {
            return List.of();
        }
        int boundedLimit = boundedLimit(limit);
        List<RunHistoryEvent> events = new ArrayList<>();
        bestEffort("listRunEvents", () -> {
            try (Connection connection = openConnection();
                    PreparedStatement statement = connection.prepareStatement("""
                            SELECT id, run_id, event_time, event_type, session_id, payload
                            FROM symphony_run_events
                            WHERE run_id = ?
                            ORDER BY event_time ASC, id ASC
                            LIMIT ?
                            """)) {
                statement.setString(1, runId);
                statement.setInt(2, boundedLimit);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        events.add(new RunHistoryEvent(
                                resultSet.getLong("id"),
                                resultSet.getString("run_id"),
                                parseInstant(resultSet.getString("event_time")),
                                resultSet.getString("event_type"),
                                resultSet.getString("session_id"),
                                resultSet.getString("payload")));
                    }
                }
            }
        });
        return List.copyOf(events);
    }

    @Override
    public synchronized RunHistoryMetrics summarizeRecentRuns(int limit) {
        int boundedLimit = boundedLimit(limit);
        List<RunHistoryRecord> runs = new ArrayList<>();
        Map<String, Boolean> retryScheduledByRunId = new LinkedHashMap<>();
        bestEffort("summarizeRecentRuns", () -> {
            try (Connection connection = openConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("""
                        SELECT run_id, issue_id, issue_identifier, issue_title, tracker_state, status,
                               attempt, worker_host, workspace_path, thread_id, session_id, codex_app_server_pid,
                               input_tokens, output_tokens, total_tokens, failure_reason,
                               started_at, updated_at, finished_at
                        FROM symphony_runs
                        ORDER BY updated_at DESC, started_at DESC
                        LIMIT ?
                        """)) {
                    statement.setInt(1, boundedLimit);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            runs.add(mapRun(resultSet));
                        }
                    }
                }
                if (!runs.isEmpty()) {
                    try (PreparedStatement statement = connection.prepareStatement("""
                            SELECT DISTINCT run_id
                            FROM symphony_run_events
                            WHERE event_type = 'retry_scheduled'
                            """);
                            ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            retryScheduledByRunId.put(resultSet.getString("run_id"), true);
                        }
                    }
                }
            }
        });
        return summarize(runs, boundedLimit, retryScheduledByRunId);
    }

    private void initialize() {
        bestEffort("initialize", () -> {
            Path parent = databasePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Connection connection = openConnection();
                    Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA journal_mode=WAL");
                statement.execute("PRAGMA busy_timeout=5000");
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS symphony_runs (
                          run_id TEXT PRIMARY KEY,
                          issue_id TEXT NOT NULL DEFAULT '',
                          issue_identifier TEXT NOT NULL DEFAULT '',
                          issue_title TEXT NOT NULL DEFAULT '',
                          tracker_state TEXT NOT NULL DEFAULT '',
                          status TEXT NOT NULL DEFAULT '',
                          attempt INTEGER NOT NULL DEFAULT 0,
                          worker_host TEXT NOT NULL DEFAULT '',
                          workspace_path TEXT NOT NULL DEFAULT '',
                          thread_id TEXT NOT NULL DEFAULT '',
                          session_id TEXT NOT NULL DEFAULT '',
                          codex_app_server_pid TEXT NOT NULL DEFAULT '',
                          input_tokens INTEGER NOT NULL DEFAULT 0,
                          output_tokens INTEGER NOT NULL DEFAULT 0,
                          total_tokens INTEGER NOT NULL DEFAULT 0,
                          failure_reason TEXT NOT NULL DEFAULT '',
                          started_at TEXT NOT NULL,
                          updated_at TEXT NOT NULL,
                          finished_at TEXT
                        )
                        """);
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS symphony_run_events (
                          id INTEGER PRIMARY KEY AUTOINCREMENT,
                          run_id TEXT NOT NULL,
                          event_time TEXT NOT NULL,
                          event_type TEXT NOT NULL,
                          session_id TEXT NOT NULL DEFAULT '',
                          payload TEXT NOT NULL DEFAULT '',
                          FOREIGN KEY(run_id) REFERENCES symphony_runs(run_id)
                        )
                        """);
                statement.execute(
                        "CREATE INDEX IF NOT EXISTS idx_symphony_runs_updated_at ON symphony_runs(updated_at DESC)");
                statement.execute(
                        "CREATE INDEX IF NOT EXISTS idx_symphony_runs_issue_identifier ON symphony_runs(issue_identifier)");
                statement.execute(
                        "CREATE INDEX IF NOT EXISTS idx_symphony_run_events_run_id ON symphony_run_events(run_id, event_time)");
            }
        });
    }

    private void bindRun(PreparedStatement statement, RunHistoryRecord record) throws SQLException {
        statement.setString(1, record.runId());
        statement.setString(2, nullToEmpty(record.issueId()));
        statement.setString(3, nullToEmpty(record.issueIdentifier()));
        statement.setString(4, nullToEmpty(record.issueTitle()));
        statement.setString(5, nullToEmpty(record.trackerState()));
        statement.setString(6, nullToEmpty(record.status()));
        statement.setInt(7, record.attempt());
        statement.setString(8, nullToEmpty(record.workerHost()));
        statement.setString(9, nullToEmpty(record.workspacePath()));
        statement.setString(10, nullToEmpty(record.threadId()));
        statement.setString(11, nullToEmpty(record.sessionId()));
        statement.setString(12, nullToEmpty(record.codexAppServerPid()));
        statement.setLong(13, record.inputTokens());
        statement.setLong(14, record.outputTokens());
        statement.setLong(15, record.totalTokens());
        statement.setString(16, nullToEmpty(record.failureReason()));
        statement.setString(17, instantString(record.startedAt()));
        statement.setString(18, instantString(record.updatedAt()));
        statement.setString(
                19, record.finishedAt() != null ? record.finishedAt().toString() : null);
    }

    private void appendEvent(String runId, Instant eventTime, String eventType, String sessionId, String payload)
            throws SQLException {
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO symphony_run_events (run_id, event_time, event_type, session_id, payload)
                        VALUES (?, ?, ?, ?, ?)
                        """)) {
            statement.setString(1, runId);
            statement.setString(2, instantString(eventTime));
            statement.setString(3, blankToDefault(eventType, "event"));
            statement.setString(4, nullToEmpty(sessionId));
            statement.setString(5, truncate(nullToEmpty(payload), MAX_PAYLOAD_CHARS));
            statement.executeUpdate();
        }
    }

    private void appendRunStartedEventIfAbsent(RunHistoryRecord record) throws SQLException {
        try (Connection connection = openConnection()) {
            try (PreparedStatement select = connection.prepareStatement("""
                    SELECT 1
                    FROM symphony_run_events
                    WHERE run_id = ?
                      AND event_type = 'run_started'
                    LIMIT 1
                    """)) {
                select.setString(1, record.runId());
                try (ResultSet resultSet = select.executeQuery()) {
                    if (resultSet.next()) {
                        return;
                    }
                }
            }
            try (PreparedStatement insert = connection.prepareStatement("""
                    INSERT INTO symphony_run_events (run_id, event_time, event_type, session_id, payload)
                    VALUES (?, ?, ?, ?, ?)
                    """)) {
                insert.setString(1, record.runId());
                insert.setString(2, instantString(record.startedAt()));
                insert.setString(3, "run_started");
                insert.setString(4, "");
                insert.setString(5, truncate(runStartedPayload(record), MAX_PAYLOAD_CHARS));
                insert.executeUpdate();
            }
        }
    }

    private void updateRunFromEvent(String runId, CodexUpdateEvent event) throws SQLException {
        String threadId = stringPayload(event, "thread_id");
        String sessionId = sessionId(event);
        String workerHost = stringPayload(event, "worker_host");
        String pid = blankToDefault(stringPayload(event, "codex_app_server_pid"), event.codexAppServerPid());
        long inputTokens = usageLong(event, "input_tokens");
        long outputTokens = usageLong(event, "output_tokens");
        long totalTokens = usageLong(event, "total_tokens");
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement("""
                        UPDATE symphony_runs
                        SET thread_id = COALESCE(NULLIF(?, ''), thread_id),
                            session_id = COALESCE(NULLIF(?, ''), session_id),
                            worker_host = COALESCE(NULLIF(?, ''), worker_host),
                            codex_app_server_pid = COALESCE(NULLIF(?, ''), codex_app_server_pid),
                            input_tokens = CASE WHEN ? > 0 THEN ? ELSE input_tokens END,
                            output_tokens = CASE WHEN ? > 0 THEN ? ELSE output_tokens END,
                            total_tokens = CASE WHEN ? > 0 THEN ? ELSE total_tokens END,
                            updated_at = ?
                        WHERE run_id = ?
                        """)) {
            statement.setString(1, threadId);
            statement.setString(2, sessionId);
            statement.setString(3, workerHost);
            statement.setString(4, pid);
            statement.setLong(5, inputTokens);
            statement.setLong(6, inputTokens);
            statement.setLong(7, outputTokens);
            statement.setLong(8, outputTokens);
            statement.setLong(9, totalTokens);
            statement.setLong(10, totalTokens);
            statement.setString(11, instantString(event.timestamp()));
            statement.setString(12, runId);
            statement.executeUpdate();
        }
    }

    private RunHistoryRecord mapRun(ResultSet resultSet) throws SQLException {
        return new RunHistoryRecord(
                resultSet.getString("run_id"),
                resultSet.getString("issue_id"),
                resultSet.getString("issue_identifier"),
                resultSet.getString("issue_title"),
                resultSet.getString("tracker_state"),
                resultSet.getString("status"),
                resultSet.getInt("attempt"),
                resultSet.getString("worker_host"),
                resultSet.getString("workspace_path"),
                resultSet.getString("thread_id"),
                resultSet.getString("session_id"),
                resultSet.getString("codex_app_server_pid"),
                resultSet.getLong("input_tokens"),
                resultSet.getLong("output_tokens"),
                resultSet.getLong("total_tokens"),
                resultSet.getString("failure_reason"),
                parseInstant(resultSet.getString("started_at")),
                parseInstant(resultSet.getString("updated_at")),
                parseNullableInstant(resultSet.getString("finished_at")));
    }

    private Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA busy_timeout=5000");
        }
        return connection;
    }

    private void bestEffort(String action, SqlRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            LOGGER.warn(
                    "action=symphony_history_{} outcome=failed database_path={} reason={}",
                    action,
                    databasePath,
                    e.toString());
        }
    }

    private static String payloadJson(CodexUpdateEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", event.event());
        payload.put("codex_app_server_pid", event.codexAppServerPid() != null ? event.codexAppServerPid() : "");
        payload.put("payload", event.payload() != null ? event.payload() : Map.of());
        payload.put("usage", event.usage() != null ? event.usage() : Map.of());
        payload.put(
                "rate_limits",
                event.payload() != null ? event.payload().getOrDefault("rate_limits", Map.of()) : Map.of());
        return json(payload);
    }

    private static String runStartedPayload(RunHistoryRecord record) {
        return json(Map.of(
                "issue_id",
                nullToEmpty(record.issueId()),
                "issue_identifier",
                nullToEmpty(record.issueIdentifier()),
                "tracker_state",
                nullToEmpty(record.trackerState()),
                "attempt",
                record.attempt()));
    }

    private static String json(Object payload) {
        try {
            return MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return String.valueOf(payload);
        }
    }

    private static String sessionId(CodexUpdateEvent event) {
        return stringPayload(event, "session_id");
    }

    private static String stringPayload(CodexUpdateEvent event, String key) {
        if (event == null || event.payload() == null || key == null) {
            return "";
        }
        Object value = event.payload().get(key);
        return value != null ? value.toString() : "";
    }

    private static long usageLong(CodexUpdateEvent event, String key) {
        if (event == null || event.usage() == null || key == null) {
            return 0L;
        }
        Object value = event.usage().get(key);
        return value instanceof Number number ? number.longValue() : 0L;
    }

    private static int boundedLimit(int limit) {
        if (limit <= 0) {
            return 50;
        }
        return Math.min(limit, 500);
    }

    private static RunHistoryMetrics summarize(
            List<RunHistoryRecord> runs, int limit, Map<String, Boolean> retryScheduledByRunId) {
        int completed = 0;
        int failed = 0;
        int terminated = 0;
        int interrupted = 0;
        int running = 0;
        int retried = 0;
        long inputTokens = 0L;
        long outputTokens = 0L;
        long totalTokens = 0L;
        long durationMillis = 0L;
        int durationCount = 0;
        Instant earliestStartedAt = null;
        Instant latestUpdatedAt = null;
        Map<String, Integer> statusCounts = new LinkedHashMap<>();
        Map<String, Integer> failureCategoryCounts = new LinkedHashMap<>();

        for (RunHistoryRecord run : runs) {
            String status = normalizeStatus(run.status());
            statusCounts.merge(status, 1, Integer::sum);
            String failureCategory = FailureCategoryClassifier.classify(status, run.failureReason());
            if (!FailureCategoryClassifier.NONE.equals(failureCategory)) {
                failureCategoryCounts.merge(failureCategory, 1, Integer::sum);
            }
            switch (status) {
                case "completed" -> completed++;
                case "failed" -> failed++;
                case "terminated" -> terminated++;
                case "interrupted" -> interrupted++;
                case "running" -> running++;
                default -> {
                    if (status.contains("fail")) {
                        failed++;
                    }
                }
            }
            if (Boolean.TRUE.equals(retryScheduledByRunId.get(run.runId()))) {
                retried++;
            }
            inputTokens += Math.max(0L, run.inputTokens());
            outputTokens += Math.max(0L, run.outputTokens());
            totalTokens += Math.max(0L, run.totalTokens());
            if (run.startedAt() != null) {
                earliestStartedAt = earliestStartedAt == null || run.startedAt().isBefore(earliestStartedAt)
                        ? run.startedAt()
                        : earliestStartedAt;
            }
            if (run.updatedAt() != null) {
                latestUpdatedAt = latestUpdatedAt == null || run.updatedAt().isAfter(latestUpdatedAt)
                        ? run.updatedAt()
                        : latestUpdatedAt;
            }
            if (run.startedAt() != null
                    && run.finishedAt() != null
                    && !run.finishedAt().isBefore(run.startedAt())) {
                durationMillis += java.time.Duration.between(run.startedAt(), run.finishedAt())
                        .toMillis();
                durationCount++;
            }
        }
        double averageDurationSeconds = durationCount == 0 ? 0.0 : durationMillis / 1000.0 / durationCount;
        return new RunHistoryMetrics(
                limit,
                runs.size(),
                completed,
                failed,
                terminated,
                interrupted,
                running,
                retried,
                inputTokens,
                outputTokens,
                totalTokens,
                averageDurationSeconds,
                earliestStartedAt,
                latestUpdatedAt,
                Map.copyOf(statusCounts),
                Map.copyOf(failureCategoryCounts));
    }

    private static String normalizeStatus(String status) {
        String normalized = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? "unknown" : normalized;
    }

    private static String instantString(Instant instant) {
        return (instant != null ? instant : Instant.now()).toString();
    }

    private static Instant parseInstant(String value) {
        Instant parsed = parseNullableInstant(value);
        return parsed != null ? parsed : Instant.EPOCH;
    }

    private static Instant parseNullableInstant(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Instant.parse(value);
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    private static String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String truncate(String value, int maxChars) {
        if (value == null || value.length() <= maxChars) {
            return value;
        }
        return value.substring(0, maxChars) + "...";
    }

    @FunctionalInterface
    private interface SqlRunnable {
        void run() throws Exception;
    }
}
