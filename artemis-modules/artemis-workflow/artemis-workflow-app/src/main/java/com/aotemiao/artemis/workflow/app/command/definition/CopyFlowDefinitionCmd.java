package com.aotemiao.artemis.workflow.app.command.definition;

/** 复制流程定义命令。 */
public record CopyFlowDefinitionCmd(Long id, String flowCode, String flowName, String tenantId) {}
