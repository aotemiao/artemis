package com.aotemiao.artemis.resource.infra.gateway.ping;

import com.aotemiao.artemis.resource.domain.gateway.ping.ResourcePingGateway;
import com.aotemiao.artemis.resource.domain.model.ping.ServicePing;
import org.springframework.stereotype.Component;

/** artemis-resource 最小状态网关实现。 */
@Component
public class ResourcePingGatewayImpl implements ResourcePingGateway {

    @Override
    public ServicePing loadPing() {
        return new ServicePing("artemis-resource", "Service scaffold is ready");
    }
}
