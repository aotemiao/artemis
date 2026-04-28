package com.aotemiao.artemis.system.app.command;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.RoleMenuBindingGateway;
import com.aotemiao.artemis.system.domain.gateway.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.gateway.SystemRoleGateway;
import com.aotemiao.artemis.system.domain.model.SystemMenu;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

/** 批量替换角色菜单绑定命令执行器。 */
@Component
public class ReplaceRoleMenusCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this executor does not expose them.")
    private final SystemRoleGateway systemRoleGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this executor does not expose them.")
    private final SystemMenuGateway systemMenuGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this executor does not expose them.")
    private final RoleMenuBindingGateway roleMenuBindingGateway;

    public ReplaceRoleMenusCmdExe(
            SystemRoleGateway systemRoleGateway,
            SystemMenuGateway systemMenuGateway,
            RoleMenuBindingGateway roleMenuBindingGateway) {
        this.systemRoleGateway = systemRoleGateway;
        this.systemMenuGateway = systemMenuGateway;
        this.roleMenuBindingGateway = roleMenuBindingGateway;
    }

    public List<SystemMenu> execute(ReplaceRoleMenusCmd cmd) {
        systemRoleGateway
                .findById(cmd.roleId())
                .orElseThrow(
                        () -> new BizException(CommonErrorCode.NOT_FOUND, "SystemRole not found: " + cmd.roleId()));

        List<Long> menuIds =
                cmd.menuIds().stream().filter(Objects::nonNull).distinct().toList();
        if (!menuIds.isEmpty() && systemMenuGateway.findByIds(menuIds).size() != menuIds.size()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Some menuIds do not exist: " + menuIds);
        }

        roleMenuBindingGateway.replaceMenus(cmd.roleId(), menuIds);
        return roleMenuBindingGateway.findMenusByRoleId(cmd.roleId());
    }
}
