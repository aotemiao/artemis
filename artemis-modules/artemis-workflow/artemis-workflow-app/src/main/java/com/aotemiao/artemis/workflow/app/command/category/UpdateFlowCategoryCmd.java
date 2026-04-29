package com.aotemiao.artemis.workflow.app.command.category;

/** 修改流程分类命令。 */
public record UpdateFlowCategoryCmd(Long id, FlowCategoryPayload payload) {}
