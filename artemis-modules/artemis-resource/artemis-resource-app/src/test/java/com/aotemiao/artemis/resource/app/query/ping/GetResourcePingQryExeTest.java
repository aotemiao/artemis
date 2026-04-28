package com.aotemiao.artemis.resource.app.query.ping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.resource.domain.gateway.ping.ResourcePingGateway;
import com.aotemiao.artemis.resource.domain.model.ping.ServicePing;
import org.junit.jupiter.api.Test;

class GetResourcePingQryExeTest {

    @Test
    void execute_should_delegate_to_gateway() {
        ResourcePingGateway pingGateway = mock(ResourcePingGateway.class);
        when(pingGateway.loadPing()).thenReturn(new ServicePing("artemis-resource", "Service scaffold is ready"));

        GetResourcePingQryExe exe = new GetResourcePingQryExe(pingGateway);

        ServicePing result = exe.execute();

        assertThat(result.serviceCode()).isEqualTo("artemis-resource");
        assertThat(result.message()).isEqualTo("Service scaffold is ready");
    }
}
