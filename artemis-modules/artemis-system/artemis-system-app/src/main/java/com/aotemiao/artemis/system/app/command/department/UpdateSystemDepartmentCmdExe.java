package com.aotemiao.artemis.system.app.command.department;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.department.SystemDepartmentGateway;
import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.springframework.stereotype.Component;

/** 更新系统部门命令执行器。 */
@Component
public class UpdateSystemDepartmentCmdExe {

    private static final Long ROOT_PARENT_ID = 0L;
    private static final String NORMAL_STATUS = "NORMAL";

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemDepartmentGateway systemDepartmentGateway;

    public UpdateSystemDepartmentCmdExe(SystemDepartmentGateway systemDepartmentGateway) {
        this.systemDepartmentGateway = systemDepartmentGateway;
    }

    public SystemDepartment execute(UpdateSystemDepartmentCmd cmd) {
        SystemDepartment current = systemDepartmentGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Department not found: " + cmd.id()));
        Long parentId = cmd.parentId() == null ? ROOT_PARENT_ID : cmd.parentId();
        if (cmd.id().equals(parentId)) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Department parent cannot be itself: " + cmd.id());
        }

        String oldAncestors = current.getAncestors();
        String oldPrefix = oldAncestors + "," + current.getId();
        String newAncestors = resolveAncestors(parentId, cmd.id(), oldPrefix);
        ensureNameUnique(parentId, cmd.deptName(), cmd.id());

        current.setParentId(parentId);
        current.setAncestors(newAncestors);
        current.setDeptName(cmd.deptName());
        current.setDeptCategory(cmd.deptCategory());
        current.setSortOrder(cmd.sortOrder());
        current.setLeaderUserId(cmd.leaderUserId());
        current.setPhone(cmd.phone());
        current.setEmail(cmd.email());
        current.setStatus(cmd.status());
        current.setRemarks(cmd.remarks());
        SystemDepartment saved = systemDepartmentGateway.save(current);

        String newPrefix = newAncestors + "," + saved.getId();
        if (!oldPrefix.equals(newPrefix)) {
            List<SystemDepartment> descendants = systemDepartmentGateway.findAll().stream()
                    .filter(dept ->
                            dept.getAncestors() != null && dept.getAncestors().startsWith(oldPrefix))
                    .filter(dept -> !dept.getId().equals(saved.getId()))
                    .peek(dept ->
                            dept.setAncestors(newPrefix + dept.getAncestors().substring(oldPrefix.length())))
                    .toList();
            if (!descendants.isEmpty()) {
                systemDepartmentGateway.saveAll(descendants);
            }
        }
        return saved;
    }

    private String resolveAncestors(Long parentId, Long currentId, String oldPrefix) {
        if (ROOT_PARENT_ID.equals(parentId)) {
            return "0";
        }
        SystemDepartment parent = systemDepartmentGateway
                .findById(parentId)
                .orElseThrow(
                        () -> new BizException(CommonErrorCode.NOT_FOUND, "Parent department not found: " + parentId));
        if (!NORMAL_STATUS.equals(parent.getStatus())) {
            throw new BizException(
                    CommonErrorCode.BAD_REQUEST, "Disabled parent department cannot add child: " + parentId);
        }
        String parentPath = parent.getAncestors() + "," + parent.getId();
        if (parent.getId().equals(currentId) || parentPath.startsWith(oldPrefix + ",")) {
            throw new BizException(
                    CommonErrorCode.BAD_REQUEST, "Department cannot move under its descendant: " + parentId);
        }
        return parentPath;
    }

    private void ensureNameUnique(Long parentId, String deptName, Long currentId) {
        systemDepartmentGateway.findByParentIdAndDeptName(parentId, deptName).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw new BizException(
                        CommonErrorCode.BAD_REQUEST, "Department name already exists under parent: " + deptName);
            }
        });
    }
}
