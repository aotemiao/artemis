package com.aotemiao.artemis.symphony.orchestrator;

import com.aotemiao.artemis.symphony.agent.CodexAppServerClient;
import com.aotemiao.artemis.symphony.config.PromptRenderer;
import com.aotemiao.artemis.symphony.config.ServiceConfig;
import com.aotemiao.artemis.symphony.core.model.Issue;
import com.aotemiao.artemis.symphony.core.model.Workspace;
import com.aotemiao.artemis.symphony.tracker.LinearTrackerClient;
import com.aotemiao.artemis.symphony.workspace.WorkspaceManager;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * 执行单次代理尝试：工作区 + 提示词 + Codex 会话。见 SPEC 第 10.7 节。
 */
public class AgentRunner {

    private final Supplier<ServiceConfig> configSupplier;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "WorkspaceManager is a shared orchestrator collaborator injected and not exposed.")
    private final WorkspaceManager workspaceManager;

    private final Supplier<LinearTrackerClient> trackerSupplier;

    public AgentRunner(
            Supplier<ServiceConfig> configSupplier,
            WorkspaceManager workspaceManager,
            Supplier<LinearTrackerClient> trackerSupplier) {
        this.configSupplier = configSupplier;
        this.workspaceManager = workspaceManager;
        this.trackerSupplier = trackerSupplier;
    }

    private ServiceConfig config() {
        return configSupplier.get();
    }

    private LinearTrackerClient tracker() {
        return trackerSupplier.get();
    }

    /**
     * 对指定议题执行一次尝试。成功则正常返回；出错则调用 {@code onFailure}（或抛出异常，取决于实现）。
     */
    public void runAttempt(
            Issue issue,
            Integer attempt,
            CodexAppServerClient.CodexUpdateListener updateListener,
            Runnable onSuccess,
            Runnable onFailure) {
        WorkspaceManager.Result<Workspace> wsResult = workspaceManager.createForIssue(issue.identifier());
        if (!wsResult.isSuccess()) {
            onFailure.run();
            return;
        }
        Path workspacePath = wsResult.value().path();

        WorkspaceManager.HookResult beforeRun = workspaceManager.runBeforeRun(workspacePath);
        if (!beforeRun.isSuccess()) {
            workspaceManager.runAfterRun(workspacePath);
            onFailure.run();
            return;
        }

        try {
            String promptTemplate = config().getPromptTemplate();
            Integer attemptForPrompt = attempt != null ? attempt : null;
            String prompt = PromptRenderer.render(promptTemplate, issue, attemptForPrompt);
            String title = issue.identifier() + ": " + (issue.title() != null ? issue.title() : "");

            ServiceConfig cfg = config();
            CodexAppServerClient client = new CodexAppServerClient(
                    cfg.getCodexCommand(),
                    workspacePath,
                    cfg.getReadTimeoutMs(),
                    cfg.getTurnTimeoutMs(),
                    cfg.getCodexApprovalPolicy(),
                    cfg.getCodexThreadSandbox(),
                    cfg.getCodexTurnSandboxPolicy());

            if (updateListener != null) {
                client.addListener(updateListener);
            }

            String threadId = client.startSession();
            int maxTurns = config().getMaxTurns();
            int turnNumber = 1;
            Issue currentIssue = issue;
            AtomicBoolean success = new AtomicBoolean(false);

            while (turnNumber <= maxTurns) {
                String turnPrompt = turnNumber == 1 ? prompt : "Continue with the next steps.";
                boolean turnOk = client.runTurn(threadId, turnPrompt, title);
                if (!turnOk) {
                    client.stopSession();
                    workspaceManager.runAfterRun(workspacePath);
                    onFailure.run();
                    return;
                }

                var refreshResult = tracker().fetchIssueStatesByIds(List.of(currentIssue.id()));
                if (!refreshResult.isSuccess()
                        || refreshResult.value() == null
                        || refreshResult.value().isEmpty()) {
                    break;
                }
                currentIssue = refreshResult.value().get(0);
                List<String> activeNorm = config().getTrackerActiveStates().stream()
                        .map(String::toLowerCase)
                        .toList();
                if (!activeNorm.contains(currentIssue.stateNormalized())) {
                    break;
                }
                if (turnNumber >= maxTurns) {
                    break;
                }
                turnNumber++;
            }

            client.stopSession();
            success.set(true);
            onSuccess.run();
        } catch (Exception e) {
            onFailure.run();
        } finally {
            workspaceManager.runAfterRun(workspacePath);
        }
    }
}
