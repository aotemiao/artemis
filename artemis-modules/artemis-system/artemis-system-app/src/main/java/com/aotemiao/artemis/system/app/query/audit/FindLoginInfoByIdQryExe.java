package com.aotemiao.artemis.system.app.query.audit;

import com.aotemiao.artemis.system.domain.gateway.audit.LoginInfoGateway;
import com.aotemiao.artemis.system.domain.model.audit.LoginInfo;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class FindLoginInfoByIdQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final LoginInfoGateway loginInfoGateway;

    public FindLoginInfoByIdQryExe(LoginInfoGateway loginInfoGateway) {
        this.loginInfoGateway = loginInfoGateway;
    }

    public Optional<LoginInfo> execute(FindLoginInfoByIdQry qry) {
        return loginInfoGateway.findById(qry.id());
    }
}
