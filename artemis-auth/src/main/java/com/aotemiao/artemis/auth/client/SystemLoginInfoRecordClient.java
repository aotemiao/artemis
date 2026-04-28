package com.aotemiao.artemis.auth.client;

import com.aotemiao.artemis.system.client.api.LoginInfoRecordService;
import com.aotemiao.artemis.system.client.dto.RecordLoginInfoRequest;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/** 调用 artemis-system 写入登录访问日志。 */
@Component
public class SystemLoginInfoRecordClient {

    @DubboReference
    private LoginInfoRecordService loginInfoRecordService;

    public void record(RecordLoginInfoRequest request) {
        loginInfoRecordService.record(request);
    }
}
