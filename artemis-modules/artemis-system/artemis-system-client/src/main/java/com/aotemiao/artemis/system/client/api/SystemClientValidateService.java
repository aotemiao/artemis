package com.aotemiao.artemis.system.client.api;

import com.aotemiao.artemis.system.client.dto.ValidateClientRequest;

/** 系统客户端授权校验服务，供认证服务登录前校验客户端状态和授权类型。 */
public interface SystemClientValidateService {

    boolean validate(ValidateClientRequest request);
}
