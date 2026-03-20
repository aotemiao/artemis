package com.aotemiao.artemis.symphony.core.model;

/**
 * 编排器状态中的 Token 与运行时累计量。
 *
 * @see <a href="https://github.com/openai/symphony/blob/main/SPEC.md">Symphony SPEC</a> 第 4.1.8 节
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
