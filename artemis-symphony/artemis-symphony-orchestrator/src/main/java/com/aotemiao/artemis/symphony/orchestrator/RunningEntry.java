package com.aotemiao.artemis.symphony.orchestrator;

import com.aotemiao.artemis.symphony.core.model.Issue;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 编排器状态中的一条「运行中」议题。见 SPEC 第 4.1.6 / 16.4 节。 */
@SuppressFBWarnings(
        value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD",
        justification = "These snapshot fields are consumed by the state API module outside this Maven module.")
public class RunningEntry {

    public final String issueId;
    public final String identifier;
    public final String runId;
    public Issue issue;
    public final int retryAttempt;
    public final Instant startedAt;

    public String sessionId;
    public String dispatchKind = "implementation";
    public String parentRunId;
    public String workerHost;
    public String workspacePath;
    public String codexAppServerPid;
    public String lastCodexEvent;
    public Instant lastCodexTimestamp;
    public String lastCodexMessage;
    public long codexInputTokens;
    public long codexOutputTokens;
    public long codexTotalTokens;
    public int turnCount;
    public boolean trackerStateClaimed;
    public boolean linearCommentAttempted;
    private final Map<String, Integer> codexEventCounts = new LinkedHashMap<>();
    private final List<ExternalEffect> externalEffects = new ArrayList<>();

    public RunningEntry(String issueId, String identifier, Issue issue, int retryAttempt, Instant startedAt) {
        this(issueId, identifier, "", issue, retryAttempt, startedAt);
    }

    public RunningEntry(
            String issueId, String identifier, String runId, Issue issue, int retryAttempt, Instant startedAt) {
        this.issueId = issueId;
        this.identifier = identifier;
        this.runId = runId != null ? runId : "";
        this.issue = issue;
        this.retryAttempt = retryAttempt;
        this.startedAt = startedAt;
    }

    public void recordExternalEffect(
            String type,
            String provider,
            String target,
            String status,
            String errorCode,
            String errorMessage,
            Instant at) {
        externalEffects.add(new ExternalEffect(type, provider, target, status, errorCode, errorMessage, at));
    }

    public synchronized void recordCodexEvent(String eventType) {
        String normalized = eventType != null && !eventType.isBlank() ? eventType : "unknown";
        codexEventCounts.merge(normalized, 1, Integer::sum);
    }

    public synchronized Map<String, Integer> codexEventCounts() {
        return new LinkedHashMap<>(codexEventCounts);
    }

    public List<ExternalEffect> externalEffects() {
        return List.copyOf(externalEffects);
    }

    public record ExternalEffect(
            String type,
            String provider,
            String target,
            String status,
            String errorCode,
            String errorMessage,
            Instant at) {}
}
