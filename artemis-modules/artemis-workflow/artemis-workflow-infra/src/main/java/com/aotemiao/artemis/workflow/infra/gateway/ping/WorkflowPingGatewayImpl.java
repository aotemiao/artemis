package com.aotemiao.artemis.workflow.infra.gateway.ping;

import com.aotemiao.artemis.workflow.domain.gateway.ping.WorkflowPingGateway;
import com.aotemiao.artemis.workflow.domain.model.ping.ServicePing;
import org.springframework.stereotype.Component;

/** artemis-workflow 最小状态网关实现。 */
@Component
public class WorkflowPingGatewayImpl implements WorkflowPingGateway {

    @Override
    public ServicePing loadPing() {
        return new ServicePing("artemis-workflow", "Service scaffold is ready");
    }
}
