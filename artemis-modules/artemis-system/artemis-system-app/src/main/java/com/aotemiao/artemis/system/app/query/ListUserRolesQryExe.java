package com.aotemiao.artemis.system.app.query;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.SystemUserGateway;
import com.aotemiao.artemis.system.domain.gateway.UserRoleBindingGateway;
import com.aotemiao.artemis.system.domain.model.SystemRole;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.springframework.stereotype.Component;

/** 查询用户角色列表执行器。 */
@Component
public class ListUserRolesQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this executor does not expose them.")
    private final SystemUserGateway systemUserGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this executor does not expose them.")
    private final UserRoleBindingGateway userRoleBindingGateway;

    public ListUserRolesQryExe(SystemUserGateway systemUserGateway, UserRoleBindingGateway userRoleBindingGateway) {
        this.systemUserGateway = systemUserGateway;
        this.userRoleBindingGateway = userRoleBindingGateway;
    }

    public List<SystemRole> execute(ListUserRolesQry qry) {
        systemUserGateway
                .findById(qry.userId())
                .orElseThrow(
                        () -> new BizException(CommonErrorCode.NOT_FOUND, "SystemUser not found: " + qry.userId()));
        return userRoleBindingGateway.findRolesByUserId(qry.userId());
    }
}
