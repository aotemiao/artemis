package com.aotemiao.artemis.symphony.core.model;

/**
 * Scheduled retry state for an issue.
 *
 * @see <a href="https://github.com/openai/symphony/blob/main/SPEC.md">Symphony SPEC</a> Section 4.1.7
 */
public record RetryEntry(
        String issueId,
        String identifier,
        int attempt,
        long dueAtMs,
        Object timerHandle,
        String error) {}
