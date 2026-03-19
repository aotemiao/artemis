package com.aotemiao.artemis.symphony.core.model;

/**
 * Blocker reference for an issue (blocked_by list item).
 *
 * @see <a href="https://github.com/openai/symphony/blob/main/SPEC.md">Symphony SPEC</a>
 */
public record BlockerRef(String id, String identifier, String state) {}
