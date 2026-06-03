package com.aotemiao.artemis.auth.client;

import com.aotemiao.artemis.system.client.api.client.SystemClientValidateService;
import com.aotemiao.artemis.system.client.dto.client.ValidateClientRequest;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/** 调用 artemis-system 的客户端授权校验能力。 */
@Component
public class SystemClientValidateClient {

    @DubboReference
    private SystemClientValidateService systemClientValidateService;

    public boolean validate(String clientId, String grantType) {
        return systemClientValidateService.validate(new ValidateClientRequest(clientId, grantType));
    }
}
