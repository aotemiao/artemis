package com.aotemiao.artemis.workflow.app.command.category;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.workflow.domain.gateway.category.FlowCategoryGateway;
import com.aotemiao.artemis.workflow.domain.model.category.FlowCategory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/** 新增流程分类命令执行器。 */
@Component
public class CreateFlowCategoryCmdExe {

    private static final Long ROOT_PARENT_ID = 0L;

    private final FlowCategoryGateway flowCategoryGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    public CreateFlowCategoryCmdExe(FlowCategoryGateway flowCategoryGateway) {
        this.flowCategoryGateway = flowCategoryGateway;
    }

    public FlowCategory execute(CreateFlowCategoryCmd cmd) {
        FlowCategoryPayload payload = requirePayload(cmd == null ? null : cmd.payload());
        Long parentId = normalizeParentId(payload.parentId());
        String ancestors = resolveAncestors(parentId);
        ensureNameUnique(parentId, payload.categoryName(), null);

        FlowCategory category = new FlowCategory();
        fill(category, payload, parentId, ancestors);
        return flowCategoryGateway.save(category);
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
                    if (currentId == null || !existing.getId().equals(currentId)) {
                        throw new BizException(
                                CommonErrorCode.BAD_REQUEST,
                                "Flow category name already exists under parent: " + categoryName);
                    }
                });
    }

    static FlowCategoryPayload requirePayload(FlowCategoryPayload payload) {
        if (payload == null
                || payload.categoryName() == null
                || payload.categoryName().isBlank()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Flow category name must not be blank");
        }
        return payload;
    }

    static Long normalizeParentId(Long parentId) {
        return parentId == null ? ROOT_PARENT_ID : parentId;
    }

    static void fill(FlowCategory category, FlowCategoryPayload payload, Long parentId, String ancestors) {
        Integer sortOrder = payload.sortOrder();
        category.setParentId(parentId);
        category.setAncestors(ancestors);
        category.setCategoryName(payload.categoryName());
        category.setSortOrder(sortOrder == null ? Integer.valueOf(0) : sortOrder);
        category.setRemarks(payload.remarks());
    }
}
