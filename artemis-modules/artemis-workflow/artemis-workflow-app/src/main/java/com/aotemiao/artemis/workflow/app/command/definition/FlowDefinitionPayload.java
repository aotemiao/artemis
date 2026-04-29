package com.aotemiao.artemis.workflow.app.command.definition;

/** 流程定义写入载荷。 */
public record FlowDefinitionPayload(
        String flowCode,
        String flowName,
        String modelType,
        Long categoryId,
        Integer version,
        Boolean customForm,
        String formPath,
        String listener,
        String extJson,
        String tenantId,
        String definitionJson,
        String definitionXml) {}
