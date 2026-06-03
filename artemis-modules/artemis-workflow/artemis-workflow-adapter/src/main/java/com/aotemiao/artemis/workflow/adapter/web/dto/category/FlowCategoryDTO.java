package com.aotemiao.artemis.workflow.adapter.web.dto.category;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;

/** 流程分类响应。 */
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        justification = "Children are copied into an immutable list before being exposed as response payload.")
public record FlowCategoryDTO(
        Long id,
        Long parentId,
        String ancestors,
        String categoryName,
        Integer sortOrder,
        String remarks,
        List<FlowCategoryDTO> children) {

    public FlowCategoryDTO {
        children = children == null ? List.of() : List.copyOf(children);
    }
}
