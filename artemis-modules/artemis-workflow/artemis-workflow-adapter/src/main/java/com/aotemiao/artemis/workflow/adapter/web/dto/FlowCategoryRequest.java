package com.aotemiao.artemis.workflow.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;

/** 流程分类写入请求。 */
public record FlowCategoryRequest(Long parentId, @NotBlank String categoryName, Integer sortOrder, String remarks) {}
