package com.aotemiao.artemis.workflow.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;

/** 流程 SpEL 表达式写入请求。 */
public record FlowSpelRequest(
        @NotBlank String componentName,
        @NotBlank String methodName,
        String parameters,
        @NotBlank String previewExpression,
        String remarks,
        Integer status) {}
