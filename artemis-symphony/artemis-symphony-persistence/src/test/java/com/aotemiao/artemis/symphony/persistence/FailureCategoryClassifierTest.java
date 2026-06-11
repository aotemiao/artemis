package com.aotemiao.artemis.symphony.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FailureCategoryClassifierTest {

    @Test
    void classify_mapsCommonSymphonyFailuresToStableCategories() {
        assertEquals("none", FailureCategoryClassifier.classify("completed", ""));
        assertEquals(
                "permission",
                FailureCategoryClassifier.classify("failed", "permission preflight failed: writable root outside"));
        assertEquals("workspace_hook", FailureCategoryClassifier.classify("failed", "before_run hook failed"));
        assertEquals("codex_startup", FailureCategoryClassifier.classify("failed", "synthetic initialize failure"));
        assertEquals("codex_timeout", FailureCategoryClassifier.classify("failed", "codex turn response timeout"));
        assertEquals("approval_required", FailureCategoryClassifier.classify("failed", "approval request rejected"));
        assertEquals("dynamic_tool", FailureCategoryClassifier.classify("failed", "dynamic tool failure"));
        assertEquals("user_input_required", FailureCategoryClassifier.classify("failed", "user input required"));
        assertEquals("codex_runtime", FailureCategoryClassifier.classify("failed", "codex turn failed"));
        assertEquals("interrupted", FailureCategoryClassifier.classify("interrupted", "startup recovery"));
        assertEquals("unknown_failure", FailureCategoryClassifier.classify("failed", ""));
    }
}
