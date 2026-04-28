package com.aotemiao.artemis.system.app.command.audit;

import com.aotemiao.artemis.system.domain.gateway.audit.LoginInfoGateway;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

@Component
public class ClearLoginInfoCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final LoginInfoGateway loginInfoGateway;

    public ClearLoginInfoCmdExe(LoginInfoGateway loginInfoGateway) {
        this.loginInfoGateway = loginInfoGateway;
    }

    public void execute(ClearLoginInfoCmd cmd) {
        loginInfoGateway.clear();
    }
}
