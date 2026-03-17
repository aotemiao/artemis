package com.aotemiao.artemis.auth.client;

import com.aotemiao.artemis.system.client.api.UserValidateService;
import com.aotemiao.artemis.system.client.dto.ValidateCredentialsRequest;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 调用 artemis-system 的用户校验能力（通过 Dubbo）。
 * 契约见 artemis-system-client 的 UserValidateService 与 openspec 文档。
 */
@Component
public class SystemUserValidateClient {

    @DubboReference
    private UserValidateService userValidateService;

    /**
     * 校验用户名与密码，返回用户 ID。
     *
     * @param username 用户名
     * @param password 密码
     * @return 校验通过时返回 userId，否则 empty
     */
    public Optional<Long> validate(String username, String password) {
        return userValidateService.validate(new ValidateCredentialsRequest(username, password));
    }
}
