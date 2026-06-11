package com.aotemiao.artemis.symphony.persistence;

import java.util.Locale;

/** 将低敏失败原因归一为稳定指标维度，避免 dashboard 被完整错误文本打散。 */
public final class FailureCategoryClassifier {

    public static final String NONE = "none";
    public static final String UNKNOWN = "unknown_failure";

    private FailureCategoryClassifier() {}

    public static String classify(String status, String failureReason) {
        String normalizedStatus = normalize(status);
        String reason = normalize(failureReason);
        if ("completed".equals(normalizedStatus) || "succeeded".equals(normalizedStatus)) {
            return NONE;
        }
        if ("interrupted".equals(normalizedStatus)) {
            return "interrupted";
        }
        if ("terminated".equals(normalizedStatus)) {
            return "terminated";
        }
        if ("running".equals(normalizedStatus)) {
            return "running";
        }
        if (reason.isBlank()) {
            return UNKNOWN;
        }
        if (reason.contains("permission preflight")
                || reason.contains("network_access_reason")
                || reason.contains("writable root")
                || reason.contains("danger-full-access")
                || reason.contains("sandbox")) {
            return "permission";
        }
        if (reason.contains("after_create") || reason.contains("before_run") || reason.contains("hook failed")) {
            return "workspace_hook";
        }
        if (reason.contains("startup") || reason.contains("initialize") || reason.contains("app-server handshake")) {
            return "codex_startup";
        }
        if (reason.contains("response timeout") || reason.contains("turn timeout") || reason.contains("timeout")) {
            return "codex_timeout";
        }
        if (reason.contains("approval")) {
            return "approval_required";
        }
        if (reason.contains("dynamic tool") || reason.contains("tool_call")) {
            return "dynamic_tool";
        }
        if (reason.contains("user input") || reason.contains("input_required")) {
            return "user_input_required";
        }
        if (reason.contains("codex") || reason.contains("turn")) {
            return "codex_runtime";
        }
        return UNKNOWN;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
