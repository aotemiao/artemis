package com.aotemiao.artemis.system.app.query;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.SystemRoleGateway;
import com.aotemiao.artemis.system.domain.model.SystemRole;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 系统角色分页查询执行器。 */
@Component
public class SystemRolePageQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemRoleGateway systemRoleGateway;

    public SystemRolePageQryExe(SystemRoleGateway systemRoleGateway) {
        this.systemRoleGateway = systemRoleGateway;
    }

    public PageResult<SystemRole> execute(SystemRolePageQry qry) {
        return systemRoleGateway.findPage(qry.pageRequest());
    }
}
