package com.aotemiao.artemis.symphony.orchestrator;

import com.aotemiao.artemis.symphony.config.PromptRenderer;
import com.aotemiao.artemis.symphony.config.ServiceConfig;
import com.aotemiao.artemis.symphony.core.model.CodexUpdateEvent;
import com.aotemiao.artemis.symphony.core.model.Issue;
import com.aotemiao.artemis.symphony.core.model.Workspace;
import com.aotemiao.artemis.symphony.agent.CodexAppServerClient;
import com.aotemiao.artemis.symphony.tracker.LinearTrackerClient;
import com.aotemiao.artemis.symphony.workspace.WorkspaceManager;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runs one agent attempt: workspace + prompt + codex session. SPEC Section 10.7.
 */
public class AgentRunner {

    private final ServiceConfig config;
    private final WorkspaceManager workspaceManager;
    private final LinearTrackerClient trackerClient;

    public AgentRunner(ServiceConfig config, WorkspaceManager workspaceManager, LinearTrackerClient trackerClient) {
        this.config = config;
        this.workspaceManager = workspaceManager;
        this.trackerClient = trackerClient;
    }

    /**
     * Run one attempt for the given issue. Returns normally on success; throws or calls onFailure on
     * error.
     */
    public void runAttempt(
            Issue issue,
            Integer attempt,
            CodexAppServerClient.CodexUpdateListener updateListener,
            Runnable onSuccess,
            Runnable onFailure) {
        WorkspaceManager.Result<Workspace> wsResult = workspaceManager.createForIssue(issue.identifier());
        if (!wsResult.success()) {
            onFailure.run();
            return;
        }
        Path workspacePath = wsResult.value().path();

        WorkspaceManager.HookResult beforeRun = workspaceManager.runBeforeRun(workspacePath);
        if (!beforeRun.success()) {
            workspaceManager.runAfterRun(workspacePath);
            onFailure.run();
            return;
        }

        try {
            String promptTemplate = config.getPromptTemplate();
            Integer attemptForPrompt = attempt != null ? attempt : null;
            String prompt = PromptRenderer.render(promptTemplate, issue, attemptForPrompt);
            String title = issue.identifier() + ": " + (issue.title() != null ? issue.title() : "");

            CodexAppServerClient client = new CodexAppServerClient(
                    config.getCodexCommand(),
                    workspacePath,
                    config.getReadTimeoutMs(),
                    config.getTurnTimeoutMs(),
                    config.getCodexApprovalPolicy(),
                    config.getCodexThreadSandbox(),
                    config.getCodexTurnSandboxPolicy());

            if (updateListener != null) {
                client.addListener(updateListener);
            }

            String threadId = client.startSession();
            int maxTurns = config.getMaxTurns();
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

                var refreshResult = trackerClient.fetchIssueStatesByIds(List.of(currentIssue.id()));
                if (!refreshResult.isSuccess() || refreshResult.value() == null || refreshResult.value().isEmpty()) {
                    break;
                }
                currentIssue = refreshResult.value().get(0);
                if (!config.getTrackerActiveStates().contains(currentIssue.state())) {
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
