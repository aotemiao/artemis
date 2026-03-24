package com.aotemiao.artemis.system.app.command;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.SystemRoleGateway;
import com.aotemiao.artemis.system.domain.gateway.SystemUserGateway;
import com.aotemiao.artemis.system.domain.gateway.UserRoleBindingGateway;
import com.aotemiao.artemis.system.domain.model.SystemRole;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

/** 批量替换用户角色绑定命令执行器。 */
@Component
public class ReplaceUserRolesCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this executor does not expose them.")
    private final SystemUserGateway systemUserGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this executor does not expose them.")
    private final SystemRoleGateway systemRoleGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this executor does not expose them.")
    private final UserRoleBindingGateway userRoleBindingGateway;

    public ReplaceUserRolesCmdExe(
            SystemUserGateway systemUserGateway,
            SystemRoleGateway systemRoleGateway,
            UserRoleBindingGateway userRoleBindingGateway) {
        this.systemUserGateway = systemUserGateway;
        this.systemRoleGateway = systemRoleGateway;
        this.userRoleBindingGateway = userRoleBindingGateway;
    }

    public List<SystemRole> execute(ReplaceUserRolesCmd cmd) {
        systemUserGateway
                .findById(cmd.userId())
                .orElseThrow(
                        () -> new BizException(CommonErrorCode.NOT_FOUND, "SystemUser not found: " + cmd.userId()));

        List<Long> roleIds = cmd.roleIds() == null
                ? List.of()
                : cmd.roleIds().stream().filter(Objects::nonNull).distinct().toList();

        if (!roleIds.isEmpty() && systemRoleGateway.findByIds(roleIds).size() != roleIds.size()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Some roleIds do not exist: " + roleIds);
        }

        userRoleBindingGateway.replaceRoles(cmd.userId(), roleIds);
        return userRoleBindingGateway.findRolesByUserId(cmd.userId());
    }
}
