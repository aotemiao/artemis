package com.aotemiao.artemis.workflow.adapter.web.dto.definition;

import jakarta.validation.constraints.NotBlank;

/** 流程定义写入请求。 */
public record FlowDefinitionRequest(
        @NotBlank String flowCode,
        @NotBlank String flowName,
        @NotBlank String modelType,
        Long categoryId,
        Integer version,
        Boolean customForm,
        String formPath,
        String listener,
        String extJson,
        @NotBlank String tenantId,
        String definitionJson,
        String definitionXml) {}
