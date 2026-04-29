package com.aotemiao.artemis.workflow.client.api;

import com.aotemiao.artemis.workflow.client.dto.PingResponse;

/** artemis-workflow 对内 ping 契约。 */
public interface WorkflowPingService {

    /** 返回当前服务模板的最小状态。 */
    PingResponse ping();
}
