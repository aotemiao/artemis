package com.aotemiao.artemis.resource.client.api.ping;

import com.aotemiao.artemis.resource.client.dto.ping.PingResponse;

/** artemis-resource 对内 ping 契约。 */
public interface ResourcePingService {

    /** 返回当前服务模板的最小状态。 */
    PingResponse ping();
}
