package com.aotemiao.artemis.symphony.config;

import com.aotemiao.artemis.symphony.core.validation.DispatchValidation;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Codex 权限边界预检，防止单次 run 在未说明时扩大 sandbox / 网络 / 写目录范围。 */
public final class PermissionPreflight {

    private PermissionPreflight() {}

    public static DispatchValidation validate(ServiceConfig config, Path workspacePath, boolean remoteWorker) {
        if (config == null) {
            return DispatchValidation.failure(List.of("permission preflight requires service config"));
        }
        return validate(
                config,
                workspacePath,
                remoteWorker,
                config.getEffectiveCodexThreadSandbox(),
                config.resolveCodexTurnSandboxPolicy(workspacePath, remoteWorker));
    }

    public static DispatchValidation validate(
            ServiceConfig config,
            Path workspacePath,
            boolean remoteWorker,
            String threadSandbox,
            Object turnSandboxPolicy) {
        List<String> errors = new ArrayList<>();
        if (config == null) {
            errors.add("permission preflight requires service config");
            return DispatchValidation.failure(errors);
        }

        String effectiveThreadSandbox = threadSandbox != null && !threadSandbox.isBlank()
                ? threadSandbox
                : config.getEffectiveCodexThreadSandbox();
        if ("danger-full-access".equalsIgnoreCase(effectiveThreadSandbox) && !config.isDangerFullAccessAllowed()) {
            errors.add("codex.thread_sandbox=danger-full-access requires permissions.allow_danger_full_access=true");
        }

        if (turnSandboxPolicy instanceof Map<?, ?> policy) {
            validateTurnSandboxPolicy(config, workspacePath, remoteWorker, policy, errors);
        } else if (turnSandboxPolicy != null) {
            errors.add("codex.turn_sandbox_policy must be a map when configured");
        }

        if (errors.isEmpty()) {
            return DispatchValidation.success();
        }
        return DispatchValidation.failure(errors);
    }

    private static void validateTurnSandboxPolicy(
            ServiceConfig config, Path workspacePath, boolean remoteWorker, Map<?, ?> policy, List<String> errors) {
        Object type = policy.get("type");
        if (type != null
                && "dangerFullAccess".equalsIgnoreCase(type.toString())
                && !config.isDangerFullAccessAllowed()) {
            errors.add(
                    "codex.turn_sandbox_policy.type=dangerFullAccess requires permissions.allow_danger_full_access=true");
        }

        Object networkAccess = policy.get("networkAccess");
        if (isNetworkEnabled(networkAccess) && config.getNetworkAccessReason() == null) {
            errors.add("codex.turn_sandbox_policy.networkAccess requires permissions.network_access_reason");
        }

        for (String root : writableRoots(policy.get("writableRoots"))) {
            if (!isWritableRootAllowed(config, workspacePath, remoteWorker, root)) {
                errors.add("writable root is outside issue workspace and permissions.allowed_writable_roots: " + root);
            }
        }
    }

    private static boolean isNetworkEnabled(Object value) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value == null) {
            return false;
        }
        String text = value.toString().trim().toLowerCase(Locale.ROOT);
        return "true".equals(text) || "enabled".equals(text);
    }

    private static List<String> writableRoots(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
                .map(item -> item == null ? "" : item.toString().trim())
                .filter(item -> !item.isBlank())
                .toList();
    }

    private static boolean isWritableRootAllowed(
            ServiceConfig config, Path workspacePath, boolean remoteWorker, String writableRoot) {
        if (writableRoot == null || writableRoot.isBlank()) {
            return true;
        }
        if (workspacePath != null && pathStartsWith(writableRoot, workspacePath.toString(), remoteWorker)) {
            return true;
        }
        for (String allowedRoot : config.getAllowedWritableRoots()) {
            if (pathStartsWith(writableRoot, allowedRoot, remoteWorker)) {
                return true;
            }
        }
        return false;
    }

    private static boolean pathStartsWith(String child, String parent, boolean remoteWorker) {
        if (child == null || child.isBlank() || parent == null || parent.isBlank()) {
            return false;
        }
        if (remoteWorker) {
            String normalizedChild = normalizeRemotePath(child);
            String normalizedParent = normalizeRemotePath(parent);
            return normalizedChild.equals(normalizedParent) || normalizedChild.startsWith(normalizedParent + "/");
        }
        Path childPath = Path.of(child).toAbsolutePath().normalize();
        Path parentPath = Path.of(parent).toAbsolutePath().normalize();
        return childPath.startsWith(parentPath);
    }

    private static String normalizeRemotePath(String path) {
        String normalized = path.trim().replace('\\', '/');
        while (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
