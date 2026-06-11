package com.aotemiao.artemis.symphony.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aotemiao.artemis.symphony.core.model.WorkflowDefinition;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PermissionPreflightTest {

    @Test
    void validate_defaultWorkspaceSandboxAllowsIssueWorkspaceOnly() {
        ServiceConfig config = new ServiceConfig(new WorkflowDefinition(
                Map.of("tracker", Map.of("kind", "memory"), "workspace", Map.of("root", "./symphony_workspaces")),
                "prompt"));

        var validation = PermissionPreflight.validate(config, Path.of("symphony_workspaces/ART-1"), false);

        assertTrue(validation.ok());
    }

    @Test
    void validate_rejectsWritableRootOutsideWorkspaceUnlessAllowed() {
        ServiceConfig config = new ServiceConfig(new WorkflowDefinition(
                Map.of(
                        "tracker",
                        Map.of("kind", "memory"),
                        "codex",
                        Map.of(
                                "turn_sandbox_policy",
                                Map.of(
                                        "type",
                                        "workspaceWrite",
                                        "writableRoots",
                                        List.of("/tmp/symphony_workspaces/ART-1", "/tmp/shared")))),
                "prompt"));

        var validation = PermissionPreflight.validate(config, Path.of("/tmp/symphony_workspaces/ART-1"), false);

        assertFalse(validation.ok());
        assertTrue(validation.errors().get(0).contains("/tmp/shared"));
    }

    @Test
    void validate_acceptsConfiguredWritableRootAllowlist() {
        ServiceConfig config = new ServiceConfig(new WorkflowDefinition(
                Map.of(
                        "tracker",
                        Map.of("kind", "memory"),
                        "permissions",
                        Map.of("allowed_writable_roots", List.of("/tmp/shared")),
                        "codex",
                        Map.of(
                                "turn_sandbox_policy",
                                Map.of(
                                        "type",
                                        "workspaceWrite",
                                        "writableRoots",
                                        List.of("/tmp/symphony_workspaces/ART-1", "/tmp/shared/cache")))),
                "prompt"));

        var validation = PermissionPreflight.validate(config, Path.of("/tmp/symphony_workspaces/ART-1"), false);

        assertTrue(validation.ok());
    }

    @Test
    void validate_rejectsNetworkAccessWithoutReason() {
        ServiceConfig config = new ServiceConfig(new WorkflowDefinition(
                Map.of(
                        "tracker",
                        Map.of("kind", "memory"),
                        "codex",
                        Map.of(
                                "turn_sandbox_policy",
                                Map.of(
                                        "type",
                                        "workspaceWrite",
                                        "writableRoots",
                                        List.of("/tmp/symphony_workspaces/ART-1"),
                                        "networkAccess",
                                        true))),
                "prompt"));

        var validation = PermissionPreflight.validate(config, Path.of("/tmp/symphony_workspaces/ART-1"), false);

        assertFalse(validation.ok());
        assertTrue(validation.errors().get(0).contains("network_access_reason"));
    }

    @Test
    void validate_acceptsNetworkAccessWithReason() {
        ServiceConfig config = new ServiceConfig(new WorkflowDefinition(
                Map.of(
                        "tracker",
                        Map.of("kind", "memory"),
                        "permissions",
                        Map.of("network_access_reason", "download public build dependencies"),
                        "codex",
                        Map.of(
                                "turn_sandbox_policy",
                                Map.of(
                                        "type",
                                        "workspaceWrite",
                                        "writableRoots",
                                        List.of("/tmp/symphony_workspaces/ART-1"),
                                        "networkAccess",
                                        true))),
                "prompt"));

        var validation = PermissionPreflight.validate(config, Path.of("/tmp/symphony_workspaces/ART-1"), false);

        assertTrue(validation.ok());
    }

    @Test
    void validate_rejectsDangerFullAccessUnlessExplicitlyAllowed() {
        ServiceConfig config = new ServiceConfig(new WorkflowDefinition(
                Map.of("tracker", Map.of("kind", "memory"), "codex", Map.of("thread_sandbox", "none")), "prompt"));

        var validation = PermissionPreflight.validate(config, Path.of("/tmp/symphony_workspaces/ART-1"), false);

        assertFalse(validation.ok());
        assertTrue(validation.errors().get(0).contains("allow_danger_full_access"));
    }

    @Test
    void validate_acceptsRemoteWorkspacePrefix() {
        ServiceConfig config = new ServiceConfig(new WorkflowDefinition(
                Map.of(
                        "tracker",
                        Map.of("kind", "memory"),
                        "codex",
                        Map.of(
                                "turn_sandbox_policy",
                                Map.of(
                                        "type",
                                        "workspaceWrite",
                                        "writableRoots",
                                        List.of("~/symphony_workspaces/ART-1/tmp")))),
                "prompt"));

        var validation = PermissionPreflight.validate(config, Path.of("~/symphony_workspaces/ART-1"), true);

        assertTrue(validation.ok());
    }

    @Test
    void validate_acceptsReadOnlyReviewSandbox() {
        ServiceConfig config = new ServiceConfig(new WorkflowDefinition(
                Map.of(
                        "tracker",
                        Map.of("kind", "memory"),
                        "delivery",
                        Map.of("adversarial_review", Map.of("enabled", true))),
                "prompt"));

        var validation = PermissionPreflight.validate(
                config,
                Path.of("/tmp/symphony_workspaces/ART-1"),
                false,
                config.getEffectiveCodexThreadSandbox(),
                config.resolveAdversarialReviewTurnSandboxPolicy(Path.of("/tmp/symphony_workspaces/ART-1"), false));

        assertTrue(validation.ok());
    }
}
