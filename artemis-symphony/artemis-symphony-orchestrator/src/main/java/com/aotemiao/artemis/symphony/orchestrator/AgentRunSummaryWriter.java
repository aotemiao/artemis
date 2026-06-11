package com.aotemiao.artemis.symphony.orchestrator;

import com.aotemiao.artemis.symphony.config.ServiceConfig;
import com.aotemiao.artemis.symphony.core.WorkspaceKeys;
import com.aotemiao.artemis.symphony.core.model.RetryEntry;
import com.aotemiao.artemis.symphony.persistence.FailureCategoryClassifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 输出低敏 agent run 摘要，完整 prompt、聊天记录和外部响应全文不进入该文件。 */
final class AgentRunSummaryWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentRunSummaryWriter.class);
    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static final int MAX_WORKSPACE_ARTIFACT_FILES = 200;

    private AgentRunSummaryWriter() {}

    static void write(
            ServiceConfig config,
            RunningEntry entry,
            String status,
            String failureReason,
            Instant finishedAt,
            RetryEntry retryEntry,
            Path workspacePath) {
        if (config == null || entry == null || !config.isAgentRunSummaryEnabled()) {
            return;
        }
        Instant resolvedFinishedAt = finishedAt != null ? finishedAt : Instant.now();
        try {
            Path directory = config.getAgentRunSummaryDirectory();
            Files.createDirectories(directory);
            Path file = directory.resolve(fileName(entry, resolvedFinishedAt));
            MAPPER.writeValue(
                    file.toFile(),
                    body(config, entry, status, failureReason, resolvedFinishedAt, retryEntry, workspacePath));
            LOGGER.info(
                    "action=agent_run_summary_write outcome=written run_id={} path={}",
                    entry.runId,
                    file.toAbsolutePath().normalize());
        } catch (Exception e) {
            LOGGER.warn("action=agent_run_summary_write outcome=failed run_id={} reason={}", entry.runId, e.toString());
        }
    }

    private static Map<String, Object> body(
            ServiceConfig config,
            RunningEntry entry,
            String status,
            String failureReason,
            Instant finishedAt,
            RetryEntry retryEntry,
            Path workspacePath) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("schema_version", 1);
        body.put("summary_type", "symphony_agent_run");
        body.put("run_id", entry.runId);
        body.put("status", blankToDefault(status, "unknown"));
        body.put("failure_reason", failureReason != null ? failureReason : "");
        body.put("failure_category", FailureCategoryClassifier.classify(status, failureReason));
        body.put("started_at", entry.startedAt != null ? entry.startedAt.toString() : "");
        body.put("finished_at", finishedAt.toString());
        body.put("duration_seconds", durationSeconds(entry.startedAt, finishedAt));
        body.put("issue", issueBody(entry));
        body.put("attempt", attemptBody(entry));
        body.put("workspace", workspaceBody(entry, workspacePath));
        body.put("codex", codexBody(entry));
        body.put("permissions", permissionsBody(config, entry, workspacePath));
        body.put("environment", environmentBody());
        body.put("retry", retryBody(retryEntry));
        body.put("external_effects", externalEffectsBody(entry));
        return body;
    }

    private static Map<String, Object> issueBody(RunningEntry entry) {
        Map<String, Object> issue = new LinkedHashMap<>();
        issue.put("id", entry.issueId != null ? entry.issueId : "");
        issue.put("identifier", entry.identifier != null ? entry.identifier : "");
        issue.put("title", entry.issue != null && entry.issue.title() != null ? entry.issue.title() : "");
        issue.put("state", entry.issue != null && entry.issue.state() != null ? entry.issue.state() : "");
        issue.put("url", entry.issue != null && entry.issue.url() != null ? entry.issue.url() : "");
        return issue;
    }

    private static Map<String, Object> attemptBody(RunningEntry entry) {
        Map<String, Object> attempt = new LinkedHashMap<>();
        attempt.put("number", entry.retryAttempt + 1);
        attempt.put("retry_attempt", entry.retryAttempt);
        attempt.put("dispatch_kind", entry.dispatchKind != null ? entry.dispatchKind : "");
        attempt.put("parent_run_id", entry.parentRunId != null ? entry.parentRunId : "");
        attempt.put("turn_count", entry.turnCount);
        return attempt;
    }

    private static Map<String, Object> workspaceBody(RunningEntry entry, Path workspacePath) {
        Map<String, Object> workspace = new LinkedHashMap<>();
        String workspaceKey = WorkspaceKeys.sanitize(entry.identifier);
        workspace.put("path", workspacePathReference(workspaceKey));
        workspace.put("key", workspaceKey);
        workspace.put("worker_host", entry.workerHost != null ? entry.workerHost : "");
        workspace.put("artifact_inventory", workspaceArtifactInventory(entry, workspacePath));
        return workspace;
    }

    private static String workspacePathReference(String workspaceKey) {
        String key = workspaceKey != null && !workspaceKey.isBlank() ? workspaceKey : "unknown";
        return "workspace/" + key;
    }

    private static Map<String, Object> workspaceArtifactInventory(RunningEntry entry, Path workspacePath) {
        Map<String, Object> inventory = new LinkedHashMap<>();
        inventory.put("max_files", MAX_WORKSPACE_ARTIFACT_FILES);
        inventory.put("file_count", 0);
        inventory.put("total_bytes", 0L);
        inventory.put("truncated", false);
        inventory.put("scan_error", "");
        inventory.put("files", List.of());
        if (entry != null && entry.workerHost != null && !entry.workerHost.isBlank()) {
            inventory.put("scan_error", "remote_workspace_not_scanned");
            return inventory;
        }
        if (workspacePath == null) {
            inventory.put("scan_error", "workspace_path_missing");
            return inventory;
        }
        Path root = workspacePath.toAbsolutePath().normalize();
        if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) {
            inventory.put("scan_error", "workspace_not_found");
            return inventory;
        }
        try (var stream = Files.walk(root)) {
            List<Path> files = stream.filter(path -> !root.equals(path))
                    .filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                    .sorted(Comparator.comparing(path -> relativeWorkspacePath(root, path)))
                    .limit(MAX_WORKSPACE_ARTIFACT_FILES + 1L)
                    .toList();
            boolean truncated = files.size() > MAX_WORKSPACE_ARTIFACT_FILES;
            List<Map<String, Object>> entries = new ArrayList<>();
            long totalBytes = 0L;
            int limit = Math.min(files.size(), MAX_WORKSPACE_ARTIFACT_FILES);
            for (int index = 0; index < limit; index++) {
                Path file = files.get(index);
                long size = fileSize(file);
                totalBytes += size;
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("path", relativeWorkspacePath(root, file));
                item.put("size_bytes", size);
                entries.add(item);
            }
            inventory.put("file_count", entries.size());
            inventory.put("total_bytes", totalBytes);
            inventory.put("truncated", truncated);
            inventory.put("files", entries);
        } catch (IOException e) {
            inventory.put("scan_error", "workspace_scan_failed");
        }
        return inventory;
    }

    private static String relativeWorkspacePath(Path root, Path path) {
        return root.relativize(path.toAbsolutePath().normalize()).toString().replace('\\', '/');
    }

    private static long fileSize(Path file) {
        try {
            return Files.size(file);
        } catch (IOException e) {
            return 0L;
        }
    }

    private static Map<String, Object> codexBody(RunningEntry entry) {
        Map<String, Object> codex = new LinkedHashMap<>();
        codex.put("session_id", entry.sessionId != null ? entry.sessionId : "");
        codex.put("app_server_pid", entry.codexAppServerPid != null ? entry.codexAppServerPid : "");
        codex.put("last_event", entry.lastCodexEvent != null ? entry.lastCodexEvent : "");
        codex.put("last_event_at", entry.lastCodexTimestamp != null ? entry.lastCodexTimestamp.toString() : "");
        codex.put("event_counts", new TreeMap<>(entry.codexEventCounts()));
        codex.put(
                "usage",
                Map.of(
                        "input_tokens",
                        entry.codexInputTokens,
                        "output_tokens",
                        entry.codexOutputTokens,
                        "total_tokens",
                        entry.codexTotalTokens));
        return codex;
    }

    private static Map<String, Object> permissionsBody(ServiceConfig config, RunningEntry entry, Path workspacePath) {
        Map<String, Object> permissions = new LinkedHashMap<>();
        Path resolvedWorkspacePath = workspacePath != null ? workspacePath : stringPath(entry.workspacePath);
        Object turnSandboxPolicy = isAdversarialReview(entry)
                ? config.resolveAdversarialReviewTurnSandboxPolicy(resolvedWorkspacePath, entry.workerHost != null)
                : config.resolveCodexTurnSandboxPolicy(resolvedWorkspacePath, entry.workerHost != null);
        String workspaceKey = WorkspaceKeys.sanitize(entry.identifier);
        permissions.put("approval_policy", config.getCodexApprovalPolicy());
        permissions.put("thread_sandbox", config.getEffectiveCodexThreadSandbox());
        permissions.put("turn_sandbox_policy", summarizePolicy(turnSandboxPolicy, workspaceKey));
        permissions.put("remote_worker", entry.workerHost != null && !entry.workerHost.isBlank());
        permissions.put("network_access", networkAccess(turnSandboxPolicy));
        permissions.put(
                "network_access_reason",
                config.getNetworkAccessReason() != null ? config.getNetworkAccessReason() : "");
        permissions.put("writable_roots", summarizePathReferences(writableRoots(turnSandboxPolicy), workspaceKey));
        permissions.put(
                "allowed_writable_roots",
                summarizePathReferences(config.getAllowedWritableRoots(), "configured-writable-root"));
        permissions.put("danger_full_access_allowed", config.isDangerFullAccessAllowed());
        return permissions;
    }

    private static boolean isAdversarialReview(RunningEntry entry) {
        return entry != null && "adversarial_review".equals(entry.dispatchKind);
    }

    private static Map<String, Object> environmentBody() {
        Map<String, Object> environment = new LinkedHashMap<>();
        environment.put("java", javaEnvironmentBody());
        environment.put("maven", Map.of("version", systemProperty("maven.version")));
        environment.put("os", osEnvironmentBody());
        environment.put(
                "process", Map.of("available_processors", Runtime.getRuntime().availableProcessors()));
        environment.put("spring_profiles", springProfiles());
        return environment;
    }

    private static Map<String, Object> javaEnvironmentBody() {
        Map<String, Object> java = new LinkedHashMap<>();
        java.put("version", systemProperty("java.version"));
        java.put("vendor", systemProperty("java.vendor"));
        java.put("runtime_name", systemProperty("java.runtime.name"));
        java.put("runtime_version", systemProperty("java.runtime.version"));
        java.put("vm_name", systemProperty("java.vm.name"));
        java.put("vm_version", systemProperty("java.vm.version"));
        return java;
    }

    private static Map<String, Object> osEnvironmentBody() {
        Map<String, Object> os = new LinkedHashMap<>();
        os.put("name", systemProperty("os.name"));
        os.put("arch", systemProperty("os.arch"));
        os.put("version", systemProperty("os.version"));
        return os;
    }

    private static List<String> springProfiles() {
        String profiles = firstNonBlank(systemProperty("spring.profiles.active"), env("SPRING_PROFILES_ACTIVE"));
        if (profiles.isBlank()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String raw : profiles.split(",")) {
            String profile = raw.trim();
            if (!profile.isEmpty()) {
                result.add(profile);
            }
        }
        return List.copyOf(result);
    }

    private static Map<String, Object> retryBody(RetryEntry retryEntry) {
        Map<String, Object> retry = new LinkedHashMap<>();
        boolean retryScheduled = isFailureRetry(retryEntry);
        retry.put("scheduled", retryScheduled);
        retry.put("dispatch_kind", retryEntry != null ? retryEntry.kind() : "");
        retry.put("next_attempt", retryScheduled ? retryEntry.attempt() + 1 : 0);
        retry.put(
                "due_at",
                retryEntry != null ? Instant.ofEpochMilli(retryEntry.dueAtMs()).toString() : "");
        retry.put("error", retryEntry != null && retryEntry.error() != null ? retryEntry.error() : "");
        return retry;
    }

    private static boolean isFailureRetry(RetryEntry retryEntry) {
        return retryEntry != null && "retry".equals(retryEntry.kind());
    }

    private static Map<String, Object> externalEffectsBody(RunningEntry entry) {
        Map<String, Object> effects = new LinkedHashMap<>();
        effects.put("tracker_state_claimed", entry.trackerStateClaimed);
        effects.put("linear_comment_attempted", entry.linearCommentAttempted);
        effects.put("events", externalEffectEvents(entry));
        return effects;
    }

    private static List<Map<String, Object>> externalEffectEvents(RunningEntry entry) {
        List<Map<String, Object>> events = new ArrayList<>();
        for (RunningEntry.ExternalEffect effect : entry.externalEffects()) {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("type", nullToEmpty(effect.type()));
            event.put("provider", nullToEmpty(effect.provider()));
            event.put("target", nullToEmpty(effect.target()));
            event.put("status", nullToEmpty(effect.status()));
            event.put("error_code", nullToEmpty(effect.errorCode()));
            event.put("error_message", nullToEmpty(effect.errorMessage()));
            event.put("at", effect.at() != null ? effect.at().toString() : "");
            events.add(event);
        }
        return events;
    }

    private static Path stringPath(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Path.of(value);
    }

    private static Object networkAccess(Object policy) {
        if (policy instanceof Map<?, ?> map && map.containsKey("networkAccess")) {
            Object value = map.get("networkAccess");
            return value != null ? value : false;
        }
        return "";
    }

    private static Object writableRoots(Object policy) {
        if (policy instanceof Map<?, ?> map) {
            Object value = map.get("writableRoots");
            return value != null ? value : java.util.List.of();
        }
        return java.util.List.of();
    }

    private static Object summarizePolicy(Object value, String workspaceKey) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> summarized = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = entry.getKey() != null ? entry.getKey().toString() : "";
                if ("writableRoots".equals(key)) {
                    summarized.put(key, summarizePathReferences(entry.getValue(), workspaceKey));
                } else {
                    summarized.put(key, summarizePolicy(entry.getValue(), workspaceKey));
                }
            }
            return summarized;
        }
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(item -> summarizePolicy(item, workspaceKey))
                    .toList();
        }
        return value != null ? value : "";
    }

    private static List<String> summarizePathReferences(Object value, String prefix) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<String> summarized = new ArrayList<>();
        int index = 1;
        for (Object item : list) {
            String text = item != null ? item.toString().trim() : "";
            if (text.isEmpty()) {
                continue;
            }
            summarized.add(pathReference(prefix, text, index));
            index++;
        }
        return List.copyOf(summarized);
    }

    private static String pathReference(String prefix, String rawPath, int index) {
        String safePrefix = WorkspaceKeys.sanitize(prefix != null && !prefix.isBlank() ? prefix : "path");
        String marker = WorkspaceKeys.sanitize(pathMarker(rawPath));
        return safePrefix + "/" + index + "-" + marker;
    }

    private static String pathMarker(String rawPath) {
        try {
            Path fileName = Path.of(rawPath).getFileName();
            return fileName != null ? fileName.toString() : "path";
        } catch (RuntimeException e) {
            return "path";
        }
    }

    private static long durationSeconds(Instant startedAt, Instant finishedAt) {
        if (startedAt == null || finishedAt == null) {
            return 0L;
        }
        return Math.max(0L, Duration.between(startedAt, finishedAt).getSeconds());
    }

    private static String fileName(RunningEntry entry, Instant finishedAt) {
        String timestamp = finishedAt.toString().replace(':', '-');
        String identifier = WorkspaceKeys.sanitize(entry.identifier != null ? entry.identifier : "issue");
        String runId = WorkspaceKeys.sanitize(entry.runId != null ? entry.runId : "run");
        return timestamp + "-" + identifier + "-" + runId + ".json";
    }

    private static String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second != null ? second : "";
    }

    private static String systemProperty(String key) {
        try {
            return System.getProperty(key, "");
        } catch (SecurityException e) {
            return "";
        }
    }

    private static String env(String key) {
        try {
            return System.getenv(key);
        } catch (SecurityException e) {
            return "";
        }
    }
}
