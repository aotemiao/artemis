package com.aotemiao.artemis.symphony.workspace;

import com.aotemiao.artemis.symphony.config.ServiceConfig;
import com.aotemiao.artemis.symphony.core.SshClient;
import com.aotemiao.artemis.symphony.core.WorkspaceKeys;
import com.aotemiao.artemis.symphony.core.model.Workspace;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 将议题标识映射到工作区路径、确保目录存在并执行钩子脚本。见 SPEC 第 9 节。
 */
public class WorkspaceManager {

    private static final String REMOTE_WORKSPACE_MARKER = "__SYMPHONY_WORKSPACE__";

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
        return createForIssue(issueIdentifier, null);
    }

    public Result<Workspace> createForIssue(String issueIdentifier, String workerHost) {
        String workspaceKey = WorkspaceKeys.sanitize(issueIdentifier);
        try {
            Workspace workspace = workerHost == null
                    ? createLocalWorkspace(workspaceKey)
                    : createRemoteWorkspace(workspaceKey, workerHost);

            if (workspace.createdNow()) {
                String afterCreate = config().getHookAfterCreate();
                if (afterCreate != null && !afterCreate.isBlank()) {
                    HookResult hr = runHook(afterCreate, workspace.path(), workerHost);
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
        return runBeforeRun(workspacePath, null);
    }

    public HookResult runBeforeRun(Path workspacePath, String workerHost) {
        ServiceConfig config = config();
        String script = config.getHookBeforeRun();
        if (script == null || script.isBlank()) {
            return HookResult.ofSuccess();
        }
        return runHook(script, workspacePath, workerHost);
    }

    /** 执行 after_run 钩子；失败仅记录日志，按 SPEC 忽略。 */
    public HookResult runAfterRun(Path workspacePath) {
        return runAfterRun(workspacePath, null);
    }

    public HookResult runAfterRun(Path workspacePath, String workerHost) {
        ServiceConfig config = config();
        String script = config.getHookAfterRun();
        if (script == null || script.isBlank()) {
            return HookResult.ofSuccess();
        }
        return runHook(script, workspacePath, workerHost);
    }

    /** 执行 before_remove 钩子后删除工作区目录。见 SPEC 9.4。 */
    public void removeWorkspace(Path workspacePath) {
        removeWorkspace(workspacePath, null);
    }

    public void removeWorkspace(Path workspacePath, String workerHost) {
        if (workspacePath == null) {
            return;
        }
        ServiceConfig config = config();
        String script = config.getHookBeforeRemove();
        if (script != null && !script.isBlank()) {
            runBeforeRemove(script, workspacePath, workerHost);
        }
        try {
            if (workerHost == null) {
                if (Files.exists(workspacePath) && Files.isDirectory(workspacePath)) {
                    deleteRecursively(workspacePath);
                }
            } else {
                RemoteCommandResult result = runRemoteCommand(
                        workerHost,
                        remoteShellAssign("workspace", workspacePath.toString())
                                + "\nrm -rf \"$workspace\"");
                if (result.exitCode() != 0) {
                    // 按 SPEC 忽略 remote 清理失败，交给日志与后续 reconcile 收敛。
                }
            }
        } catch (Exception ignored) {
            // 按 SPEC 记录日志并忽略异常
        }
    }

    public void removeIssueWorkspaces(String issueIdentifier) {
        String workspaceKey = WorkspaceKeys.sanitize(issueIdentifier);
        List<String> workerHosts = config().getWorkerSshHosts();
        if (workerHosts.isEmpty()) {
            removeWorkspace(localWorkspacePath(workspaceKey), null);
            return;
        }
        for (String workerHost : workerHosts) {
            removeWorkspace(Path.of(remoteWorkspacePath(workspaceKey)), workerHost);
        }
    }

    public Path getWorkspaceRoot() {
        return config().getWorkspaceRoot().toAbsolutePath();
    }

    public String getWorkspaceRootRaw() {
        return config().getWorkspaceRootRaw();
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

    private Workspace createLocalWorkspace(String workspaceKey) throws Exception {
        Path root = config().getWorkspaceRoot().toAbsolutePath().normalize();
        Path workspacePath = root.resolve(workspaceKey).normalize();
        if (!workspacePath.startsWith(root)) {
            throw new IllegalArgumentException("工作区路径超出根目录范围");
        }
        boolean createdNow;
        if (Files.isDirectory(workspacePath)) {
            createdNow = false;
        } else {
            if (Files.exists(workspacePath)) {
                deleteRecursively(workspacePath);
            }
            Files.createDirectories(workspacePath);
            createdNow = true;
        }
        return new Workspace(workspacePath, workspaceKey, createdNow);
    }

    private Workspace createRemoteWorkspace(String workspaceKey, String workerHost) throws Exception {
        String workspacePath = remoteWorkspacePath(workspaceKey);
        validateRemoteWorkspacePath(workspacePath);
        RemoteCommandResult result = runRemoteCommand(workerHost, """
                set -eu
                %s
                if [ -d "$workspace" ]; then
                  created=0
                elif [ -e "$workspace" ]; then
                  rm -rf "$workspace"
                  mkdir -p "$workspace"
                  created=1
                else
                  mkdir -p "$workspace"
                  created=1
                fi
                cd "$workspace"
                printf '%%s\\t%%s\\t%%s\\n' '%s' "$created" "$(pwd -P)"
                """
                .formatted(remoteShellAssign("workspace", workspacePath), REMOTE_WORKSPACE_MARKER));
        if (result.exitCode() != 0) {
            throw new IllegalStateException("workspace_prepare_failed: " + truncate(result.output(), 500));
        }
        ParsedRemoteWorkspace parsed = parseRemoteWorkspace(result.output());
        return new Workspace(Path.of(parsed.path()), workspaceKey, parsed.createdNow());
    }

    private HookResult runHook(String script, Path cwd, String workerHost) {
        if (cwd == null) {
            return HookResult.failure("workspace path is required");
        }
        if (workerHost == null) {
            return new HookRunner(config().getHooksTimeoutMs()).run(script, cwd);
        }
        try {
            RemoteCommandResult result = runRemoteCommand(
                    workerHost,
                    "cd " + SshClient.shellEscape(cwd.toString()) + " && " + script);
            if (result.exitCode() != 0) {
                return HookResult.failure("exit " + result.exitCode() + ": " + truncate(result.output(), 500));
            }
            return HookResult.ofSuccess();
        } catch (Exception e) {
            return HookResult.failure(e.getMessage());
        }
    }

    private void runBeforeRemove(String script, Path workspacePath, String workerHost) {
        try {
            if (workerHost == null) {
                if (Files.exists(workspacePath) && Files.isDirectory(workspacePath)) {
                    new HookRunner(config().getHooksTimeoutMs()).run(script, workspacePath);
                }
                return;
            }
            runRemoteCommand(
                    workerHost,
                    """
                    %s
                    if [ -d "$workspace" ]; then
                      cd "$workspace"
                      %s
                    fi
                    """
                            .formatted(remoteShellAssign("workspace", workspacePath.toString()), script));
        } catch (Exception ignored) {
            // before_remove 与参考实现一致：失败不阻断主流程
        }
    }

    private Path localWorkspacePath(String workspaceKey) {
        return config().getWorkspaceRoot().toAbsolutePath().normalize().resolve(workspaceKey).normalize();
    }

    private String remoteWorkspacePath(String workspaceKey) {
        String root = config().getWorkspaceRootRaw();
        if (root.endsWith("/")) {
            return root + workspaceKey;
        }
        return root + "/" + workspaceKey;
    }

    private static void validateRemoteWorkspacePath(String workspacePath) {
        if (workspacePath == null || workspacePath.isBlank()) {
            throw new IllegalArgumentException("工作区路径为空");
        }
        if (workspacePath.contains("\n") || workspacePath.contains("\r") || workspacePath.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("工作区路径包含非法字符");
        }
    }

    private RemoteCommandResult runRemoteCommand(String workerHost, String script) throws Exception {
        HookRunner.TimeoutTask<RemoteCommandResult> task =
                new HookRunner.TimeoutTask<>(() -> {
                    SshClient.CommandResult result = SshClient.run(workerHost, script);
                    return new RemoteCommandResult(result.output(), result.exitCode());
                });
        return task.await(config().getHooksTimeoutMs(), "remote command timeout after ");
    }

    private static String remoteShellAssign(String variableName, String rawPath) {
        return """
                %1$s=%2$s
                if [ "$%1$s" = "~" ]; then
                  %1$s="$HOME"
                elif [ "${%1$s#\\~/}" != "$%1$s" ]; then
                  %1$s="$HOME/${%1$s#\\~/}"
                fi
                """
                .formatted(variableName, SshClient.shellEscape(rawPath))
                .trim();
    }

    private static ParsedRemoteWorkspace parseRemoteWorkspace(String output) {
        String[] lines = output.split("\\R");
        for (String line : lines) {
            String[] parts = line.split("\t", 3);
            if (parts.length == 3
                    && REMOTE_WORKSPACE_MARKER.equals(parts[0])
                    && ("0".equals(parts[1]) || "1".equals(parts[1]))
                    && !parts[2].isBlank()) {
                return new ParsedRemoteWorkspace("1".equals(parts[1]), parts[2]);
            }
        }
        throw new IllegalStateException("workspace_prepare_failed: invalid output");
    }

    private static String truncate(String s, int max) {
        if (s == null || s.length() <= max) {
            return s;
        }
        return s.substring(0, max) + "...";
    }

    private static class HookRunner {
        private final int timeoutMs;

        HookRunner(int timeoutMs) {
            this.timeoutMs = timeoutMs <= 0 ? 60_000 : timeoutMs;
        }

        HookResult run(String script, Path cwd) {
            try {
                ProcessBuilder pb = new ProcessBuilder()
                        .command("bash", "-lc", script)
                        .directory(cwd.toFile())
                        .redirectErrorStream(true);
                Process p = pb.start();
                String output = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
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

        private static final class TimeoutTask<T> {
            private final java.util.concurrent.Callable<T> callable;

            private TimeoutTask(java.util.concurrent.Callable<T> callable) {
                this.callable = callable;
            }

            private T await(int timeoutMs, String timeoutPrefix) throws Exception {
                java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
                try {
                    java.util.concurrent.Future<T> future = executor.submit(callable);
                    return future.get(timeoutMs, TimeUnit.MILLISECONDS);
                } catch (java.util.concurrent.TimeoutException e) {
                    throw new IllegalStateException(timeoutPrefix + timeoutMs + " ms", e);
                } finally {
                    executor.shutdownNow();
                }
            }
        }
    }

    private record RemoteCommandResult(String output, int exitCode) {}

    private record ParsedRemoteWorkspace(boolean createdNow, String path) {}

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
