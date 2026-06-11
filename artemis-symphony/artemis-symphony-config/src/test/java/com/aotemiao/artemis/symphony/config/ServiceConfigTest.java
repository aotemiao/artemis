package com.aotemiao.artemis.symphony.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aotemiao.artemis.symphony.core.model.WorkflowDefinition;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ServiceConfigTest {

    @Test
    void defaults_matchReferenceWorkflowSemantics() {
        ServiceConfig config = new ServiceConfig(new WorkflowDefinition(Map.of(), "prompt"));

        Object approvalPolicy = config.getCodexApprovalPolicy();
        Map<?, ?> approvalPolicyMap = assertInstanceOf(Map.class, approvalPolicy);
        Map<?, ?> rejectPolicy = assertInstanceOf(Map.class, approvalPolicyMap.get("reject"));

        assertEquals(
                Path.of(System.getProperty("java.io.tmpdir"), "symphony_workspaces")
                        .toString(),
                config.getWorkspaceRootRaw());
        assertTrue(Boolean.TRUE.equals(rejectPolicy.get("sandbox_approval")));
        assertTrue(Boolean.TRUE.equals(rejectPolicy.get("rules")));
        assertTrue(Boolean.TRUE.equals(rejectPolicy.get("mcp_elicitations")));
        assertFalse(config.isSpecDrivenDeliveryEnabled());
        assertTrue(config.isAgentRunSummaryEnabled());
        assertEquals(Path.of("artifacts/agent-runs"), config.getAgentRunSummaryDirectory());
        assertEquals("workspace-write", config.getEffectiveCodexThreadSandbox());
        assertFalse(config.isDangerFullAccessAllowed());
        assertEquals(List.of(), config.getAllowedWritableRoots());
        assertTrue(config.getSpecDrivenDeliveryRequiredAssets().contains("docs/security/THREAT_MODEL.md"));
        assertTrue(config.getSpecDrivenDeliveryRequiredAssets().contains("artemis-symphony/tools/registry.json"));
    }

    @Test
    void specDrivenDelivery_readsEnabledAssetsAndPromptAddon() {
        ServiceConfig config = new ServiceConfig(new WorkflowDefinition(
                Map.of(
                        "delivery",
                        Map.of(
                                "spec_driven",
                                Map.of(
                                        "enabled",
                                        true,
                                        "required_assets",
                                        List.of(
                                                "docs/feature-specs/README.md",
                                                "scripts/harness/check-feature-specs.sh")))),
                "prompt"));

        assertTrue(config.isSpecDrivenDeliveryEnabled());
        assertEquals(
                List.of("docs/feature-specs/README.md", "scripts/harness/check-feature-specs.sh"),
                config.getSpecDrivenDeliveryRequiredAssets());
        assertTrue(config.getSpecDrivenDeliveryPromptAddon().contains("Spec-driven delivery guidance"));
        assertTrue(config.getSpecDrivenDeliveryPromptAddon().contains("docs/feature-specs/README.md"));
    }

    @Test
    void adversarialReview_readsEnabledFilterAndReadOnlySandbox() {
        ServiceConfig config = new ServiceConfig(new WorkflowDefinition(
                Map.of(
                        "delivery",
                        Map.of(
                                "adversarial_review",
                                Map.of(
                                        "enabled",
                                        true,
                                        "issue_title_regex",
                                        "RBAC",
                                        "risk_labels",
                                        List.of("security")))),
                "prompt"));

        assertTrue(config.isAdversarialReviewEnabled());
        assertEquals("RBAC", config.getAdversarialReviewIssueTitleRegex());
        assertEquals(List.of("security"), config.getAdversarialReviewRiskLabels());
        Object sandbox = config.resolveAdversarialReviewTurnSandboxPolicy(Path.of("/tmp/workspace"), false);
        Map<?, ?> sandboxMap = assertInstanceOf(Map.class, sandbox);
        assertEquals("readOnly", sandboxMap.get("type"));
        assertEquals(false, sandboxMap.get("networkAccess"));
    }

    @Test
    void agentRunSummary_readsEnabledAndDirectory() {
        ServiceConfig config = new ServiceConfig(new WorkflowDefinition(
                Map.of("reporting", Map.of("agent_runs", Map.of("enabled", false, "directory", "./tmp/agent-runs"))),
                "prompt"));

        assertFalse(config.isAgentRunSummaryEnabled());
        assertEquals(Path.of("./tmp/agent-runs").normalize(), config.getAgentRunSummaryDirectory());
    }

    @Test
    void permissions_readsAuditGuardrails() {
        ServiceConfig config = new ServiceConfig(new WorkflowDefinition(
                Map.of(
                        "codex",
                        Map.of("thread_sandbox", "none"),
                        "permissions",
                        Map.of(
                                "allow_danger_full_access",
                                true,
                                "network_access_reason",
                                "download public build dependencies",
                                "allowed_writable_roots",
                                List.of("~/shared-cache"))),
                "prompt"));

        assertEquals("danger-full-access", config.getEffectiveCodexThreadSandbox());
        assertTrue(config.isDangerFullAccessAllowed());
        assertEquals("download public build dependencies", config.getNetworkAccessReason());
        assertEquals(
                List.of(Path.of(System.getProperty("user.home"), "shared-cache").toString()),
                config.getAllowedWritableRoots());
    }
}
