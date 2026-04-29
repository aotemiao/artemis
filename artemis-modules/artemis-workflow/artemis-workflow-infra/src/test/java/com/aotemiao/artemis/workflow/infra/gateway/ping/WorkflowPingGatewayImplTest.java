package com.aotemiao.artemis.workflow.infra.gateway.ping;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WorkflowPingGatewayImplTest {

    @Test
    void loadPing_should_return_default_payload() {
        WorkflowPingGatewayImpl gateway = new WorkflowPingGatewayImpl();

        var result = gateway.loadPing();

        assertThat(result.serviceCode()).isEqualTo("artemis-workflow");
        assertThat(result.message()).isEqualTo("Service scaffold is ready");
    }
}
