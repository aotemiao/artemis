package com.aotemiao.artemis.workflow.app.command.category;

/** 流程分类写入载荷。 */
public record FlowCategoryPayload(Long parentId, String categoryName, Integer sortOrder, String remarks) {}
