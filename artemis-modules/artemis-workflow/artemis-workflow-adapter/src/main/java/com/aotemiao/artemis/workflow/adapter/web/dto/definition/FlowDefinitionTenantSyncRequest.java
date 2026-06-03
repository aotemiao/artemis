package com.aotemiao.artemis.workflow.adapter.web.dto.definition;

import jakarta.validation.constraints.NotBlank;

/** 流程定义租户同步请求。 */
public record FlowDefinitionTenantSyncRequest(@NotBlank String tenantId) {}
