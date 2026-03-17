package com.aotemiao.artemis.system.app.command;

import com.aotemiao.artemis.system.domain.gateway.UserCredentialsGateway;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 校验用户凭证命令执行器。
 */
@Component
public class ValidateCredentialsCmdExe {

    private final UserCredentialsGateway userCredentialsGateway;

    public ValidateCredentialsCmdExe(UserCredentialsGateway userCredentialsGateway) {
        this.userCredentialsGateway = userCredentialsGateway;
    }

    /**
     * 校验用户名与密码，返回对应用户 ID。
     *
     * @param cmd 用户名与密码
     * @return 校验通过时返回 userId，否则 empty
     */
    public Optional<Long> execute(ValidateCredentialsCmd cmd) {
        return userCredentialsGateway.validate(cmd.username(), cmd.password());
    }
}
