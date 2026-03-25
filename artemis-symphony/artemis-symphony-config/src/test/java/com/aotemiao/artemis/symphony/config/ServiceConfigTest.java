package com.aotemiao.artemis.symphony.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aotemiao.artemis.symphony.core.model.WorkflowDefinition;
import java.nio.file.Path;
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
                Path.of(System.getProperty("java.io.tmpdir"), "symphony_workspaces").toString(),
                config.getWorkspaceRootRaw());
        assertTrue(Boolean.TRUE.equals(rejectPolicy.get("sandbox_approval")));
        assertTrue(Boolean.TRUE.equals(rejectPolicy.get("rules")));
        assertTrue(Boolean.TRUE.equals(rejectPolicy.get("mcp_elicitations")));
    }
}
