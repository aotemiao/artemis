package com.aotemiao.artemis.workflow.app.query.ping;

import com.aotemiao.artemis.workflow.domain.gateway.ping.WorkflowPingGateway;
import com.aotemiao.artemis.workflow.domain.model.ping.ServicePing;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 读取 artemis-workflow 最小状态的查询执行器。 */
@Component
public class GetWorkflowPingQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    private final WorkflowPingGateway pingGateway;

    public GetWorkflowPingQryExe(WorkflowPingGateway pingGateway) {
        this.pingGateway = pingGateway;
    }

    /** 返回服务最小状态。 */
    public ServicePing execute() {
        return pingGateway.loadPing();
    }
}
