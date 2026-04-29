package com.aotemiao.artemis.workflow.app.command.category;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.workflow.domain.gateway.category.FlowCategoryGateway;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 删除流程分类命令执行器。 */
@Component
public class DeleteFlowCategoryCmdExe {

    private final FlowCategoryGateway flowCategoryGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    public DeleteFlowCategoryCmdExe(FlowCategoryGateway flowCategoryGateway) {
        this.flowCategoryGateway = flowCategoryGateway;
    }

    public void execute(DeleteFlowCategoryCmd cmd) {
        Long id = cmd == null ? null : cmd.id();
        if (id == null || id <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid flow category id: " + id);
        }
        flowCategoryGateway
                .findById(id)
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Flow category not found: " + id));
        if (flowCategoryGateway.existsByParentId(id)) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Flow category has children: " + id);
        }
        flowCategoryGateway.deleteById(id);
    }
}
