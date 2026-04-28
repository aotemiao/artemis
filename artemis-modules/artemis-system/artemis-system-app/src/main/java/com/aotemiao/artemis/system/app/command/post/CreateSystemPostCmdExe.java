package com.aotemiao.artemis.system.app.command.post;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.department.SystemDepartmentGateway;
import com.aotemiao.artemis.system.domain.gateway.post.SystemPostGateway;
import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import com.aotemiao.artemis.system.domain.model.post.SystemPost;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 创建系统岗位命令执行器。 */
@Component
public class CreateSystemPostCmdExe {

    private static final String NORMAL_STATUS = "NORMAL";

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemPostGateway systemPostGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final SystemDepartmentGateway systemDepartmentGateway;

    public CreateSystemPostCmdExe(
            SystemPostGateway systemPostGateway, SystemDepartmentGateway systemDepartmentGateway) {
        this.systemPostGateway = systemPostGateway;
        this.systemDepartmentGateway = systemDepartmentGateway;
    }

    public SystemPost execute(CreateSystemPostCmd cmd) {
        ensureDepartmentNormal(cmd.deptId());
        ensurePostCodeUnique(cmd.postCode(), null);
        ensurePostNameUnique(cmd.deptId(), cmd.postName(), null);

        SystemPost systemPost = new SystemPost();
        systemPost.setDeptId(cmd.deptId());
        systemPost.setPostCode(cmd.postCode());
        systemPost.setPostCategory(cmd.postCategory());
        systemPost.setPostName(cmd.postName());
        systemPost.setSortOrder(cmd.sortOrder());
        systemPost.setStatus(cmd.status());
        systemPost.setRemarks(cmd.remarks());
        return systemPostGateway.save(systemPost);
    }

    private void ensureDepartmentNormal(Long deptId) {
        SystemDepartment department = systemDepartmentGateway
                .findById(deptId)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Department not found: " + deptId));
        if (!NORMAL_STATUS.equals(department.getStatus())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Disabled department cannot add post: " + deptId);
        }
    }

    private void ensurePostCodeUnique(String postCode, Long currentId) {
        systemPostGateway.findByPostCode(postCode).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BizException(CommonErrorCode.BAD_REQUEST, "Post code already exists: " + postCode);
            }
        });
    }

    private void ensurePostNameUnique(Long deptId, String postName, Long currentId) {
        systemPostGateway.findByDeptIdAndPostName(deptId, postName).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BizException(
                        CommonErrorCode.BAD_REQUEST, "Post name already exists under department: " + postName);
            }
        });
    }
}
