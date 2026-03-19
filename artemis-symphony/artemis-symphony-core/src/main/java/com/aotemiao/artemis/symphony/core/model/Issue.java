package com.aotemiao.artemis.symphony.core.model;

import java.time.Instant;
import java.util.List;

/**
 * Normalized issue record used by orchestration, prompt rendering, and observability.
 *
 * @see <a href="https://github.com/openai/symphony/blob/main/SPEC.md">Symphony SPEC</a> Section 4.1.1
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

    /** Normalized state for comparison (lowercase). */
    public String stateNormalized() {
        return state == null ? "" : state.toLowerCase();
    }
}
