package com.aotemiao.artemis.symphony.orchestrator;

import com.aotemiao.artemis.symphony.agent.CodexAppServerClient;
import com.aotemiao.artemis.symphony.config.PermissionPreflight;
import com.aotemiao.artemis.symphony.config.PromptRenderer;
import com.aotemiao.artemis.symphony.config.ServiceConfig;
import com.aotemiao.artemis.symphony.core.model.Issue;
import com.aotemiao.artemis.symphony.core.model.Workspace;
import com.aotemiao.artemis.symphony.core.validation.DispatchValidation;
import com.aotemiao.artemis.symphony.tracker.TrackerClient;
import com.aotemiao.artemis.symphony.workspace.WorkspaceManager;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
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

    private final Supplier<TrackerClient> trackerSupplier;

    public AgentRunner(
            Supplier<ServiceConfig> configSupplier,
            WorkspaceManager workspaceManager,
            Supplier<TrackerClient> trackerSupplier) {
        this.configSupplier = configSupplier;
        this.workspaceManager = workspaceManager;
        this.trackerSupplier = trackerSupplier;
    }

    private ServiceConfig config() {
        return configSupplier.get();
    }

    private TrackerClient tracker() {
        return trackerSupplier.get();
    }

    /**
     * 对指定议题执行一次尝试。成功则正常返回；出错则调用 {@code onFailure}（或抛出异常，取决于实现）。
     */
    public void runAttempt(
            Issue issue,
            Integer attempt,
            String workerHost,
            CodexAppServerClient.CodexUpdateListener updateListener,
            Consumer<RuntimeInfo> runtimeInfoListener,
            Runnable onSuccess,
            Consumer<String> onFailure) {
        runAttemptInternal(
                issue,
                attempt,
                workerHost,
                updateListener,
                runtimeInfoListener,
                onSuccess,
                onFailure,
                AttemptKind.IMPLEMENTATION,
                "");
    }

    public void runAdversarialReviewAttempt(
            Issue issue,
            Integer attempt,
            String workerHost,
            String implementationRunId,
            CodexAppServerClient.CodexUpdateListener updateListener,
            Consumer<RuntimeInfo> runtimeInfoListener,
            Runnable onSuccess,
            Consumer<String> onFailure) {
        runAttemptInternal(
                issue,
                attempt,
                workerHost,
                updateListener,
                runtimeInfoListener,
                onSuccess,
                onFailure,
                AttemptKind.ADVERSARIAL_REVIEW,
                implementationRunId);
    }

    private void runAttemptInternal(
            Issue issue,
            Integer attempt,
            String workerHost,
            CodexAppServerClient.CodexUpdateListener updateListener,
            Consumer<RuntimeInfo> runtimeInfoListener,
            Runnable onSuccess,
            Consumer<String> onFailure,
            AttemptKind attemptKind,
            String implementationRunId) {
        WorkspaceManager.Result<Workspace> wsResult = workspaceManager.createForIssue(issue.identifier(), workerHost);
        if (!wsResult.isSuccess()) {
            onFailure.accept(describeFailure(wsResult.errorCode(), wsResult.errorMessage()));
            return;
        }
        Path workspacePath = wsResult.value().path();
        if (runtimeInfoListener != null) {
            runtimeInfoListener.accept(new RuntimeInfo(workerHost, workspacePath.toString()));
        }

        ServiceConfig cfg = config();
        boolean reviewAttempt = AttemptKind.ADVERSARIAL_REVIEW.equals(attemptKind);
        Object turnSandboxPolicy = reviewAttempt
                ? cfg.resolveAdversarialReviewTurnSandboxPolicy(workspacePath, workerHost != null)
                : cfg.resolveCodexTurnSandboxPolicy(workspacePath, workerHost != null);
        DispatchValidation permissionValidation = PermissionPreflight.validate(
                cfg, workspacePath, workerHost != null, cfg.getEffectiveCodexThreadSandbox(), turnSandboxPolicy);
        if (!permissionValidation.ok()) {
            workspaceManager.runAfterRun(workspacePath, workerHost);
            onFailure.accept("permission preflight failed: " + String.join("; ", permissionValidation.errors()));
            return;
        }

        WorkspaceManager.HookResult beforeRun = workspaceManager.runBeforeRun(workspacePath, workerHost);
        if (!beforeRun.isSuccess()) {
            workspaceManager.runAfterRun(workspacePath, workerHost);
            onFailure.accept("before_run hook failed: " + blankToDefault(beforeRun.error(), "unknown error"));
            return;
        }

        CodexAppServerClient client = null;
        try {
            String promptTemplate = config().getPromptTemplate();
            Integer attemptForPrompt = attempt != null ? attempt : null;
            String prompt = reviewAttempt
                    ? buildAdversarialReviewPrompt(issue, implementationRunId, workspacePath)
                    : appendSpecDrivenDeliveryGuidance(
                            PromptRenderer.render(promptTemplate, issue, attemptForPrompt), cfg);
            String title = issue.identifier() + ": " + (issue.title() != null ? issue.title() : "")
                    + (reviewAttempt ? " [adversarial review]" : "");

            CodexAppServerClient.DynamicToolExecutor dynamicToolExecutor =
                    "linear".equals(cfg.getTrackerKind()) ? new LinearGraphqlDynamicToolExecutor(this::tracker) : null;
            client = new CodexAppServerClient(
                    cfg.getCodexCommand(),
                    workspacePath,
                    cfg.getReadTimeoutMs(),
                    cfg.getTurnTimeoutMs(),
                    cfg.getCodexApprovalPolicy(),
                    cfg.getCodexThreadSandbox(),
                    turnSandboxPolicy,
                    dynamicToolExecutor,
                    workerHost);

            if (updateListener != null) {
                client.addListener(updateListener);
            }

            String threadId = client.startSession();
            int maxTurns = reviewAttempt ? 1 : config().getMaxTurns();
            int turnNumber = 1;
            Issue currentIssue = issue;

            while (turnNumber <= maxTurns) {
                String turnPrompt = turnNumber == 1
                        ? prompt
                        : """
                                Continuation guidance:

                                - The previous Codex turn completed normally, but the Linear issue is still in an active state.
                                - This is continuation turn #%d of %d for the current agent run.
                                - Resume from the current workspace and workpad state instead of restarting from scratch.
                                - The original task instructions and prior turn context are already present in this thread, so do not restate them before acting.
                                - Focus on the remaining ticket work and do not end the turn while the issue stays active unless you are truly blocked.
                                """.formatted(turnNumber, maxTurns).trim();
                boolean turnOk = client.runTurn(threadId, turnPrompt, title);
                if (!turnOk) {
                    onFailure.accept(blankToDefault(client.lastTurnFailureReason(), "codex turn failed"));
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
            onSuccess.run();
        } catch (Exception e) {
            onFailure.accept(blankToDefault(e.getMessage(), e.toString()));
        } finally {
            if (client != null) {
                client.stopSession();
            }
            workspaceManager.runAfterRun(workspacePath, workerHost);
        }
    }

    private static String describeFailure(String code, String message) {
        String resolvedCode = blankToDefault(code, "unknown_failure");
        String resolvedMessage = blankToDefault(message, "no detail");
        return resolvedCode + ": " + resolvedMessage;
    }

    private static String blankToDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    static String appendSpecDrivenDeliveryGuidance(String prompt, ServiceConfig cfg) {
        String addon = cfg != null ? cfg.getSpecDrivenDeliveryPromptAddon() : "";
        if (addon == null || addon.isBlank()) {
            return prompt;
        }
        String base = prompt != null ? prompt.trim() : "";
        if (base.isEmpty()) {
            return addon;
        }
        return base + "\n\n---\n\n" + addon;
    }

    static String buildAdversarialReviewPrompt(Issue issue, String implementationRunId, Path workspacePath) {
        String identifier = issue != null && issue.identifier() != null ? issue.identifier() : "";
        String title = issue != null && issue.title() != null ? issue.title() : "";
        String description = issue != null && issue.description() != null ? issue.description() : "";
        String workspace = workspacePath != null ? workspacePath.toString() : "";
        String parentRunId = implementationRunId != null ? implementationRunId : "";
        return """
                Adversarial review guidance:

                You are the independent reviewer for a completed Symphony implementation attempt. Review the current workspace in read-only or diff-only mode. Do not implement feature code, do not rewrite the handoff, and do not broaden the original task.

                Required local assets:

                - `docs/agent-workflow/AGENT_REVIEW_LOOP.md`
                - `docs/patterns/security-review-checklist.md`
                - `docs/patterns/agent-delivery-handoff.md`
                - `artemis-symphony/prompts/adversarial-review.md`
                - `artemis-symphony/skills/adversarial-review.md`

                Review target:

                - implementation_run_id: `%s`
                - workspace: `%s`
                - issue: `%s`
                - title: `%s`

                Issue description:

                %s

                Output a low-sensitive review result with these sections:

                ## Findings
                ## Missing Evidence
                ## Reviewer Decision
                """.formatted(parentRunId, workspace, identifier, title, description)
                .trim();
    }

    public enum AttemptKind {
        IMPLEMENTATION,
        ADVERSARIAL_REVIEW
    }

    public record RuntimeInfo(String workerHost, String workspacePath) {}
}
