package com.aotemiao.artemis.workflow.app.query.ping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.workflow.domain.gateway.ping.WorkflowPingGateway;
import com.aotemiao.artemis.workflow.domain.model.ping.ServicePing;
import org.junit.jupiter.api.Test;

class GetWorkflowPingQryExeTest {

    @Test
    void execute_should_delegate_to_gateway() {
        WorkflowPingGateway pingGateway = mock(WorkflowPingGateway.class);
        when(pingGateway.loadPing()).thenReturn(new ServicePing("artemis-workflow", "Service scaffold is ready"));

        GetWorkflowPingQryExe exe = new GetWorkflowPingQryExe(pingGateway);

        ServicePing result = exe.execute();

        assertThat(result.serviceCode()).isEqualTo("artemis-workflow");
        assertThat(result.message()).isEqualTo("Service scaffold is ready");
    }
}
