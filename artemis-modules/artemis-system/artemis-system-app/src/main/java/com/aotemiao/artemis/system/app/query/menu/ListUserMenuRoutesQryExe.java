package com.aotemiao.artemis.system.app.query.menu;

import com.aotemiao.artemis.system.domain.gateway.menu.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.gateway.role.RoleMenuBindingGateway;
import com.aotemiao.artemis.system.domain.gateway.user.UserRoleBindingGateway;
import com.aotemiao.artemis.system.domain.model.menu.SystemMenu;
import com.aotemiao.artemis.system.domain.model.role.SystemRole;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** 按用户角色授权生成前端可用路由菜单。 */
@Component
public class ListUserMenuRoutesQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateways as managed collaborators; this executor does not expose them.")
    private final UserRoleBindingGateway userRoleBindingGateway;

    private final RoleMenuBindingGateway roleMenuBindingGateway;

    private final SystemMenuGateway systemMenuGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateways as managed collaborators; this executor does not expose them.")
    public ListUserMenuRoutesQryExe(
            UserRoleBindingGateway userRoleBindingGateway,
            RoleMenuBindingGateway roleMenuBindingGateway,
            SystemMenuGateway systemMenuGateway) {
        this.userRoleBindingGateway = userRoleBindingGateway;
        this.roleMenuBindingGateway = roleMenuBindingGateway;
        this.systemMenuGateway = systemMenuGateway;
    }

    public List<SystemMenu> execute(ListUserMenuRoutesQry qry) {
        List<Long> roleIds = userRoleBindingGateway.findRolesByUserId(qry.userId()).stream()
                .filter(SystemRole::isEnabled)
                .map(SystemRole::getId)
                .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        Map<Long, SystemMenu> allMenusById = systemMenuGateway.findAll().stream()
                .filter(SystemMenu::isEnabled)
                .collect(LinkedHashMap::new, (map, menu) -> map.put(menu.getId(), menu), Map::putAll);
        Map<Long, SystemMenu> routeMenus = new LinkedHashMap<>();
        roleMenuBindingGateway.findMenusByRoleIds(roleIds).stream()
                .filter(SystemMenu::isEnabled)
                .filter(menu -> !menu.isButton())
                .filter(SystemMenu::isVisible)
                .forEach(menu -> addWithAncestors(menu, allMenusById, routeMenus));
        return routeMenus.values().stream()
                .filter(menu -> !menu.isButton())
                .filter(SystemMenu::isVisible)
                .toList();
    }

    private void addWithAncestors(SystemMenu menu, Map<Long, SystemMenu> allMenusById, Map<Long, SystemMenu> result) {
        if (menu.getParentId() != null && menu.getParentId() > 0) {
            SystemMenu parent = allMenusById.get(menu.getParentId());
            if (parent != null && parent.isEnabled()) {
                addWithAncestors(parent, allMenusById, result);
            }
        }
        result.put(menu.getId(), menu);
    }
}
