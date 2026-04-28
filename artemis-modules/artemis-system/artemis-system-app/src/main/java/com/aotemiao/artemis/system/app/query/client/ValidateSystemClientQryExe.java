package com.aotemiao.artemis.system.app.query.client;

import com.aotemiao.artemis.system.domain.gateway.client.SystemClientGateway;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 校验系统客户端授权请求执行器。 */
@Component
public class ValidateSystemClientQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    private final SystemClientGateway systemClientGateway;

    public ValidateSystemClientQryExe(SystemClientGateway systemClientGateway) {
        this.systemClientGateway = systemClientGateway;
    }

    public boolean execute(ValidateSystemClientQry qry) {
        return systemClientGateway
                .findByClientId(qry.clientId())
                .filter(client -> client.isNormal() && client.supportsGrantType(qry.grantType()))
                .isPresent();
    }
}
