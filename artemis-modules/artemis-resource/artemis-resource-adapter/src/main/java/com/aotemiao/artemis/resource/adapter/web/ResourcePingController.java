package com.aotemiao.artemis.resource.adapter.web;

import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.resource.app.query.GetResourcePingQryExe;
import com.aotemiao.artemis.resource.client.dto.PingResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** artemis-resource 对外 ping 接口。 */
@RestController
@RequestMapping(ResourcePingController.BASE_PATH)
public class ResourcePingController {

    public static final String BASE_PATH = "/api/resource/ping";

    private final GetResourcePingQryExe getResourcePingQryExe;

    public ResourcePingController(GetResourcePingQryExe getResourcePingQryExe) {
        this.getResourcePingQryExe = getResourcePingQryExe;
    }

    /** 返回服务模板的最小状态。 */
    @GetMapping
    public R<PingResponse> ping() {
        var ping = getResourcePingQryExe.execute();
        return R.ok(new PingResponse(ping.serviceCode(), ping.message()));
    }
}
