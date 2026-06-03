package com.aotemiao.artemis.workflow.adapter.web.dto.spel;

/** 流程 SpEL 表达式响应。 */
public record FlowSpelDTO(
        Long id,
        String componentName,
        String methodName,
        String parameters,
        String previewExpression,
        String remarks,
        Integer status) {}
