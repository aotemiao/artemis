package com.aotemiao.artemis.auth.client;

import com.aotemiao.artemis.system.client.api.auth.UserRegisterService;
import com.aotemiao.artemis.system.client.dto.auth.RegisterUserRequest;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/** 调用 artemis-system 的用户注册能力。 */
@Component
public class SystemUserRegisterClient {

    @DubboReference
    private UserRegisterService userRegisterService;

    public Long register(RegisterUserRequest request) {
        return userRegisterService.register(request);
    }
}
