package com.aotemiao.artemis.system.client.api;

import com.aotemiao.artemis.system.client.dto.RegisterUserRequest;

/** 用户注册远程服务。 */
public interface UserRegisterService {

    Long register(RegisterUserRequest request);
}
