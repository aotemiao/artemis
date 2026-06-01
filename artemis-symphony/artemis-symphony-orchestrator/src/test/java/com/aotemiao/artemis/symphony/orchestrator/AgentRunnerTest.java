package com.aotemiao.artemis.symphony.orchestrator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aotemiao.artemis.symphony.config.ServiceConfig;
import com.aotemiao.artemis.symphony.core.model.WorkflowDefinition;
import java.util.List;
import java.util.Map;
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
}
