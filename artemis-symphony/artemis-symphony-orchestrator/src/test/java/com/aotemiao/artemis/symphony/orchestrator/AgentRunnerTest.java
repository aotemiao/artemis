package com.aotemiao.artemis.symphony.orchestrator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aotemiao.artemis.symphony.config.ServiceConfig;
import com.aotemiao.artemis.symphony.core.model.Issue;
import com.aotemiao.artemis.symphony.core.model.WorkflowDefinition;
import com.aotemiao.artemis.symphony.core.model.Workspace;
import com.aotemiao.artemis.symphony.tracker.MemoryTrackerClient;
import com.aotemiao.artemis.symphony.workspace.WorkspaceManager;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class AgentRunnerTest {

    @Test
    void appendSpecDrivenDeliveryGuidance_whenDisabled_returnsPromptOnly() {
        ServiceConfig config = new ServiceConfig(new WorkflowDefinition(Map.of(), "prompt"));

        assertEquals("base prompt", AgentRunner.appendSpecDrivenDeliveryGuidance("base prompt", config));
    }

    @Test
    void appendSpecDrivenDeliveryGuidance_whenEnabled_appendsAssetGuidance() {
        ServiceConfig config = new ServiceConfig(new WorkflowDefinition(
                Map.of(
                        "delivery",
                        Map.of(
                                "spec_driven",
                                Map.of("enabled", true, "required_assets", List.of("docs/feature-specs/README.md")))),
                "prompt"));

        String prompt = AgentRunner.appendSpecDrivenDeliveryGuidance("base prompt", config);

        assertTrue(prompt.startsWith("base prompt"));
        assertTrue(prompt.contains("Spec-driven delivery guidance"));
        assertTrue(prompt.contains("docs/feature-specs/README.md"));
    }

    @Test
    void buildAdversarialReviewPrompt_includesIndependentReviewGuardrails() {
        String prompt = AgentRunner.buildAdversarialReviewPrompt(issue(), "run-1", Path.of("/tmp/workspace/ART-1"));

        assertTrue(prompt.contains("Adversarial review guidance"));
        assertTrue(prompt.contains("independent reviewer"));
        assertTrue(prompt.contains("read-only or diff-only mode"));
        assertTrue(prompt.contains("artemis-symphony/prompts/adversarial-review.md"));
        assertTrue(prompt.contains("implementation_run_id: `run-1`"));
        assertTrue(prompt.contains("## Reviewer Decision"));
    }

    @Test
    void runAttempt_whenPermissionPreflightFails_stopsBeforeBeforeRunAndCodex() {
        WorkflowDefinition definition = new WorkflowDefinition(
                Map.of(
                        "tracker",
                        Map.of("kind", "memory"),
                        "codex",
                        Map.of(
                                "command",
                                "printf should-not-run",
                                "turn_sandbox_policy",
                                Map.of(
                                        "type",
                                        "workspaceWrite",
                                        "writableRoots",
                                        List.of("/tmp/symphony_workspaces/ART-1", "/tmp/outside")))),
                "prompt");
        ServiceConfig config = new ServiceConfig(definition);
        RecordingWorkspaceManager workspaceManager = new RecordingWorkspaceManager(config);
        AgentRunner runner =
                new AgentRunner(() -> config, workspaceManager, () -> new MemoryTrackerClient(new ArrayList<>()));
        AtomicReference<String> failure = new AtomicReference<>();

        runner.runAttempt(
                issue(),
                null,
                null,
                null,
                null,
                () -> {
                    throw new AssertionError("preflight failure should not succeed");
                },
                failure::set);

        assertTrue(failure.get().contains("permission preflight failed"));
        assertTrue(failure.get().contains("/tmp/outside"));
        assertTrue(workspaceManager.afterRunCalled);
        assertFalse(workspaceManager.beforeRunCalled);
    }

    @Test
    void runAttempt_whenBeforeRunHookFails_stopsBeforeCodex() {
        WorkflowDefinition definition = new WorkflowDefinition(
                Map.of("tracker", Map.of("kind", "memory"), "codex", Map.of("command", "printf should-not-run")),
                "prompt");
        ServiceConfig config = new ServiceConfig(definition);
        RecordingWorkspaceManager workspaceManager = new RecordingWorkspaceManager(config);
        workspaceManager.beforeRunResult = WorkspaceManager.HookResult.failure("synthetic before_run failure");
        AgentRunner runner =
                new AgentRunner(() -> config, workspaceManager, () -> new MemoryTrackerClient(new ArrayList<>()));
        AtomicReference<String> failure = new AtomicReference<>();

        runner.runAttempt(
                issue(),
                null,
                null,
                null,
                null,
                () -> {
                    throw new AssertionError("before_run hook failure should not reach success");
                },
                failure::set);

        assertTrue(failure.get().contains("before_run hook failed"));
        assertTrue(failure.get().contains("synthetic before_run failure"));
        assertTrue(workspaceManager.beforeRunCalled);
        assertTrue(workspaceManager.afterRunCalled);
    }

    @Test
    void runAttempt_whenWorkspaceCreationFails_stopsBeforeBeforeRunAndCodex() {
        WorkflowDefinition definition = new WorkflowDefinition(
                Map.of("tracker", Map.of("kind", "memory"), "codex", Map.of("command", "printf should-not-run")),
                "prompt");
        ServiceConfig config = new ServiceConfig(definition);
        RecordingWorkspaceManager workspaceManager = new RecordingWorkspaceManager(config);
        workspaceManager.createResult =
                WorkspaceManager.Result.failure("after_create_hook_failed", "after_create hook failed: synthetic");
        AgentRunner runner =
                new AgentRunner(() -> config, workspaceManager, () -> new MemoryTrackerClient(new ArrayList<>()));
        AtomicReference<String> failure = new AtomicReference<>();

        runner.runAttempt(
                issue(),
                null,
                null,
                null,
                null,
                () -> {
                    throw new AssertionError("workspace creation failure should not reach success");
                },
                failure::set);

        assertNotNull(failure.get());
        assertFalse(workspaceManager.beforeRunCalled);
    }

    private static Issue issue() {
        return new Issue(
                "issue-1",
                "ART-1",
                "Permission preflight",
                null,
                1,
                "In Progress",
                null,
                null,
                List.of(),
                List.of(),
                Instant.parse("2026-06-09T00:00:00Z"),
                Instant.parse("2026-06-09T00:30:00Z"));
    }

    private static final class RecordingWorkspaceManager extends WorkspaceManager {
        private boolean beforeRunCalled;
        private boolean afterRunCalled;
        private Result<Workspace> createResult =
                Result.success(new Workspace(Path.of("/tmp/symphony_workspaces/ART-1"), "ART-1", false));
        private HookResult beforeRunResult = HookResult.ofSuccess();

        private RecordingWorkspaceManager(ServiceConfig config) {
            super(() -> config);
        }

        @Override
        public Result<Workspace> createForIssue(String issueIdentifier, String workerHost) {
            return createResult;
        }

        @Override
        public HookResult runBeforeRun(Path workspacePath, String workerHost) {
            beforeRunCalled = true;
            return beforeRunResult;
        }

        @Override
        public HookResult runAfterRun(Path workspacePath, String workerHost) {
            afterRunCalled = true;
            return HookResult.ofSuccess();
        }
    }
}
