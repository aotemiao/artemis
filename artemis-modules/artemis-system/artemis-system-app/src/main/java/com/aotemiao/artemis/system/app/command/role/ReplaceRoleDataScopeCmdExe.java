package com.aotemiao.artemis.system.app.command.role;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.department.SystemDepartmentGateway;
import com.aotemiao.artemis.system.domain.gateway.role.RoleDepartmentBindingGateway;
import com.aotemiao.artemis.system.domain.gateway.role.SystemRoleGateway;
import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import com.aotemiao.artemis.system.domain.model.role.SystemRole;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

/** 替换角色数据权限范围命令执行器。 */
@Component
public class ReplaceRoleDataScopeCmdExe {

    private static final List<String> DATA_SCOPES =
            List.of("ALL", "CUSTOM", "DEPARTMENT", "DEPARTMENT_AND_CHILD", "SELF");

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this executor does not expose them.")
    private final SystemRoleGateway systemRoleGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this executor does not expose them.")
    private final SystemDepartmentGateway systemDepartmentGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this executor does not expose them.")
    private final RoleDepartmentBindingGateway roleDepartmentBindingGateway;

    public ReplaceRoleDataScopeCmdExe(
            SystemRoleGateway systemRoleGateway,
            SystemDepartmentGateway systemDepartmentGateway,
            RoleDepartmentBindingGateway roleDepartmentBindingGateway) {
        this.systemRoleGateway = systemRoleGateway;
        this.systemDepartmentGateway = systemDepartmentGateway;
        this.roleDepartmentBindingGateway = roleDepartmentBindingGateway;
    }

    public List<Long> execute(ReplaceRoleDataScopeCmd cmd) {
        SystemRole systemRole = systemRoleGateway
                .findById(cmd.roleId())
                .orElseThrow(
                        () -> new BizException(CommonErrorCode.NOT_FOUND, "SystemRole not found: " + cmd.roleId()));
        String dataScope = normalizeDataScope(cmd.dataScope());
        List<Long> departmentIds = normalizeDepartmentIds(cmd.departmentIds());
        ensureDepartmentsExist(departmentIds);

        systemRole.setDataScope(dataScope);
        systemRoleGateway.save(systemRole);
        roleDepartmentBindingGateway.replaceDepartments(cmd.roleId(), departmentIds);
        return roleDepartmentBindingGateway.findDepartmentIdsByRoleId(cmd.roleId());
    }

    private String normalizeDataScope(String dataScope) {
        String normalizedDataScope = dataScope == null || dataScope.isBlank() ? "ALL" : dataScope;
        if (!DATA_SCOPES.contains(normalizedDataScope)) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Unsupported role dataScope: " + dataScope);
        }
        return normalizedDataScope;
    }

    private List<Long> normalizeDepartmentIds(List<Long> departmentIds) {
        if (departmentIds == null) {
            return List.of();
        }
        return departmentIds.stream().filter(Objects::nonNull).distinct().toList();
    }

    private void ensureDepartmentsExist(List<Long> departmentIds) {
        if (departmentIds.isEmpty()) {
            return;
        }
        List<Long> existingIds = systemDepartmentGateway.findAll().stream()
                .map(SystemDepartment::getId)
                .toList();
        if (!existingIds.containsAll(departmentIds)) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Some departmentIds do not exist: " + departmentIds);
        }
    }
}
