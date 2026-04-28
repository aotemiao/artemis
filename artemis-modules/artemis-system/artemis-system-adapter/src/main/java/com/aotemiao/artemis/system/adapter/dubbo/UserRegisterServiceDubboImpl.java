package com.aotemiao.artemis.system.adapter.dubbo;

import com.aotemiao.artemis.system.app.command.auth.RegisterUserCmd;
import com.aotemiao.artemis.system.app.command.auth.RegisterUserCmdExe;
import com.aotemiao.artemis.system.client.api.UserRegisterService;
import com.aotemiao.artemis.system.client.dto.RegisterUserRequest;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class UserRegisterServiceDubboImpl implements UserRegisterService {

    private final RegisterUserCmdExe registerUserCmdExe;

    public UserRegisterServiceDubboImpl(RegisterUserCmdExe registerUserCmdExe) {
        this.registerUserCmdExe = registerUserCmdExe;
    }

    @Override
    public Long register(RegisterUserRequest request) {
        return registerUserCmdExe.execute(new RegisterUserCmd(
                request.tenantId(),
                request.clientId(),
                request.grantType(),
                request.username(),
                request.password(),
                request.userType()));
    }
}
