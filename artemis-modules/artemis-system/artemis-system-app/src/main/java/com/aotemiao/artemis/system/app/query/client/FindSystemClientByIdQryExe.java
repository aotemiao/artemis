package com.aotemiao.artemis.system.app.query.client;

import com.aotemiao.artemis.system.domain.gateway.client.SystemClientGateway;
import com.aotemiao.artemis.system.domain.model.client.SystemClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 按 ID 查询系统客户端执行器。 */
@Component
public class FindSystemClientByIdQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    private final SystemClientGateway systemClientGateway;

    public FindSystemClientByIdQryExe(SystemClientGateway systemClientGateway) {
        this.systemClientGateway = systemClientGateway;
    }

    public Optional<SystemClient> execute(FindSystemClientByIdQry qry) {
        return systemClientGateway.findById(qry.id());
    }
}
