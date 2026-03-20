package com.aotemiao.artemis.symphony.config;

import com.aotemiao.artemis.symphony.core.validation.DispatchValidation;

import java.util.ArrayList;
import java.util.List;

/** 调度前预检验证。见 SPEC 第 6.3 节。 */
public final class DispatchPreflight {

    private DispatchPreflight() {}

    public static DispatchValidation validate(ServiceConfig config) {
        List<String> errors = new ArrayList<>();

        if (config.getTrackerKind() == null || config.getTrackerKind().isBlank()) {
            errors.add("tracker.kind is required");
        } else if (!"linear".equals(config.getTrackerKind())) {
            errors.add("tracker.kind must be 'linear' (only supported value)");
        }

        if (config.getTrackerApiKey() == null || config.getTrackerApiKey().isBlank()) {
            errors.add("tracker.api_key is required (set LINEAR_API_KEY or configure in workflow)");
        }

        if ("linear".equals(config.getTrackerKind())
                && (config.getTrackerProjectSlug() == null || config.getTrackerProjectSlug().isBlank())) {
            errors.add("tracker.project_slug is required when tracker.kind is linear");
        }

        String cmd = config.getCodexCommand();
        if (cmd == null || cmd.isBlank()) {
            errors.add("codex.command must be present and non-empty");
        }

        if (errors.isEmpty()) {
            return DispatchValidation.success();
        }
        return DispatchValidation.failure(errors);
    }
}
