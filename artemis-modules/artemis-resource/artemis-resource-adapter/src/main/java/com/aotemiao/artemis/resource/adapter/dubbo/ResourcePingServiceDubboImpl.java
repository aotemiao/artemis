package com.aotemiao.artemis.resource.adapter.dubbo;

import com.aotemiao.artemis.resource.app.query.GetResourcePingQryExe;
import com.aotemiao.artemis.resource.client.api.ResourcePingService;
import com.aotemiao.artemis.resource.client.dto.PingResponse;
import org.apache.dubbo.config.annotation.DubboService;

/** artemis-resource 的 Dubbo ping 实现。 */
@DubboService
public class ResourcePingServiceDubboImpl implements ResourcePingService {

    private final GetResourcePingQryExe getResourcePingQryExe;

    public ResourcePingServiceDubboImpl(GetResourcePingQryExe getResourcePingQryExe) {
        this.getResourcePingQryExe = getResourcePingQryExe;
    }

    @Override
    public PingResponse ping() {
        var ping = getResourcePingQryExe.execute();
        return new PingResponse(ping.serviceCode(), ping.message());
    }
}
