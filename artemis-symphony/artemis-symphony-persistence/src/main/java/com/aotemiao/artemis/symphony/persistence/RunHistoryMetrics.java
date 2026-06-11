package com.aotemiao.artemis.symphony.persistence;

import java.time.Instant;
import java.util.Map;

public record RunHistoryMetrics(
        int limit,
        int totalRuns,
        int completedRuns,
        int failedRuns,
        int terminatedRuns,
        int interruptedRuns,
        int runningRuns,
        int retriedRuns,
        long inputTokens,
        long outputTokens,
        long totalTokens,
        double averageDurationSeconds,
        Instant earliestStartedAt,
        Instant latestUpdatedAt,
        Map<String, Integer> statusCounts,
        Map<String, Integer> failureCategoryCounts) {

    public RunHistoryMetrics {
        statusCounts = Map.copyOf(statusCounts);
        failureCategoryCounts = Map.copyOf(failureCategoryCounts);
    }

    @Override
    public Map<String, Integer> statusCounts() {
        return Map.copyOf(statusCounts);
    }

    @Override
    public Map<String, Integer> failureCategoryCounts() {
        return Map.copyOf(failureCategoryCounts);
    }
}
