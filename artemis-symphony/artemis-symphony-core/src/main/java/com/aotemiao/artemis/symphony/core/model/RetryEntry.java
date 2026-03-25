package com.aotemiao.artemis.symphony.core.model;

/**
 * 某议题的定时重试状态。
 *
 * @see <a href="https://github.com/openai/symphony/blob/main/SPEC.md">Symphony SPEC</a> 第 4.1.7 节
 */
public record RetryEntry(
        String issueId,
        String identifier,
        int attempt,
        long dueAtMs,
        Object timerHandle,
        String error,
        String workerHost,
        String workspacePath) {

    public RetryEntry(String issueId, String identifier, int attempt, long dueAtMs, Object timerHandle, String error) {
        this(issueId, identifier, attempt, dueAtMs, timerHandle, error, null, null);
    }
}
