package com.aotemiao.artemis.system.adapter.dubbo.audit;

import com.aotemiao.artemis.system.app.command.audit.RecordLoginInfoCmd;
import com.aotemiao.artemis.system.app.command.audit.RecordLoginInfoCmdExe;
import com.aotemiao.artemis.system.client.api.audit.LoginInfoRecordService;
import com.aotemiao.artemis.system.client.dto.audit.RecordLoginInfoRequest;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class LoginInfoRecordServiceDubboImpl implements LoginInfoRecordService {

    private final RecordLoginInfoCmdExe recordLoginInfoCmdExe;

    public LoginInfoRecordServiceDubboImpl(RecordLoginInfoCmdExe recordLoginInfoCmdExe) {
        this.recordLoginInfoCmdExe = recordLoginInfoCmdExe;
    }

    @Override
    public void record(RecordLoginInfoRequest request) {
        recordLoginInfoCmdExe.execute(new RecordLoginInfoCmd(
                request.tenantId(),
                request.username(),
                request.clientId(),
                request.deviceType(),
                request.ipaddr(),
                request.loginLocation(),
                request.browser(),
                request.os(),
                request.status(),
                request.msg()));
    }
}
