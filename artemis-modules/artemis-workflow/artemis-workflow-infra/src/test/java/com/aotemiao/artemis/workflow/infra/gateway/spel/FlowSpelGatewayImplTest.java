package com.aotemiao.artemis.workflow.infra.gateway.spel;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.workflow.domain.model.spel.FlowSpel;
import com.aotemiao.artemis.workflow.infra.converter.spel.FlowSpelConverter;
import com.aotemiao.artemis.workflow.infra.dataobject.spel.FlowSpelDO;
import org.junit.jupiter.api.Test;

class FlowSpelGatewayImplTest {

    @Test
    void converter_should_round_trip_spel_fields() {
        FlowSpel spel = new FlowSpel();
        spel.setId(1L);
        spel.setComponentName("starter");
        spel.setMethodName("userId");
        spel.setParameters("$userId");
        spel.setPreviewExpression("#{starter.userId($userId)}");
        spel.setRemarks("remarks");
        spel.setStatus(1);

        FlowSpelDO entity = FlowSpelConverter.toDO(spel);
        FlowSpel converted = FlowSpelConverter.toDomain(entity);

        assertThat(converted.getId()).isEqualTo(1L);
        assertThat(converted.getComponentName()).isEqualTo("starter");
        assertThat(converted.getMethodName()).isEqualTo("userId");
        assertThat(converted.getParameters()).isEqualTo("$userId");
        assertThat(converted.getPreviewExpression()).isEqualTo("#{starter.userId($userId)}");
        assertThat(converted.getStatus()).isEqualTo(1);
    }
}
