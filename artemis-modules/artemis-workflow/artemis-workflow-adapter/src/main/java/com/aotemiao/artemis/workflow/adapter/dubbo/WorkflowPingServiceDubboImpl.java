package com.aotemiao.artemis.workflow.adapter.dubbo;

import com.aotemiao.artemis.workflow.app.query.ping.GetWorkflowPingQryExe;
import com.aotemiao.artemis.workflow.client.api.WorkflowPingService;
import com.aotemiao.artemis.workflow.client.dto.PingResponse;
import org.apache.dubbo.config.annotation.DubboService;

/** artemis-workflow 的 Dubbo ping 实现。 */
@DubboService
public class WorkflowPingServiceDubboImpl implements WorkflowPingService {

    private final GetWorkflowPingQryExe getWorkflowPingQryExe;

    public WorkflowPingServiceDubboImpl(GetWorkflowPingQryExe getWorkflowPingQryExe) {
        this.getWorkflowPingQryExe = getWorkflowPingQryExe;
    }

    @Override
    public PingResponse ping() {
        var ping = getWorkflowPingQryExe.execute();
        return new PingResponse(ping.serviceCode(), "flow-category", ping.message());
    }
}
