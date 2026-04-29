package com.aotemiao.artemis.workflow.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;

/** 流程定义复制请求。 */
public record FlowDefinitionCopyRequest(@NotBlank String flowCode, String flowName) {}
