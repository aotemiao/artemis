package com.aotemiao.artemis.system.app.command;

import com.aotemiao.artemis.system.domain.gateway.UserCredentialsGateway;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 校验用户凭证命令执行器。 */
@Component
public class ValidateCredentialsCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
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
