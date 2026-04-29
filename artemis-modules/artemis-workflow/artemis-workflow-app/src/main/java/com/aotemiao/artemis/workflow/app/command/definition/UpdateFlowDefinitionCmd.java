package com.aotemiao.artemis.workflow.app.command.definition;

/** 修改流程定义命令。 */
public record UpdateFlowDefinitionCmd(Long id, FlowDefinitionPayload payload) {}
