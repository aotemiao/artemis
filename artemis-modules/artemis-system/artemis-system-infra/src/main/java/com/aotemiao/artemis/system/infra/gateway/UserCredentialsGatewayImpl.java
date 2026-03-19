package com.aotemiao.artemis.system.infra.gateway;

import com.aotemiao.artemis.system.domain.gateway.UserCredentialsGateway;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 用户凭证校验 Gateway 实现（stub）。 当前仅支持固定账号 admin/123456 返回 userId=1，后续可替换为真实用户表与密码校验。 */
@Component
public class UserCredentialsGatewayImpl implements UserCredentialsGateway {

    private static final String STUB_USERNAME = "admin";
    private static final String STUB_PASSWORD = "123456";
    private static final long STUB_USER_ID = 1L;

    @Override
    public Optional<Long> validate(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }
        if (STUB_USERNAME.equals(username) && STUB_PASSWORD.equals(password)) {
            return Optional.of(STUB_USER_ID);
        }
        return Optional.empty();
    }
}
