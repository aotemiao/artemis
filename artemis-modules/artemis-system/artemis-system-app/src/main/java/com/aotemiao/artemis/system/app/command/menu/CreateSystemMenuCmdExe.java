package com.aotemiao.artemis.system.app.command.menu;

import com.aotemiao.artemis.system.domain.gateway.menu.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.model.menu.SystemMenu;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 新增系统菜单命令执行器。 */
@Component
public class CreateSystemMenuCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemMenuGateway systemMenuGateway;

    public CreateSystemMenuCmdExe(SystemMenuGateway systemMenuGateway) {
        this.systemMenuGateway = systemMenuGateway;
    }

    public SystemMenu execute(CreateSystemMenuCmd cmd) {
        Long parentId = SystemMenuCommandSupport.normalizeParentId(cmd.parentId());
        String menuType = SystemMenuCommandSupport.normalizeMenuType(cmd.menuType());
        String menuName = SystemMenuCommandSupport.normalizeRequiredText(cmd.menuName(), "menuName");
        String path = SystemMenuCommandSupport.normalizeText(cmd.path());

        SystemMenuCommandSupport.validateParent(systemMenuGateway, parentId, null);
        SystemMenuCommandSupport.validateUniqueName(systemMenuGateway, parentId, menuName, null);
        SystemMenuCommandSupport.validateUniquePath(systemMenuGateway, menuType, path, null);

        SystemMenu systemMenu = new SystemMenu();
        systemMenu.setParentId(parentId);
        systemMenu.setMenuType(menuType);
        systemMenu.setMenuName(menuName);
        systemMenu.setSortOrder(SystemMenuCommandSupport.normalizeSortOrder(cmd.sortOrder()));
        systemMenu.setPath(path);
        systemMenu.setComponent(SystemMenuCommandSupport.normalizeText(cmd.component()));
        systemMenu.setPermissionCode(SystemMenuCommandSupport.normalizeText(cmd.permissionCode()));
        systemMenu.setVisible(cmd.visible() == null || cmd.visible());
        systemMenu.setEnabled(true);
        return systemMenuGateway.save(systemMenu);
    }
}
