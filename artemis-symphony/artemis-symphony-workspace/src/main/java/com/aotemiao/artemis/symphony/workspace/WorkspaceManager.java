package com.aotemiao.artemis.symphony.workspace;

import com.aotemiao.artemis.symphony.config.ServiceConfig;
import com.aotemiao.artemis.symphony.core.WorkspaceKeys;
import com.aotemiao.artemis.symphony.core.model.Workspace;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 将议题标识映射到工作区路径、确保目录存在并执行钩子脚本。见 SPEC 第 9 节。
 */
public class WorkspaceManager {

    private final Supplier<ServiceConfig> configSupplier;

    public WorkspaceManager(Supplier<ServiceConfig> configSupplier) {
        this.configSupplier = configSupplier;
    }

    private ServiceConfig config() {
        return configSupplier.get();
    }

    /**
     * 为议题标识创建或复用工作区；仅在本次新建目录时执行 after_create。见 SPEC 9.2。
     */
    public Result<Workspace> createForIssue(String issueIdentifier) {
        ServiceConfig config = config();
        Path root = config.getWorkspaceRoot().toAbsolutePath();
        String workspaceKey = WorkspaceKeys.sanitize(issueIdentifier);
        Path workspacePath = root.resolve(workspaceKey).normalize();

        if (!workspacePath.startsWith(root)) {
            return Result.failure("invalid_workspace_path", "工作区路径超出根目录范围");
        }

        try {
            boolean createdNow = Files.notExists(workspacePath) || !Files.isDirectory(workspacePath);
            if (createdNow) {
                Files.createDirectories(workspacePath);
            }
            Workspace workspace = new Workspace(workspacePath, workspaceKey, createdNow);
            HookRunner hookRunner = new HookRunner(config.getHooksTimeoutMs());

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

    /** 执行 before_run 钩子；失败则中止当前尝试。 */
    public HookResult runBeforeRun(Path workspacePath) {
        ServiceConfig config = config();
        String script = config.getHookBeforeRun();
        if (script == null || script.isBlank()) {
            return HookResult.ofSuccess();
        }
        return new HookRunner(config.getHooksTimeoutMs()).run(script, workspacePath);
    }

    /** 执行 after_run 钩子；失败仅记录日志，按 SPEC 忽略。 */
    public HookResult runAfterRun(Path workspacePath) {
        ServiceConfig config = config();
        String script = config.getHookAfterRun();
        if (script == null || script.isBlank()) {
            return HookResult.ofSuccess();
        }
        return new HookRunner(config.getHooksTimeoutMs()).run(script, workspacePath);
    }

    /** 执行 before_remove 钩子后删除工作区目录。见 SPEC 9.4。 */
    public void removeWorkspace(Path workspacePath) {
        if (workspacePath == null) return;
        ServiceConfig config = config();
        String script = config.getHookBeforeRemove();
        if (script != null && !script.isBlank()) {
            new HookRunner(config.getHooksTimeoutMs()).run(script, workspacePath);
        }
        try {
            if (Files.exists(workspacePath) && Files.isDirectory(workspacePath)) {
                deleteRecursively(workspacePath);
            }
        } catch (Exception e) {
            // 按 SPEC 记录日志并忽略异常
        }
    }

    public Path getWorkspaceRoot() {
        return config().getWorkspaceRoot().toAbsolutePath();
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

        /** 静态工厂方法，避免与 record 访问器 {@code success()} 命名冲突。 */
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
}
