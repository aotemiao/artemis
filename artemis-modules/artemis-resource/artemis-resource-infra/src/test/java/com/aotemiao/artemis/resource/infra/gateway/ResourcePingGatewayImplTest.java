package com.aotemiao.artemis.resource.infra.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ResourcePingGatewayImplTest {

    @Test
    void loadPing_should_return_default_payload() {
        ResourcePingGatewayImpl gateway = new ResourcePingGatewayImpl();

        var result = gateway.loadPing();

        assertThat(result.serviceCode()).isEqualTo("artemis-resource");
        assertThat(result.message()).isEqualTo("Service scaffold is ready");
    }
}
