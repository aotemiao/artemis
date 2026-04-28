package com.aotemiao.artemis.system.app.query.menu;

import com.aotemiao.artemis.system.domain.gateway.RoleMenuBindingGateway;
import com.aotemiao.artemis.system.domain.model.SystemMenu;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.springframework.stereotype.Component;

/** 查询角色菜单绑定执行器。 */
@Component
public class ListRoleMenusQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final RoleMenuBindingGateway roleMenuBindingGateway;

    public ListRoleMenusQryExe(RoleMenuBindingGateway roleMenuBindingGateway) {
        this.roleMenuBindingGateway = roleMenuBindingGateway;
    }

    public List<SystemMenu> execute(ListRoleMenusQry qry) {
        return roleMenuBindingGateway.findMenusByRoleId(qry.roleId());
    }
}
