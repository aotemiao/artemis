package com.aotemiao.artemis.system.app.query;

import com.aotemiao.artemis.system.domain.gateway.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.model.SystemMenu;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 按 ID 查询系统菜单执行器。 */
@Component
public class FindSystemMenuByIdQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemMenuGateway systemMenuGateway;

    public FindSystemMenuByIdQryExe(SystemMenuGateway systemMenuGateway) {
        this.systemMenuGateway = systemMenuGateway;
    }

    public Optional<SystemMenu> execute(FindSystemMenuByIdQry qry) {
        return systemMenuGateway.findById(qry.id());
    }
}
