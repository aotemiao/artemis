package com.aotemiao.artemis.symphony.core.model;

/**
 * 议题阻塞关系引用（{@code blocked_by} 列表元素）。
 *
 * @see <a href="https://github.com/openai/symphony/blob/main/SPEC.md">Symphony SPEC</a>
 */
public record BlockerRef(String id, String identifier, String state) {}
