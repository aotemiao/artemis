package com.aotemiao.artemis.symphony.api;

import com.aotemiao.artemis.symphony.core.model.CodexTotals;
import com.aotemiao.artemis.symphony.core.model.RetryEntry;
import com.aotemiao.artemis.symphony.orchestrator.Orchestrator;
import com.aotemiao.artemis.symphony.orchestrator.RunningEntry;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Optional HTTP API for observability. SPEC Section 13.7.
 */
@RestController
@RequestMapping("/api/v1")
public class SymphonyStateController {

    private final Orchestrator orchestrator;

    public SymphonyStateController(Orchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @GetMapping("/state")
    public ResponseEntity<Map<String, Object>> getState() {
        List<Map<String, Object>> runningList = new ArrayList<>();
        for (RunningEntry e : orchestrator.getRunning().values()) {
            runningList.add(Map.of(
                    "issue_id", e.issueId,
                    "issue_identifier", e.identifier,
                    "state", e.issue != null && e.issue.state() != null ? e.issue.state() : "",
                    "session_id", e.sessionId != null ? e.sessionId : "",
                    "turn_count", e.turnCount,
                    "last_event", e.lastCodexEvent != null ? e.lastCodexEvent : "",
                    "last_message", e.lastCodexMessage != null ? e.lastCodexMessage : "",
                    "started_at", e.startedAt.toString(),
                    "last_event_at", e.lastCodexTimestamp != null ? e.lastCodexTimestamp.toString() : "",
                    "tokens", Map.of(
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
                "generated_at", Instant.now().toString(),
                "counts", Map.of("running", runningList.size(), "retrying", retryingList.size()),
                "running", runningList,
                "retrying", retryingList,
                "codex_totals", codexTotals,
                "rate_limits", (Object) null);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh() {
        return ResponseEntity.accepted().body(Map.of(
                "queued", true,
                "coalesced", false,
                "requested_at", Instant.now().toString(),
                "operations", List.of("poll", "reconcile")));
    }
}
