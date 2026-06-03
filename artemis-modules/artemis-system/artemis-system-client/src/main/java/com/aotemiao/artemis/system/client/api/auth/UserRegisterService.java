package com.aotemiao.artemis.system.client.api.auth;

import com.aotemiao.artemis.system.client.dto.auth.RegisterUserRequest;

/** 用户注册远程服务。 */
public interface UserRegisterService {

    Long register(RegisterUserRequest request);
}
