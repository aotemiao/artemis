package com.aotemiao.artemis.resource.domain.gateway;

import com.aotemiao.artemis.resource.domain.model.ServicePing;

/** artemis-resource 读取最小状态的领域网关。 */
public interface ResourcePingGateway {

    /** 返回服务模板的最小可用状态。 */
    ServicePing loadPing();
}
