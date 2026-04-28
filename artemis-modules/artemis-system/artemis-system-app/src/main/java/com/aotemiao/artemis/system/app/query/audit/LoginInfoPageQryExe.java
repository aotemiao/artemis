package com.aotemiao.artemis.system.app.query.audit;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.audit.LoginInfoGateway;
import com.aotemiao.artemis.system.domain.model.audit.LoginInfo;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

@Component
public class LoginInfoPageQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final LoginInfoGateway loginInfoGateway;

    public LoginInfoPageQryExe(LoginInfoGateway loginInfoGateway) {
        this.loginInfoGateway = loginInfoGateway;
    }

    public PageResult<LoginInfo> execute(LoginInfoPageQry qry) {
        return loginInfoGateway.findPage(qry.pageRequest());
    }
}
