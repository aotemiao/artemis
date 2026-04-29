package com.aotemiao.artemis.workflow.infra.gateway.definition;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.workflow.domain.model.definition.FlowDefinition;
import com.aotemiao.artemis.workflow.infra.converter.definition.FlowDefinitionConverter;
import com.aotemiao.artemis.workflow.infra.dataobject.definition.FlowDefinitionDO;
import org.junit.jupiter.api.Test;

class FlowDefinitionGatewayImplTest {

    @Test
    void converter_should_round_trip_definition_fields() {
        FlowDefinition definition = new FlowDefinition();
        definition.setId(1L);
        definition.setFlowCode("leave");
        definition.setFlowName("Leave");
        definition.setModelType("JSON");
        definition.setCategoryId(2L);
        definition.setVersion(1);
        definition.setPublishStatus(0);
        definition.setCustomForm(true);
        definition.setFormPath("/leave/form");
        definition.setActiveStatus(1);
        definition.setListener("listener");
        definition.setExtJson("{}");
        definition.setTenantId("000000");
        definition.setDefinitionJson("{\"assigneeConfig\":{}}");
        definition.setDefinitionXml("<process />");

        FlowDefinitionDO entity = FlowDefinitionConverter.toDO(definition);
        FlowDefinition converted = FlowDefinitionConverter.toDomain(entity);

        assertThat(converted.getId()).isEqualTo(1L);
        assertThat(converted.getFlowCode()).isEqualTo("leave");
        assertThat(converted.getFlowName()).isEqualTo("Leave");
        assertThat(converted.getCategoryId()).isEqualTo(2L);
        assertThat(converted.getCustomForm()).isTrue();
        assertThat(converted.getDefinitionJson()).contains("assigneeConfig");
    }
}
