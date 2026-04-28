package com.aotemiao.artemis.system.app.query.department;

import com.aotemiao.artemis.system.domain.gateway.department.SystemDepartmentGateway;
import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 按 ID 查询系统部门执行器。 */
@Component
public class FindSystemDepartmentByIdQryExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification =
                    "Spring injects the gateway as a managed collaborator; this query executor does not expose it.")
    private final SystemDepartmentGateway systemDepartmentGateway;

    public FindSystemDepartmentByIdQryExe(SystemDepartmentGateway systemDepartmentGateway) {
        this.systemDepartmentGateway = systemDepartmentGateway;
    }

    public Optional<SystemDepartment> execute(FindSystemDepartmentByIdQry qry) {
        return systemDepartmentGateway.findById(qry.id());
    }
}
