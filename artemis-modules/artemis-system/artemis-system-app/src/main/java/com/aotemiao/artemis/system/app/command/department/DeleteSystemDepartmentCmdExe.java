package com.aotemiao.artemis.system.app.command.department;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.department.SystemDepartmentGateway;
import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 删除系统部门命令执行器。 */
@Component
public class DeleteSystemDepartmentCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemDepartmentGateway systemDepartmentGateway;

    public DeleteSystemDepartmentCmdExe(SystemDepartmentGateway systemDepartmentGateway) {
        this.systemDepartmentGateway = systemDepartmentGateway;
    }

    public void execute(DeleteSystemDepartmentCmd cmd) {
        SystemDepartment current = systemDepartmentGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Department not found: " + cmd.id()));
        String prefix = current.getAncestors() + "," + current.getId();
        boolean hasChild = systemDepartmentGateway.findAll().stream()
                .anyMatch(dept -> !dept.getId().equals(current.getId())
                        && dept.getAncestors() != null
                        && dept.getAncestors().startsWith(prefix));
        if (hasChild) {
            throw new BizException(
                    CommonErrorCode.BAD_REQUEST, "Department with children cannot be deleted: " + cmd.id());
        }
        systemDepartmentGateway.deleteById(cmd.id());
    }
}
