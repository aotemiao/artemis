package com.aotemiao.artemis.system.app.command.department;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.department.SystemDepartmentGateway;
import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 新增系统部门命令执行器。 */
@Component
public class CreateSystemDepartmentCmdExe {

    private static final Long ROOT_PARENT_ID = 0L;
    private static final String NORMAL_STATUS = "NORMAL";

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemDepartmentGateway systemDepartmentGateway;

    public CreateSystemDepartmentCmdExe(SystemDepartmentGateway systemDepartmentGateway) {
        this.systemDepartmentGateway = systemDepartmentGateway;
    }

    public SystemDepartment execute(CreateSystemDepartmentCmd cmd) {
        Long parentId = cmd.parentId() == null ? ROOT_PARENT_ID : cmd.parentId();
        String ancestors = "0";
        if (!ROOT_PARENT_ID.equals(parentId)) {
            SystemDepartment parent = systemDepartmentGateway
                    .findById(parentId)
                    .orElseThrow(() ->
                            new BizException(CommonErrorCode.NOT_FOUND, "Parent department not found: " + parentId));
            if (!NORMAL_STATUS.equals(parent.getStatus())) {
                throw new BizException(
                        CommonErrorCode.BAD_REQUEST, "Disabled parent department cannot add child: " + parentId);
            }
            ancestors = parent.getAncestors() + "," + parent.getId();
        }
        ensureNameUnique(parentId, cmd.deptName(), null);

        SystemDepartment systemDepartment = new SystemDepartment();
        fill(systemDepartment, parentId, ancestors, cmd);
        return systemDepartmentGateway.save(systemDepartment);
    }

    private void ensureNameUnique(Long parentId, String deptName, Long currentId) {
        systemDepartmentGateway.findByParentIdAndDeptName(parentId, deptName).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BizException(
                        CommonErrorCode.BAD_REQUEST, "Department name already exists under parent: " + deptName);
            }
        });
    }

    private static void fill(
            SystemDepartment systemDepartment, Long parentId, String ancestors, CreateSystemDepartmentCmd cmd) {
        systemDepartment.setParentId(parentId);
        systemDepartment.setAncestors(ancestors);
        systemDepartment.setDeptName(cmd.deptName());
        systemDepartment.setDeptCategory(cmd.deptCategory());
        systemDepartment.setSortOrder(cmd.sortOrder());
        systemDepartment.setLeaderUserId(cmd.leaderUserId());
        systemDepartment.setPhone(cmd.phone());
        systemDepartment.setEmail(cmd.email());
        systemDepartment.setStatus(cmd.status());
        systemDepartment.setRemarks(cmd.remarks());
    }
}
