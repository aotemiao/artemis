package com.aotemiao.artemis.system.app.command.menu;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.menu.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.gateway.role.RoleMenuBindingGateway;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantPackageGateway;
import com.aotemiao.artemis.system.domain.model.menu.SystemMenu;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/** 级联删除系统菜单及关联授权。 */
@Component
public class DeleteSystemMenuCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateways as managed collaborators; this executor does not expose them.")
    private final SystemMenuGateway systemMenuGateway;

    private final RoleMenuBindingGateway roleMenuBindingGateway;

    private final TenantPackageGateway tenantPackageGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateways as managed collaborators; this executor does not expose them.")
    public DeleteSystemMenuCmdExe(
            SystemMenuGateway systemMenuGateway,
            RoleMenuBindingGateway roleMenuBindingGateway,
            TenantPackageGateway tenantPackageGateway) {
        this.systemMenuGateway = systemMenuGateway;
        this.roleMenuBindingGateway = roleMenuBindingGateway;
        this.tenantPackageGateway = tenantPackageGateway;
    }

    public void execute(DeleteSystemMenuCmd cmd) {
        SystemMenu current = systemMenuGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "SystemMenu not found: " + cmd.id()));
        List<Long> menuIds = collectDescendantIds(current.getId());
        roleMenuBindingGateway.deleteByMenuIds(menuIds);
        tenantPackageGateway.deleteMenuBindingsByMenuIds(menuIds);
        systemMenuGateway.deleteByIds(menuIds);
    }

    private List<Long> collectDescendantIds(Long rootId) {
        Set<Long> ids = new LinkedHashSet<>();
        ids.add(rootId);
        List<SystemMenu> allMenus = systemMenuGateway.findAll();
        boolean changed = true;
        while (changed) {
            changed = false;
            List<Long> knownIds = new ArrayList<>(ids);
            for (SystemMenu menu : allMenus) {
                if (!ids.contains(menu.getId()) && knownIds.contains(menu.getParentId())) {
                    ids.add(menu.getId());
                    changed = true;
                }
            }
        }
        return List.copyOf(ids);
    }
}
