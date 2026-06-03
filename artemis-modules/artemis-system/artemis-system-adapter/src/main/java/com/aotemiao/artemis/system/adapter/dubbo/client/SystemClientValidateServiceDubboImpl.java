package com.aotemiao.artemis.system.adapter.dubbo.client;

import com.aotemiao.artemis.system.app.query.client.ValidateSystemClientQry;
import com.aotemiao.artemis.system.app.query.client.ValidateSystemClientQryExe;
import com.aotemiao.artemis.system.client.api.client.SystemClientValidateService;
import com.aotemiao.artemis.system.client.dto.client.ValidateClientRequest;
import org.apache.dubbo.config.annotation.DubboService;

/** 系统客户端授权校验 Dubbo 服务实现。 */
@DubboService
public class SystemClientValidateServiceDubboImpl implements SystemClientValidateService {

    private final ValidateSystemClientQryExe validateSystemClientQryExe;

    public SystemClientValidateServiceDubboImpl(ValidateSystemClientQryExe validateSystemClientQryExe) {
        this.validateSystemClientQryExe = validateSystemClientQryExe;
    }

    @Override
    public boolean validate(ValidateClientRequest request) {
        return validateSystemClientQryExe.execute(new ValidateSystemClientQry(request.clientId(), request.grantType()));
    }
}
