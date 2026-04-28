package com.aotemiao.artemis.system.app.command.menu;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.menu.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.model.menu.SystemMenu;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 修改系统菜单命令执行器。 */
@Component
public class UpdateSystemMenuCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemMenuGateway systemMenuGateway;

    public UpdateSystemMenuCmdExe(SystemMenuGateway systemMenuGateway) {
        this.systemMenuGateway = systemMenuGateway;
    }

    public SystemMenu execute(UpdateSystemMenuCmd cmd) {
        SystemMenu existing = systemMenuGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "SystemMenu not found: " + cmd.id()));

        Long parentId = SystemMenuCommandSupport.normalizeParentId(cmd.parentId());
        String menuType = SystemMenuCommandSupport.normalizeMenuType(cmd.menuType());
        String menuName = SystemMenuCommandSupport.normalizeRequiredText(cmd.menuName(), "menuName");
        String path = SystemMenuCommandSupport.normalizeText(cmd.path());

        SystemMenuCommandSupport.validateParent(systemMenuGateway, parentId, cmd.id());
        SystemMenuCommandSupport.validateUniqueName(systemMenuGateway, parentId, menuName, cmd.id());
        SystemMenuCommandSupport.validateUniquePath(systemMenuGateway, menuType, path, cmd.id());

        existing.setParentId(parentId);
        existing.setMenuType(menuType);
        existing.setMenuName(menuName);
        existing.setSortOrder(SystemMenuCommandSupport.normalizeSortOrder(cmd.sortOrder()));
        existing.setPath(path);
        existing.setComponent(SystemMenuCommandSupport.normalizeText(cmd.component()));
        existing.setPermissionCode(SystemMenuCommandSupport.normalizeText(cmd.permissionCode()));
        existing.setVisible(cmd.visible() == null || cmd.visible());
        existing.setEnabled(cmd.enabled() == null || cmd.enabled());
        return systemMenuGateway.save(existing);
    }
}
