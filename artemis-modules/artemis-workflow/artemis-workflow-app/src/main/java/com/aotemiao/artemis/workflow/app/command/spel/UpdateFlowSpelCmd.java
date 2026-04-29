package com.aotemiao.artemis.workflow.app.command.spel;

/** 修改流程 SpEL 表达式命令。 */
public record UpdateFlowSpelCmd(Long id, FlowSpelPayload payload) {}
