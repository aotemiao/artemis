package com.aotemiao.artemis.symphony.workspace;

import com.aotemiao.artemis.symphony.config.ServiceConfig;
import com.aotemiao.artemis.symphony.core.WorkspaceKeys;
import com.aotemiao.artemis.symphony.core.model.Workspace;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Maps issue identifiers to workspace paths, ensures directories exist, runs hooks. SPEC Section 9.
 */
public class WorkspaceManager {

    private final ServiceConfig config;
    private final HookRunner hookRunner;

    public WorkspaceManager(ServiceConfig config) {
        this.config = config;
        this.hookRunner = new HookRunner(config.getHooksTimeoutMs());
    }

    /**
     * Create or reuse workspace for the given issue identifier. Runs after_create only when newly
     * created. SPEC 9.2.
     */
    public Result<Workspace> createForIssue(String issueIdentifier) {
        Path root = config.getWorkspaceRoot().toAbsolutePath();
        String workspaceKey = WorkspaceKeys.sanitize(issueIdentifier);
        Path workspacePath = root.resolve(workspaceKey).normalize();

        if (!workspacePath.startsWith(root)) {
            return Result.failure("invalid_workspace_path", "Workspace path outside root");
        }

        try {
            boolean createdNow = Files.notExists(workspacePath) || !Files.isDirectory(workspacePath);
            if (createdNow) {
                java.nio.file.Files.createDirectories(workspacePath);
            }
            Workspace workspace = new Workspace(workspacePath, workspaceKey, createdNow);

            if (createdNow) {
                String afterCreate = config.getHookAfterCreate();
                if (afterCreate != null && !afterCreate.isBlank()) {
                    HookResult hr = hookRunner.run(afterCreate, workspacePath);
                    if (!hr.isSuccess()) {
                        return Result.failure("after_create_hook_failed", hr.error());
                    }
                }
            }
            return Result.success(workspace);
        } catch (Exception e) {
            return Result.failure("workspace_creation_failed", e.getMessage());
        }
    }

    /**
     * Run before_run hook. Failure aborts the current attempt.
     */
    public HookResult runBeforeRun(Path workspacePath) {
        String script = config.getHookBeforeRun();
        if (script == null || script.isBlank()) {
            return HookResult.ofSuccess();
        }
        return hookRunner.run(script, workspacePath);
    }

    /**
     * Run after_run hook. Failure is logged but ignored.
     */
    public HookResult runAfterRun(Path workspacePath) {
        String script = config.getHookAfterRun();
        if (script == null || script.isBlank()) {
            return HookResult.ofSuccess();
        }
        return hookRunner.run(script, workspacePath);
    }

    /**
     * Run before_remove hook then remove workspace directory. SPEC 9.4.
     */
    public void removeWorkspace(Path workspacePath) {
        if (workspacePath == null) return;
        String script = config.getHookBeforeRemove();
        if (script != null && !script.isBlank()) {
            hookRunner.run(script, workspacePath);
        }
        try {
            if (Files.exists(workspacePath) && Files.isDirectory(workspacePath)) {
                deleteRecursively(workspacePath);
            }
        } catch (Exception e) {
            // log and ignore per spec
        }
    }

    public Path getWorkspaceRoot() {
        return config.getWorkspaceRoot().toAbsolutePath();
    }

    public record Result<T>(boolean success, String errorCode, String errorMessage, T value) {

        public boolean isSuccess() {
            return success;
        }

        public static <T> Result<T> success(T value) {
            return new Result<>(true, null, null, value);
        }

        public static <T> Result<T> failure(String errorCode, String errorMessage) {
            return new Result<>(false, errorCode, errorMessage, null);
        }
    }

    public record HookResult(boolean success, String output, String error) {

        public boolean isSuccess() {
            return success;
        }

        /** Static factory to avoid name clash with record accessor success(). */
        public static HookResult ofSuccess() {
            return new HookResult(true, null, null);
        }

        public static HookResult failure(String error) {
            return new HookResult(false, null, error);
        }
    }

    private static class HookRunner {
        private final int timeoutMs;

        HookRunner(int timeoutMs) {
            this.timeoutMs = timeoutMs <= 0 ? 60_000 : timeoutMs;
        }

        HookResult run(String script, Path cwd) {
            try {
                ProcessBuilder pb =
                        new ProcessBuilder()
                                .command("bash", "-lc", script)
                                .directory(cwd.toFile())
                                .redirectErrorStream(true);
                Process p = pb.start();
                String output = new String(p.getInputStream().readAllBytes());
                boolean finished = p.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
                if (!finished) {
                    p.destroyForcibly();
                    return HookResult.failure("hook timeout after " + timeoutMs + " ms");
                }
                if (p.exitValue() != 0) {
                    return HookResult.failure("exit " + p.exitValue() + ": " + truncate(output, 500));
                }
                return HookResult.ofSuccess();
            } catch (Exception e) {
                return HookResult.failure(e.getMessage());
            }
        }

        private static String truncate(String s, int max) {
            if (s == null || s.length() <= max) return s;
            return s.substring(0, max) + "...";
        }
    }

    private static void deleteRecursively(Path path) throws java.io.IOException {
        if (Files.isDirectory(path)) {
            try (var stream = Files.list(path)) {
                List<Path> children = stream.toList();
                for (Path child : children) {
                    deleteRecursively(child);
                }
            }
        }
        Files.delete(path);
    }

    private static final class Files {
        static boolean exists(Path p) {
            return java.nio.file.Files.exists(p);
        }

        static boolean notExists(Path p) {
            return java.nio.file.Files.notExists(p);
        }

        static boolean isDirectory(Path p) {
            return java.nio.file.Files.isDirectory(p);
        }

        static java.util.stream.Stream<Path> list(Path p) throws java.io.IOException {
            return java.nio.file.Files.list(p);
        }

        static void delete(Path p) throws java.io.IOException {
            java.nio.file.Files.delete(p);
        }
    }
}
