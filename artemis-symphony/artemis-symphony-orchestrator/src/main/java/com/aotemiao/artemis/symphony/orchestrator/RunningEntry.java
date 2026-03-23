package com.aotemiao.artemis.symphony.orchestrator;

import com.aotemiao.artemis.symphony.core.model.Issue;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Instant;

/** 编排器状态中的一条「运行中」议题。见 SPEC 第 4.1.6 / 16.4 节。 */
@SuppressFBWarnings(
        value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD",
        justification = "These snapshot fields are consumed by the state API module outside this Maven module.")
public class RunningEntry {

    public final String issueId;
    public final String identifier;
    public Issue issue;
    public final int retryAttempt;
    public final Instant startedAt;

    public String sessionId;
    public String lastCodexEvent;
    public Instant lastCodexTimestamp;
    public String lastCodexMessage;
    public long codexInputTokens;
    public long codexOutputTokens;
    public long codexTotalTokens;
    public int turnCount;

    public RunningEntry(String issueId, String identifier, Issue issue, int retryAttempt, Instant startedAt) {
        this.issueId = issueId;
        this.identifier = identifier;
        this.issue = issue;
        this.retryAttempt = retryAttempt;
        this.startedAt = startedAt;
    }
}
