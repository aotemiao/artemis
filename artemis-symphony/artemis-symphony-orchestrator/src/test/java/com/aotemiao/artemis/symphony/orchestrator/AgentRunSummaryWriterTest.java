package com.aotemiao.artemis.symphony.orchestrator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aotemiao.artemis.symphony.config.ServiceConfig;
import com.aotemiao.artemis.symphony.core.model.Issue;
import com.aotemiao.artemis.symphony.core.model.RetryEntry;
import com.aotemiao.artemis.symphony.core.model.WorkflowDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AgentRunSummaryWriterTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @TempDir
    Path tempDir;

    @Test
    void write_createsLowSensitivityJsonSummary() throws Exception {
        Path summaryDir = tempDir.resolve("agent-runs");
        ServiceConfig config = config(true, summaryDir);
        Issue issue = new Issue(
                "issue-1",
                "ART-1",
                "整理运行摘要",
                "敏感描述不进入摘要",
                1,
                "In Progress",
                null,
                "https://linear.app/example/ART-1",
                List.of("agent"),
                List.of(),
                Instant.parse("2026-06-09T01:00:00Z"),
                Instant.parse("2026-06-09T01:30:00Z"));
        RunningEntry entry =
                new RunningEntry("issue-1", "ART-1", "run-1", issue, 1, Instant.parse("2026-06-09T02:00:00Z"));
        entry.workspacePath = tempDir.resolve("workspace").toString();
        Files.createDirectories(Path.of(entry.workspacePath).resolve("docs"));
        Files.writeString(Path.of(entry.workspacePath).resolve("EVAL_RESULT.md"), "summary ok\n");
        Files.writeString(Path.of(entry.workspacePath).resolve("docs/report.md"), "report\n");
        entry.sessionId = "session-1";
        entry.codexAppServerPid = "4321";
        entry.lastCodexEvent = "turn_completed";
        entry.lastCodexTimestamp = Instant.parse("2026-06-09T02:01:00Z");
        entry.recordCodexEvent("session_started");
        entry.recordCodexEvent("thread/tokenUsage/updated");
        entry.recordCodexEvent("turn_completed");
        entry.recordCodexEvent("turn_completed");
        entry.turnCount = 2;
        entry.codexInputTokens = 11;
        entry.codexOutputTokens = 22;
        entry.codexTotalTokens = 33;
        entry.linearCommentAttempted = true;
        entry.recordExternalEffect(
                "linear_comment",
                "linear",
                "issue-1",
                "failed",
                "linear_error",
                "comment body rejected",
                Instant.parse("2026-06-09T02:01:30Z"));

        AgentRunSummaryWriter.write(
                config,
                entry,
                "failed",
                "codex turn failed",
                Instant.parse("2026-06-09T02:02:00Z"),
                new RetryEntry(
                        "issue-1",
                        "ART-1",
                        2,
                        Instant.parse("2026-06-09T02:03:00Z").toEpochMilli(),
                        null,
                        "x"),
                Path.of(entry.workspacePath));

        List<Path> files;
        try (var stream = Files.list(summaryDir)) {
            files = stream.toList();
        }
        assertEquals(1, files.size());
        JsonNode body = MAPPER.readTree(files.get(0).toFile());
        assertEquals("symphony_agent_run", body.path("summary_type").asText());
        assertEquals("run-1", body.path("run_id").asText());
        assertEquals("failed", body.path("status").asText());
        assertEquals("codex_runtime", body.path("failure_category").asText());
        assertEquals("ART-1", body.path("issue").path("identifier").asText());
        assertEquals(2, body.path("attempt").path("number").asInt());
        assertEquals(
                "implementation", body.path("attempt").path("dispatch_kind").asText());
        assertEquals("", body.path("attempt").path("parent_run_id").asText());
        JsonNode eventCounts = body.path("codex").path("event_counts");
        assertEquals(1, eventCounts.path("session_started").asInt());
        assertEquals(1, eventCounts.path("thread/tokenUsage/updated").asInt());
        assertEquals(2, eventCounts.path("turn_completed").asInt());
        assertEquals(33L, body.path("codex").path("usage").path("total_tokens").asLong());
        assertEquals("workspace/ART-1", body.path("workspace").path("path").asText());
        assertEquals(
                "workspaceWrite",
                body.path("permissions")
                        .path("turn_sandbox_policy")
                        .path("type")
                        .asText());
        assertFalse(body.path("permissions").path("network_access").asBoolean());
        assertEquals(
                "ART-1/1-workspace",
                body.path("permissions").path("writable_roots").get(0).asText());
        assertEquals(
                "ART-1/1-workspace",
                body.path("permissions")
                        .path("turn_sandbox_policy")
                        .path("writableRoots")
                        .get(0)
                        .asText());
        JsonNode inventory = body.path("workspace").path("artifact_inventory");
        assertEquals(2, inventory.path("file_count").asInt());
        assertEquals(18L, inventory.path("total_bytes").asLong());
        assertFalse(inventory.path("truncated").asBoolean());
        assertEquals("", inventory.path("scan_error").asText());
        assertEquals(
                "EVAL_RESULT.md", inventory.path("files").get(0).path("path").asText());
        assertEquals(11L, inventory.path("files").get(0).path("size_bytes").asLong());
        assertEquals(
                "docs/report.md", inventory.path("files").get(1).path("path").asText());
        assertTrue(body.path("permissions").path("approval_policy").has("reject"));
        assertFalse(
                body.path("environment").path("java").path("version").asText().isBlank());
        assertFalse(
                body.path("environment").path("java").path("vendor").asText().isBlank());
        assertFalse(body.path("environment").path("os").path("name").asText().isBlank());
        assertFalse(body.path("environment").path("os").path("arch").asText().isBlank());
        assertTrue(body.path("environment")
                        .path("process")
                        .path("available_processors")
                        .asInt()
                > 0);
        assertTrue(body.path("environment").path("spring_profiles").isArray());
        assertTrue(body.path("retry").path("scheduled").asBoolean());
        assertEquals("retry", body.path("retry").path("dispatch_kind").asText());
        assertTrue(
                body.path("external_effects").path("linear_comment_attempted").asBoolean());
        JsonNode effect = body.path("external_effects").path("events").get(0);
        assertEquals("linear_comment", effect.path("type").asText());
        assertEquals("linear", effect.path("provider").asText());
        assertEquals("issue-1", effect.path("target").asText());
        assertEquals("failed", effect.path("status").asText());
        assertEquals("linear_error", effect.path("error_code").asText());
        assertEquals("comment body rejected", effect.path("error_message").asText());
        assertEquals("2026-06-09T02:01:30Z", effect.path("at").asText());
        assertFalse(body.toString().contains("敏感描述不进入摘要"));
    }

    @Test
    void write_recordsReadOnlySandboxForAdversarialReview() throws Exception {
        Path summaryDir = tempDir.resolve("review-agent-runs");
        ServiceConfig config = config(true, summaryDir);
        Issue issue = new Issue(
                "issue-review",
                "ART-REVIEW",
                "高风险 review",
                null,
                1,
                "In Progress",
                null,
                null,
                List.of("high-risk"),
                List.of(),
                Instant.parse("2026-06-09T05:00:00Z"),
                Instant.parse("2026-06-09T05:30:00Z"));
        RunningEntry entry = new RunningEntry(
                "issue-review", "ART-REVIEW", "run-review", issue, 0, Instant.parse("2026-06-09T06:00:00Z"));
        entry.dispatchKind = "adversarial_review";
        entry.parentRunId = "run-implementation";
        entry.workspacePath = tempDir.resolve("review-workspace").toString();

        AgentRunSummaryWriter.write(
                config,
                entry,
                "completed",
                null,
                Instant.parse("2026-06-09T06:01:00Z"),
                null,
                Path.of(entry.workspacePath));

        List<Path> files;
        try (var stream = Files.list(summaryDir)) {
            files = stream.toList();
        }
        JsonNode body = MAPPER.readTree(files.get(0).toFile());
        assertEquals(
                "adversarial_review", body.path("attempt").path("dispatch_kind").asText());
        assertEquals(
                "run-implementation", body.path("attempt").path("parent_run_id").asText());
        assertEquals(
                "readOnly",
                body.path("permissions")
                        .path("turn_sandbox_policy")
                        .path("type")
                        .asText());
        assertFalse(body.path("permissions").path("network_access").asBoolean());
        assertEquals(0, body.path("permissions").path("writable_roots").size());
    }

    @Test
    void write_recordsAllowedWritableRootWithLowSensitivityReferences() throws Exception {
        Path summaryDir = tempDir.resolve("allowed-root-agent-runs");
        Path workspacePath = tempDir.resolve("workspace");
        Path outsideWritableRoot = tempDir.resolve("outside-writable-root");
        ServiceConfig config = configWithWritableRoots(true, summaryDir, workspacePath, outsideWritableRoot);
        Issue issue = new Issue(
                "issue-root",
                "ART-ROOT",
                "额外写目录",
                null,
                1,
                "In Progress",
                null,
                null,
                List.of("permission"),
                List.of(),
                Instant.parse("2026-06-09T07:00:00Z"),
                Instant.parse("2026-06-09T07:30:00Z"));
        RunningEntry entry =
                new RunningEntry("issue-root", "ART-ROOT", "run-root", issue, 0, Instant.parse("2026-06-09T08:00:00Z"));
        entry.workspacePath = workspacePath.toString();

        AgentRunSummaryWriter.write(
                config, entry, "completed", null, Instant.parse("2026-06-09T08:01:00Z"), null, workspacePath);

        List<Path> files;
        try (var stream = Files.list(summaryDir)) {
            files = stream.toList();
        }
        assertEquals(1, files.size());
        JsonNode body = MAPPER.readTree(files.get(0).toFile());
        JsonNode permissions = body.path("permissions");
        assertEquals(2, permissions.path("writable_roots").size());
        assertEquals(
                "ART-ROOT/1-workspace",
                permissions.path("writable_roots").get(0).asText());
        assertEquals(
                "ART-ROOT/2-outside-writable-root",
                permissions.path("writable_roots").get(1).asText());
        assertEquals(
                "configured-writable-root/1-outside-writable-root",
                permissions.path("allowed_writable_roots").get(0).asText());
        assertEquals(
                "ART-ROOT/2-outside-writable-root",
                permissions
                        .path("turn_sandbox_policy")
                        .path("writableRoots")
                        .get(1)
                        .asText());
        assertFalse(body.toString().contains(outsideWritableRoot.toString()));
    }

    @Test
    void write_marksContinuationDispatchAsNonRetry() throws Exception {
        Path summaryDir = tempDir.resolve("continuation-agent-runs");
        ServiceConfig config = config(true, summaryDir);
        Issue issue = new Issue(
                "issue-2",
                "ART-2",
                "成功后续跑检查",
                null,
                1,
                "In Progress",
                null,
                "https://linear.app/example/ART-2",
                List.of("agent"),
                List.of(),
                Instant.parse("2026-06-09T03:00:00Z"),
                Instant.parse("2026-06-09T03:30:00Z"));
        RunningEntry entry =
                new RunningEntry("issue-2", "ART-2", "run-2", issue, 0, Instant.parse("2026-06-09T04:00:00Z"));

        AgentRunSummaryWriter.write(
                config,
                entry,
                "completed",
                null,
                Instant.parse("2026-06-09T04:01:00Z"),
                new RetryEntry(
                        "issue-2",
                        "ART-2",
                        1,
                        Instant.parse("2026-06-09T04:01:01Z").toEpochMilli(),
                        null,
                        null,
                        null,
                        null,
                        "continuation"),
                null);

        List<Path> files;
        try (var stream = Files.list(summaryDir)) {
            files = stream.toList();
        }
        JsonNode body = MAPPER.readTree(files.get(0).toFile());
        assertFalse(body.path("retry").path("scheduled").asBoolean());
        assertEquals("continuation", body.path("retry").path("dispatch_kind").asText());
        assertEquals(0, body.path("retry").path("next_attempt").asInt());
        assertEquals("2026-06-09T04:01:01Z", body.path("retry").path("due_at").asText());
    }

    @Test
    void write_skipsWhenDisabled() throws Exception {
        Path summaryDir = tempDir.resolve("disabled-agent-runs");
        RunningEntry entry = new RunningEntry("issue-1", "ART-1", "run-1", null, 0, Instant.now());

        AgentRunSummaryWriter.write(config(false, summaryDir), entry, "completed", null, Instant.now(), null, null);

        assertFalse(Files.exists(summaryDir));
    }

    private static ServiceConfig config(boolean enabled, Path summaryDir) {
        return new ServiceConfig(new WorkflowDefinition(
                Map.of(
                        "reporting",
                        Map.of("agent_runs", Map.of("enabled", enabled, "directory", summaryDir.toString()))),
                "prompt"));
    }

    private static ServiceConfig configWithWritableRoots(
            boolean enabled, Path summaryDir, Path workspacePath, Path outsideWritableRoot) {
        return new ServiceConfig(new WorkflowDefinition(
                Map.of(
                        "reporting",
                        Map.of("agent_runs", Map.of("enabled", enabled, "directory", summaryDir.toString())),
                        "codex",
                        Map.of(
                                "turn_sandbox_policy",
                                Map.of(
                                        "type",
                                        "workspaceWrite",
                                        "writableRoots",
                                        List.of(workspacePath.toString(), outsideWritableRoot.toString()),
                                        "readOnlyAccess",
                                        Map.of("type", "fullAccess"),
                                        "networkAccess",
                                        false)),
                        "permissions",
                        Map.of("allowed_writable_roots", List.of(outsideWritableRoot.toString()))),
                "prompt"));
    }
}
