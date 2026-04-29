package com.aotemiao.artemis.system.domain.gateway.auth;

import java.util.Optional;

/** 用户凭证校验 Gateway（供认证服务通过 REST 调用后，由本模块 app 层使用）。 校验用户名与密码，返回对应用户 ID。 */
public interface UserCredentialsGateway {

    /**
     * 校验用户名与密码。
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 校验通过时返回用户 ID，否则 empty
     */
    default Optional<Long> validate(String username, String password) {
        return validate("000000", username, password);
    }

    /**
     * 按租户校验用户名与密码。
     *
     * @param tenantNo 租户编号
     * @param username 用户名
     * @param password 明文密码
     * @return 校验通过时返回用户 ID，否则 empty
     */
    Optional<Long> validate(String tenantNo, String username, String password);
}
