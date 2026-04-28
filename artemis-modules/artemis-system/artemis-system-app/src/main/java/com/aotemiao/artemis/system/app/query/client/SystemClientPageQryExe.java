package com.aotemiao.artemis.system.app.query.client;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.client.SystemClientGateway;
import com.aotemiao.artemis.system.domain.model.client.SystemClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 分页查询系统客户端执行器。 */
@Component
public class SystemClientPageQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    private final SystemClientGateway systemClientGateway;

    public SystemClientPageQryExe(SystemClientGateway systemClientGateway) {
        this.systemClientGateway = systemClientGateway;
    }

    public PageResult<SystemClient> execute(SystemClientPageQry qry) {
        return systemClientGateway.findPage(qry.pageRequest());
    }
}
