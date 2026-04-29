package com.aotemiao.artemis.system.app.query.role;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.role.RoleDepartmentBindingGateway;
import com.aotemiao.artemis.system.domain.gateway.role.SystemRoleGateway;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.springframework.stereotype.Component;

/** 查询角色部门数据权限绑定。 */
@Component
public class ListRoleDepartmentsQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this executor does not expose them.")
    private final SystemRoleGateway systemRoleGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this executor does not expose them.")
    private final RoleDepartmentBindingGateway roleDepartmentBindingGateway;

    public ListRoleDepartmentsQryExe(
            SystemRoleGateway systemRoleGateway, RoleDepartmentBindingGateway roleDepartmentBindingGateway) {
        this.systemRoleGateway = systemRoleGateway;
        this.roleDepartmentBindingGateway = roleDepartmentBindingGateway;
    }

    public List<Long> execute(ListRoleDepartmentsQry qry) {
        systemRoleGateway
                .findById(qry.roleId())
                .orElseThrow(
                        () -> new BizException(CommonErrorCode.NOT_FOUND, "SystemRole not found: " + qry.roleId()));
        return roleDepartmentBindingGateway.findDepartmentIdsByRoleId(qry.roleId());
    }
}
