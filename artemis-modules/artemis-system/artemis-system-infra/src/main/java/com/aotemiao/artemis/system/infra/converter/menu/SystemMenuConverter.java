package com.aotemiao.artemis.system.infra.converter.menu;

import com.aotemiao.artemis.system.domain.model.menu.SystemMenu;
import com.aotemiao.artemis.system.infra.dataobject.menu.SystemMenuDO;

/** 系统菜单 Domain / DO 转换器。 */
public final class SystemMenuConverter {

    private SystemMenuConverter() {}

    public static SystemMenuDO toDO(SystemMenu systemMenu) {
        if (systemMenu == null) {
            return null;
        }
        SystemMenuDO systemMenuDO = new SystemMenuDO();
        systemMenuDO.setId(systemMenu.getId());
        systemMenuDO.setParentId(systemMenu.getParentId());
        systemMenuDO.setMenuType(systemMenu.getMenuType());
        systemMenuDO.setMenuName(systemMenu.getMenuName());
        systemMenuDO.setSortOrder(systemMenu.getSortOrder());
        systemMenuDO.setPath(systemMenu.getPath());
        systemMenuDO.setComponent(systemMenu.getComponent());
        systemMenuDO.setPermissionCode(systemMenu.getPermissionCode());
        systemMenuDO.setVisible(systemMenu.isVisible());
        systemMenuDO.setEnabled(systemMenu.isEnabled());
        return systemMenuDO;
    }

    public static SystemMenu toDomain(SystemMenuDO systemMenuDO) {
        if (systemMenuDO == null) {
            return null;
        }
        SystemMenu systemMenu = new SystemMenu();
        systemMenu.setId(systemMenuDO.getId());
        systemMenu.setParentId(systemMenuDO.getParentId());
        systemMenu.setMenuType(systemMenuDO.getMenuType());
        systemMenu.setMenuName(systemMenuDO.getMenuName());
        systemMenu.setSortOrder(systemMenuDO.getSortOrder());
        systemMenu.setPath(systemMenuDO.getPath());
        systemMenu.setComponent(systemMenuDO.getComponent());
        systemMenu.setPermissionCode(systemMenuDO.getPermissionCode());
        systemMenu.setVisible(systemMenuDO.isVisible());
        systemMenu.setEnabled(systemMenuDO.isEnabled());
        return systemMenu;
    }
}
