package com.aotemiao.artemis.workflow.client.api.ping;

import com.aotemiao.artemis.workflow.client.dto.ping.PingResponse;

/** artemis-workflow 对内 ping 契约。 */
public interface WorkflowPingService {

    /** 返回当前服务模板的最小状态。 */
    PingResponse ping();
}
