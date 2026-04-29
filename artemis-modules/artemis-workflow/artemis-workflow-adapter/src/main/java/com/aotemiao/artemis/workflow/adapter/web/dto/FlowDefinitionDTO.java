package com.aotemiao.artemis.workflow.adapter.web.dto;

/** 流程定义响应。 */
public record FlowDefinitionDTO(
        Long id,
        String flowCode,
        String flowName,
        String modelType,
        Long categoryId,
        Integer version,
        Integer publishStatus,
        Boolean customForm,
        String formPath,
        Integer activeStatus,
        String listener,
        String extJson,
        String tenantId,
        String definitionJson,
        String definitionXml) {}
