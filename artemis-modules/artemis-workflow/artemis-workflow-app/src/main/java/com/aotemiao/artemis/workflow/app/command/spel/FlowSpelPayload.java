package com.aotemiao.artemis.workflow.app.command.spel;

/** 流程 SpEL 表达式写入载荷。 */
public record FlowSpelPayload(
        String componentName,
        String methodName,
        String parameters,
        String previewExpression,
        String remarks,
        Integer status) {}
