package com.aotemiao.artemis.workflow.app.command.category;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.workflow.domain.gateway.category.FlowCategoryGateway;
import com.aotemiao.artemis.workflow.domain.model.category.FlowCategory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 修改流程分类命令执行器。 */
@Component
public class UpdateFlowCategoryCmdExe {

    private static final Long ROOT_PARENT_ID = 0L;

    private final FlowCategoryGateway flowCategoryGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    public UpdateFlowCategoryCmdExe(FlowCategoryGateway flowCategoryGateway) {
        this.flowCategoryGateway = flowCategoryGateway;
    }

    public FlowCategory execute(UpdateFlowCategoryCmd cmd) {
        if (cmd == null || cmd.id() == null || cmd.id() <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid flow category id");
        }
        FlowCategoryPayload payload = CreateFlowCategoryCmdExe.requirePayload(cmd.payload());
        FlowCategory existing = flowCategoryGateway
                .findById(cmd.id())
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Flow category not found: " + cmd.id()));
        Long parentId = CreateFlowCategoryCmdExe.normalizeParentId(payload.parentId());
        if (ROOT_PARENT_ID.equals(existing.getParentId()) && !ROOT_PARENT_ID.equals(parentId)) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Top-level flow category cannot change parent");
        }
        if (existing.getId().equals(parentId)) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Flow category cannot use itself as parent");
        }
        String ancestors = resolveAncestors(parentId);
        ensureNameUnique(parentId, payload.categoryName(), existing.getId());

        CreateFlowCategoryCmdExe.fill(existing, payload, parentId, ancestors);
        return flowCategoryGateway.save(existing);
    }

    private String resolveAncestors(Long parentId) {
        if (ROOT_PARENT_ID.equals(parentId)) {
            return "0";
        }
        FlowCategory parent = flowCategoryGateway
                .findById(parentId)
                .orElseThrow(() ->
                        new BizException(CommonErrorCode.NOT_FOUND, "Parent flow category not found: " + parentId));
        return parent.getAncestors() + "," + parent.getId();
    }

    private void ensureNameUnique(Long parentId, String categoryName, Long currentId) {
        flowCategoryGateway
                .findByParentIdAndCategoryName(parentId, categoryName)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(currentId)) {
                        throw new BizException(
                                CommonErrorCode.BAD_REQUEST,
                                "Flow category name already exists under parent: " + categoryName);
                    }
                });
    }
}
