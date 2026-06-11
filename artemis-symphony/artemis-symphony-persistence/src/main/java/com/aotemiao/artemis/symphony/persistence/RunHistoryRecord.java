package com.aotemiao.artemis.symphony.persistence;

import java.time.Instant;

public record RunHistoryRecord(
        String runId,
        String issueId,
        String issueIdentifier,
        String issueTitle,
        String trackerState,
        String status,
        int attempt,
        String workerHost,
        String workspacePath,
        String threadId,
        String sessionId,
        String codexAppServerPid,
        long inputTokens,
        long outputTokens,
        long totalTokens,
        String failureReason,
        Instant startedAt,
        Instant updatedAt,
        Instant finishedAt) {}
