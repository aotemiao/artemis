package com.aotemiao.artemis.system.app.query.auth;

import com.aotemiao.artemis.system.domain.gateway.role.RoleMenuBindingGateway;
import com.aotemiao.artemis.system.domain.gateway.user.SystemUserGateway;
import com.aotemiao.artemis.system.domain.gateway.user.UserRoleBindingGateway;
import com.aotemiao.artemis.system.domain.model.auth.UserAuthorizationSnapshot;
import com.aotemiao.artemis.system.domain.model.role.SystemRole;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 查询用户授权快照执行器。 */
@Component
public class GetUserAuthorizationQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this executor does not expose them.")
    private final SystemUserGateway systemUserGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this executor does not expose them.")
    private final UserRoleBindingGateway userRoleBindingGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final RoleMenuBindingGateway roleMenuBindingGateway;

    public GetUserAuthorizationQryExe(
            SystemUserGateway systemUserGateway,
            UserRoleBindingGateway userRoleBindingGateway,
            RoleMenuBindingGateway roleMenuBindingGateway) {
        this.systemUserGateway = systemUserGateway;
        this.userRoleBindingGateway = userRoleBindingGateway;
        this.roleMenuBindingGateway = roleMenuBindingGateway;
    }

    public Optional<UserAuthorizationSnapshot> execute(GetUserAuthorizationQry qry) {
        return systemUserGateway.findById(qry.userId()).map(systemUser -> {
            List<SystemRole> enabledRoles = userRoleBindingGateway.findRolesByUserId(qry.userId()).stream()
                    .filter(systemRole -> Boolean.TRUE.equals(systemRole.isEnabled()))
                    .toList();
            return new UserAuthorizationSnapshot(
                    systemUser.getId(),
                    systemUser.getUsername(),
                    systemUser.getDisplayName(),
                    enabledRoles.stream().map(SystemRole::getRoleKey).distinct().toList(),
                    roleMenuBindingGateway
                            .findMenusByRoleIds(
                                    enabledRoles.stream().map(SystemRole::getId).toList())
                            .stream()
                            .filter(systemMenu -> Boolean.TRUE.equals(systemMenu.isEnabled()))
                            .map(systemMenu -> systemMenu.getPermissionCode())
                            .filter(permissionCode -> permissionCode != null && !permissionCode.isBlank())
                            .distinct()
                            .toList());
        });
    }
}
