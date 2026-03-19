package com.aotemiao.artemis.system.adapter.dubbo;

import com.aotemiao.artemis.system.app.command.ValidateCredentialsCmd;
import com.aotemiao.artemis.system.app.command.ValidateCredentialsCmdExe;
import com.aotemiao.artemis.system.client.api.UserValidateService;
import com.aotemiao.artemis.system.client.dto.ValidateCredentialsRequest;
import java.util.Optional;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 用户凭证校验 Dubbo 服务实现，供 artemis-auth 等内部调用。 委托 app 层 ValidateCredentialsCmdExe，与 REST
 * InternalAuthController 等价。
 */
@DubboService
public class UserValidateServiceDubboImpl implements UserValidateService {

    private final ValidateCredentialsCmdExe validateCredentialsCmdExe;

    public UserValidateServiceDubboImpl(ValidateCredentialsCmdExe validateCredentialsCmdExe) {
        this.validateCredentialsCmdExe = validateCredentialsCmdExe;
    }

    @Override
    public Optional<Long> validate(ValidateCredentialsRequest request) {
        var cmd = new ValidateCredentialsCmd(request.username(), request.password());
        return validateCredentialsCmdExe.execute(cmd);
    }
}
