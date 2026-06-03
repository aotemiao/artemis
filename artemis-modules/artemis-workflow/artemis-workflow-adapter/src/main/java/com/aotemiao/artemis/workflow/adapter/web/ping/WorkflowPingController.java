package com.aotemiao.artemis.workflow.adapter.web.ping;

import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.workflow.app.query.ping.GetWorkflowPingQryExe;
import com.aotemiao.artemis.workflow.client.dto.ping.PingResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** artemis-workflow 对外 ping 接口。 */
@RestController
@RequestMapping(WorkflowPingController.BASE_PATH)
public class WorkflowPingController {

    public static final String BASE_PATH = "/api/workflow/ping";

    private final GetWorkflowPingQryExe getWorkflowPingQryExe;

    public WorkflowPingController(GetWorkflowPingQryExe getWorkflowPingQryExe) {
        this.getWorkflowPingQryExe = getWorkflowPingQryExe;
    }

    /** 返回服务模板的最小状态。 */
    @GetMapping
    public R<PingResponse> ping() {
        var ping = getWorkflowPingQryExe.execute();
        return R.ok(new PingResponse(ping.serviceCode(), "flow-category", ping.message()));
    }
}
