package com.aotemiao.artemis.system.app.query;

import com.aotemiao.artemis.system.domain.gateway.SystemUserGateway;
import com.aotemiao.artemis.system.domain.gateway.UserRoleBindingGateway;
import com.aotemiao.artemis.system.domain.model.UserAuthorizationSnapshot;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

    public GetUserAuthorizationQryExe(
            SystemUserGateway systemUserGateway, UserRoleBindingGateway userRoleBindingGateway) {
        this.systemUserGateway = systemUserGateway;
        this.userRoleBindingGateway = userRoleBindingGateway;
    }

    public Optional<UserAuthorizationSnapshot> execute(GetUserAuthorizationQry qry) {
        return systemUserGateway
                .findById(qry.userId())
                .map(systemUser -> new UserAuthorizationSnapshot(
                        systemUser.getId(),
                        systemUser.getUsername(),
                        systemUser.getDisplayName(),
                        userRoleBindingGateway.findRolesByUserId(qry.userId()).stream()
                                .filter(systemRole -> Boolean.TRUE.equals(systemRole.isEnabled()))
                                .map(systemRole -> systemRole.getRoleKey())
                                .distinct()
                                .toList()));
    }
}
