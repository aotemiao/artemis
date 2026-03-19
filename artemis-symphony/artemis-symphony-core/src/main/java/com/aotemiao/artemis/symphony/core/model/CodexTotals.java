package com.aotemiao.artemis.symphony.core.model;

/**
 * Aggregate token and runtime totals in orchestrator state.
 *
 * @see <a href="https://github.com/openai/symphony/blob/main/SPEC.md">Symphony SPEC</a> Section 4.1.8
 */
public record CodexTotals(
        long inputTokens,
        long outputTokens,
        long totalTokens,
        double secondsRunning) {

    public static CodexTotals zero() {
        return new CodexTotals(0, 0, 0, 0.0);
    }
}
