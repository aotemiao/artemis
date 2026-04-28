package com.aotemiao.artemis.system.app.query.department;

import com.aotemiao.artemis.system.domain.gateway.department.SystemDepartmentGateway;
import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.springframework.stereotype.Component;

/** 查询系统部门列表执行器。 */
@Component
public class ListSystemDepartmentQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    private final SystemDepartmentGateway systemDepartmentGateway;

    public ListSystemDepartmentQryExe(SystemDepartmentGateway systemDepartmentGateway) {
        this.systemDepartmentGateway = systemDepartmentGateway;
    }

    public List<SystemDepartment> execute(ListSystemDepartmentQry qry) {
        List<SystemDepartment> departments = systemDepartmentGateway.findAll();
        if (qry.excludeId() == null) {
            return departments;
        }
        return departments.stream()
                .filter(dept -> !qry.excludeId().equals(dept.getId()))
                .filter(dept -> {
                    String ancestors = dept.getAncestors();
                    return ancestors == null || !ancestors.matches("(^|.*,)" + qry.excludeId() + "(,.*|$)");
                })
                .toList();
    }
}
