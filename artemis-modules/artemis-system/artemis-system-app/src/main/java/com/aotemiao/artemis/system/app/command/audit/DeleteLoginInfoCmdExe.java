package com.aotemiao.artemis.system.app.command.audit;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.audit.LoginInfoGateway;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

@Component
public class DeleteLoginInfoCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final LoginInfoGateway loginInfoGateway;

    public DeleteLoginInfoCmdExe(LoginInfoGateway loginInfoGateway) {
        this.loginInfoGateway = loginInfoGateway;
    }

    public void execute(DeleteLoginInfoCmd cmd) {
        if (cmd.ids() == null || cmd.ids().isEmpty()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "LoginInfo ids must not be empty");
        }
        loginInfoGateway.deleteByIds(cmd.ids());
    }
}
