package com.aotemiao.artemis.resource.infra.gateway;

import com.aotemiao.artemis.resource.domain.gateway.ResourcePingGateway;
import com.aotemiao.artemis.resource.domain.model.ServicePing;
import org.springframework.stereotype.Component;

/** artemis-resource 最小状态网关实现。 */
@Component
public class ResourcePingGatewayImpl implements ResourcePingGateway {

    @Override
    public ServicePing loadPing() {
        return new ServicePing("artemis-resource", "Service scaffold is ready");
    }
}
