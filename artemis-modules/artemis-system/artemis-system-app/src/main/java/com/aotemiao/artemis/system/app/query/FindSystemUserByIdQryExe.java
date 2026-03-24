package com.aotemiao.artemis.system.app.query;

import com.aotemiao.artemis.system.domain.gateway.SystemUserGateway;
import com.aotemiao.artemis.system.domain.model.SystemUser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 按 ID 查询系统用户执行器。 */
@Component
public class FindSystemUserByIdQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemUserGateway systemUserGateway;

    public FindSystemUserByIdQryExe(SystemUserGateway systemUserGateway) {
        this.systemUserGateway = systemUserGateway;
    }

    public Optional<SystemUser> execute(FindSystemUserByIdQry qry) {
        return systemUserGateway.findById(qry.id());
    }
}
