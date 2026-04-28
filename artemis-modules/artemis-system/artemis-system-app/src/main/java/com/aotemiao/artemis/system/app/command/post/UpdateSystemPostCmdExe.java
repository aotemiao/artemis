package com.aotemiao.artemis.system.app.command.post;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.department.SystemDepartmentGateway;
import com.aotemiao.artemis.system.domain.gateway.post.SystemPostGateway;
import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import com.aotemiao.artemis.system.domain.model.post.SystemPost;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 更新系统岗位命令执行器。 */
@Component
public class UpdateSystemPostCmdExe {

    private static final String NORMAL_STATUS = "NORMAL";

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemPostGateway systemPostGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemDepartmentGateway systemDepartmentGateway;

    public UpdateSystemPostCmdExe(
            SystemPostGateway systemPostGateway, SystemDepartmentGateway systemDepartmentGateway) {
        this.systemPostGateway = systemPostGateway;
        this.systemDepartmentGateway = systemDepartmentGateway;
    }

    public SystemPost execute(UpdateSystemPostCmd cmd) {
        SystemPost current = systemPostGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Post not found: " + cmd.id()));
        ensureDepartmentNormal(cmd.deptId());
        ensurePostCodeUnique(cmd.postCode(), cmd.id());
        ensurePostNameUnique(cmd.deptId(), cmd.postName(), cmd.id());

        current.setDeptId(cmd.deptId());
        current.setPostCode(cmd.postCode());
        current.setPostCategory(cmd.postCategory());
        current.setPostName(cmd.postName());
        current.setSortOrder(cmd.sortOrder());
        current.setStatus(cmd.status());
        current.setRemarks(cmd.remarks());
        return systemPostGateway.save(current);
    }

    private void ensureDepartmentNormal(Long deptId) {
        SystemDepartment department = systemDepartmentGateway
                .findById(deptId)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Department not found: " + deptId));
        if (!NORMAL_STATUS.equals(department.getStatus())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Disabled department cannot bind post: " + deptId);
        }
    }

    private void ensurePostCodeUnique(String postCode, Long currentId) {
        systemPostGateway.findByPostCode(postCode).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw new BizException(CommonErrorCode.BAD_REQUEST, "Post code already exists: " + postCode);
            }
        });
    }

    private void ensurePostNameUnique(Long deptId, String postName, Long currentId) {
        systemPostGateway.findByDeptIdAndPostName(deptId, postName).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw new BizException(
                        CommonErrorCode.BAD_REQUEST, "Post name already exists under department: " + postName);
            }
        });
    }
}
