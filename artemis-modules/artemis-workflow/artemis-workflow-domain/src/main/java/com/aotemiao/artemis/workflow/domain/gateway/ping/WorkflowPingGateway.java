package com.aotemiao.artemis.workflow.domain.gateway.ping;

import com.aotemiao.artemis.workflow.domain.model.ping.ServicePing;

/** artemis-workflow 读取最小状态的领域网关。 */
public interface WorkflowPingGateway {

    /** 返回服务模板的最小可用状态。 */
    ServicePing loadPing();
}
