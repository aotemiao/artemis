package com.aotemiao.artemis.system.domain.gateway.role;

import com.aotemiao.artemis.system.domain.model.menu.SystemMenu;
import java.util.Collection;
import java.util.List;

/** 角色与菜单权限绑定 Gateway。 */
public interface RoleMenuBindingGateway {

    List<SystemMenu> findMenusByRoleId(Long roleId);

    List<SystemMenu> findMenusByRoleIds(Collection<Long> roleIds);

    void replaceMenus(Long roleId, List<Long> menuIds);

    void deleteByMenuIds(Collection<Long> menuIds);
}
