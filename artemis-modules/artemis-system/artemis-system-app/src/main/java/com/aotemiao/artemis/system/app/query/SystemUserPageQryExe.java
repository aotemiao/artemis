package com.aotemiao.artemis.system.app.query;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.SystemUserGateway;
import com.aotemiao.artemis.system.domain.model.SystemUser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 系统用户分页查询执行器。 */
@Component
public class SystemUserPageQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemUserGateway systemUserGateway;

    public SystemUserPageQryExe(SystemUserGateway systemUserGateway) {
        this.systemUserGateway = systemUserGateway;
    }

    public PageResult<SystemUser> execute(SystemUserPageQry qry) {
        return systemUserGateway.findPage(qry.pageRequest());
    }
}
