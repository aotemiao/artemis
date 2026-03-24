package com.aotemiao.artemis.system.app.command;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.SystemRoleGateway;
import com.aotemiao.artemis.system.domain.model.SystemRole;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 更新系统角色命令执行器。 */
@Component
public class UpdateSystemRoleCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemRoleGateway systemRoleGateway;

    public UpdateSystemRoleCmdExe(SystemRoleGateway systemRoleGateway) {
        this.systemRoleGateway = systemRoleGateway;
    }

    public SystemRole execute(UpdateSystemRoleCmd cmd) {
        SystemRole systemRole = systemRoleGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "SystemRole not found: " + cmd.id()));

        systemRoleGateway.findByRoleKey(cmd.roleKey()).ifPresent(existing -> {
            if (!existing.getId().equals(cmd.id())) {
                throw new BizException(CommonErrorCode.BAD_REQUEST, "Role key already exists: " + cmd.roleKey());
            }
        });
        systemRoleGateway.findByRoleName(cmd.roleName()).ifPresent(existing -> {
            if (!existing.getId().equals(cmd.id())) {
                throw new BizException(CommonErrorCode.BAD_REQUEST, "Role name already exists: " + cmd.roleName());
            }
        });

        systemRole.setRoleKey(cmd.roleKey());
        systemRole.setRoleName(cmd.roleName());
        systemRole.setEnabled(Boolean.TRUE.equals(cmd.enabled()));
        return systemRoleGateway.save(systemRole);
    }
}
