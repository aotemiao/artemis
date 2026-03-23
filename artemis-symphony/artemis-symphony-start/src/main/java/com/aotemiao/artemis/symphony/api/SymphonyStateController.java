package com.aotemiao.artemis.symphony.api;

import com.aotemiao.artemis.symphony.core.WorkspaceKeys;
import com.aotemiao.artemis.symphony.core.model.CodexTotals;
import com.aotemiao.artemis.symphony.core.model.RetryEntry;
import com.aotemiao.artemis.symphony.orchestrator.Orchestrator;
import com.aotemiao.artemis.symphony.orchestrator.RunningEntry;
import com.aotemiao.artemis.symphony.workspace.WorkspaceManager;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 可选的 HTTP 可观测性接口。见 SPEC 第 13.7 节。 */
@RestController
@RequestMapping("/api/v1")
public class SymphonyStateController {

    private final Orchestrator orchestrator;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "WorkspaceManager is a Spring-managed shared collaborator and is not mutated here.")
    private final WorkspaceManager workspaceManager;

    public SymphonyStateController(Orchestrator orchestrator, WorkspaceManager workspaceManager) {
        this.orchestrator = orchestrator;
        this.workspaceManager = workspaceManager;
    }

    @GetMapping("/state")
    public ResponseEntity<Map<String, Object>> getState() {
        List<Map<String, Object>> runningList = new ArrayList<>();
        for (RunningEntry e : orchestrator.getRunning().values()) {
            runningList.add(Map.of(
                    "issue_id",
                    e.issueId,
                    "issue_identifier",
                    e.identifier,
                    "state",
                    e.issue != null && e.issue.state() != null ? e.issue.state() : "",
                    "session_id",
                    e.sessionId != null ? e.sessionId : "",
                    "turn_count",
                    e.turnCount,
                    "last_event",
                    e.lastCodexEvent != null ? e.lastCodexEvent : "",
                    "last_message",
                    e.lastCodexMessage != null ? e.lastCodexMessage : "",
                    "started_at",
                    e.startedAt.toString(),
                    "last_event_at",
                    e.lastCodexTimestamp != null ? e.lastCodexTimestamp.toString() : "",
                    "tokens",
                    Map.of(
                            "input_tokens", e.codexInputTokens,
                            "output_tokens", e.codexOutputTokens,
                            "total_tokens", e.codexTotalTokens)));
        }
        List<Map<String, Object>> retryingList = new ArrayList<>();
        for (RetryEntry re : orchestrator.getRetryAttempts().values()) {
            retryingList.add(Map.of(
                    "issue_id", re.issueId(),
                    "issue_identifier", re.identifier(),
                    "attempt", re.attempt(),
                    "due_at", Instant.ofEpochMilli(re.dueAtMs()).toString(),
                    "error", re.error() != null ? re.error() : ""));
        }
        CodexTotals totals = orchestrator.getCodexTotals();
        Map<String, Object> codexTotals = Map.of(
                "input_tokens", totals.inputTokens(),
                "output_tokens", totals.outputTokens(),
                "total_tokens", totals.totalTokens(),
                "seconds_running", totals.secondsRunning());
        Map<String, Object> body = Map.of(
                "generated_at",
                Instant.now().toString(),
                "counts",
                Map.of("running", runningList.size(), "retrying", retryingList.size()),
                "running",
                runningList,
                "retrying",
                retryingList,
                "codex_totals",
                codexTotals,
                "rate_limits",
                (Object) null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh() {
        boolean coalesced = orchestrator.requestImmediateTick();
        return ResponseEntity.accepted()
                .body(Map.of(
                        "queued",
                        true,
                        "coalesced",
                        coalesced,
                        "requested_at",
                        Instant.now().toString(),
                        "operations",
                        List.of("poll", "reconcile")));
    }

    @GetMapping("/issues/{identifier}")
    public ResponseEntity<Map<String, Object>> getIssue(@PathVariable("identifier") String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return errorResponse(HttpStatus.BAD_REQUEST, "invalid_identifier", "必须提供 identifier");
        }
        RunningEntry running = orchestrator.findRunningByIdentifier(identifier);
        RetryEntry retry = running != null ? null : orchestrator.findRetryByIdentifier(identifier);
        if (running == null && retry == null) {
            return errorResponse(HttpStatus.NOT_FOUND, "not_found", "未找到运行中或重试中的议题，identifier=" + identifier);
        }

        String issueId = running != null ? running.issueId : retry.issueId();
        Path workspacePath = workspacePathFor(identifier);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("issue_id", issueId);
        body.put("issue_identifier", identifier);
        body.put("workspace_path", workspacePath.toString());
        body.put("recent_events", List.of());

        if (running != null) {
            body.put("phase", "running");
            body.put(
                    "tracker_state",
                    running.issue != null && running.issue.state() != null ? running.issue.state() : "");
            body.put("session_id", running.sessionId != null ? running.sessionId : "");
            body.put("started_at", running.startedAt.toString());
            body.put(
                    "last_codex",
                    Map.of(
                            "event", running.lastCodexEvent != null ? running.lastCodexEvent : "",
                            "at", running.lastCodexTimestamp != null ? running.lastCodexTimestamp.toString() : "",
                            "tokens",
                                    Map.of(
                                            "input", running.codexInputTokens,
                                            "output", running.codexOutputTokens,
                                            "total", running.codexTotalTokens)));
        } else {
            body.put("phase", "retrying");
            body.put(
                    "retry",
                    Map.of(
                            "attempt", retry.attempt(),
                            "due_at", Instant.ofEpochMilli(retry.dueAtMs()).toString(),
                            "error", retry.error() != null ? retry.error() : ""));
        }

        return ResponseEntity.ok(body);
    }

    private Path workspacePathFor(String issueIdentifier) {
        var root = workspaceManager.getWorkspaceRoot();
        var key = WorkspaceKeys.sanitize(issueIdentifier);
        return root.resolve(key).normalize();
    }

    private static ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
                .body(Map.of(
                        "error", code,
                        "message", message));
    }
}
