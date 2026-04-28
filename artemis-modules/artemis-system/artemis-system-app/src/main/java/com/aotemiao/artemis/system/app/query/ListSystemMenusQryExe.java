package com.aotemiao.artemis.system.app.query;

import com.aotemiao.artemis.system.domain.gateway.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.model.SystemMenu;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.springframework.stereotype.Component;

/** 查询系统菜单列表执行器。 */
@Component
public class ListSystemMenusQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemMenuGateway systemMenuGateway;

    public ListSystemMenusQryExe(SystemMenuGateway systemMenuGateway) {
        this.systemMenuGateway = systemMenuGateway;
    }

    public List<SystemMenu> execute(ListSystemMenusQry qry) {
        return systemMenuGateway.findAll();
    }
}
