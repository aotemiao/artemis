package com.aotemiao.artemis.workflow.app.command.definition;

/** 同步流程定义到租户命令。 */
public record SyncFlowDefinitionTenantCmd(Long id, String tenantId) {}
