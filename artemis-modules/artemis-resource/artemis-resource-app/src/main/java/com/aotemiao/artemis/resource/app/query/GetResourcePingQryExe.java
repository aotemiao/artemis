package com.aotemiao.artemis.resource.app.query;

import com.aotemiao.artemis.resource.domain.gateway.ResourcePingGateway;
import com.aotemiao.artemis.resource.domain.model.ServicePing;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 读取 artemis-resource 最小状态的查询执行器。 */
@Component
public class GetResourcePingQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    private final ResourcePingGateway pingGateway;

    public GetResourcePingQryExe(ResourcePingGateway pingGateway) {
        this.pingGateway = pingGateway;
    }

    /** 返回服务最小状态。 */
    public ServicePing execute() {
        return pingGateway.loadPing();
    }
}
