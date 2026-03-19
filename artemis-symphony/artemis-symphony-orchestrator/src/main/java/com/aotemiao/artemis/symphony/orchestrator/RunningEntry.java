package com.aotemiao.artemis.symphony.orchestrator;

import com.aotemiao.artemis.symphony.core.model.Issue;

import java.time.Instant;

/**
 * One running issue in orchestrator state. SPEC Section 4.1.6 / 16.4.
 */
public class RunningEntry {

    public final String issueId;
    public final String identifier;
    public Issue issue;
    public final int retryAttempt;
    public final Instant startedAt;

    public String sessionId;
    public String codexAppServerPid;
    public String lastCodexEvent;
    public Instant lastCodexTimestamp;
    public String lastCodexMessage;
    public long codexInputTokens;
    public long codexOutputTokens;
    public long codexTotalTokens;
    public long lastReportedInputTokens;
    public long lastReportedOutputTokens;
    public long lastReportedTotalTokens;
    public int turnCount;

    public RunningEntry(String issueId, String identifier, Issue issue, int retryAttempt, Instant startedAt) {
        this.issueId = issueId;
        this.identifier = identifier;
        this.issue = issue;
        this.retryAttempt = retryAttempt;
        this.startedAt = startedAt;
    }
}
