package com.aotemiao.artemis.symphony.core.model;

import java.time.Instant;
import java.util.List;

/**
 * 归一化后的议题记录，供编排、模板渲染与可观测性使用。
 *
 * @see <a href="https://github.com/openai/symphony/blob/main/SPEC.md">Symphony SPEC</a> 第 4.1.1 节
 */
public record Issue(
        String id,
        String identifier,
        String title,
        String description,
        Integer priority,
        String state,
        String branchName,
        String url,
        List<String> labels,
        List<BlockerRef> blockedBy,
        Instant createdAt,
        Instant updatedAt) {

    /** 用于比较的状态归一化形式（小写）。 */
    public String stateNormalized() {
        return state == null ? "" : state.toLowerCase();
    }
}
