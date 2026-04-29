package com.aotemiao.artemis.system.app.command.role;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.role.SystemRoleGateway;
import com.aotemiao.artemis.system.domain.model.role.SystemRole;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.springframework.stereotype.Component;

/** 新增系统角色命令执行器。 */
@Component
public class CreateSystemRoleCmdExe {

    private static final List<String> DATA_SCOPES =
            List.of("ALL", "CUSTOM", "DEPARTMENT", "DEPARTMENT_AND_CHILD", "SELF");

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemRoleGateway systemRoleGateway;

    public CreateSystemRoleCmdExe(SystemRoleGateway systemRoleGateway) {
        this.systemRoleGateway = systemRoleGateway;
    }

    public SystemRole execute(CreateSystemRoleCmd cmd) {
        systemRoleGateway.findByRoleKey(cmd.roleKey()).ifPresent(existing -> {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Role key already exists: " + cmd.roleKey());
        });
        systemRoleGateway.findByRoleName(cmd.roleName()).ifPresent(existing -> {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Role name already exists: " + cmd.roleName());
        });

        SystemRole systemRole = new SystemRole();
        systemRole.setRoleKey(cmd.roleKey());
        systemRole.setRoleName(cmd.roleName());
        systemRole.setDataScope(normalizeDataScope(cmd.dataScope()));
        systemRole.setEnabled(true);
        return systemRoleGateway.save(systemRole);
    }

    private String normalizeDataScope(String dataScope) {
        String normalizedDataScope = dataScope == null || dataScope.isBlank() ? "ALL" : dataScope;
        if (!DATA_SCOPES.contains(normalizedDataScope)) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Unsupported role dataScope: " + dataScope);
        }
        return normalizedDataScope;
    }
}
