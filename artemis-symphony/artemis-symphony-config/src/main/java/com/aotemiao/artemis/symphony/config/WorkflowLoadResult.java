package com.aotemiao.artemis.symphony.config;

import com.aotemiao.artemis.symphony.core.model.WorkflowDefinition;

/**
 * Result of loading WORKFLOW.md: either success with definition or error code.
 */
public sealed interface WorkflowLoadResult permits WorkflowLoadResult.Success, WorkflowLoadResult.Error {

    record Success(WorkflowDefinition definition) implements WorkflowLoadResult {}

    record Error(String code, String message) implements WorkflowLoadResult {}
}
