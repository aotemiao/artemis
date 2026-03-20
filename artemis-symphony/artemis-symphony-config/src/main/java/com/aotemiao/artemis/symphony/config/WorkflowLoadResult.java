package com.aotemiao.artemis.symphony.config;

import com.aotemiao.artemis.symphony.core.model.WorkflowDefinition;

/** 加载 WORKFLOW.md 的结果：成功携带定义，失败携带错误码与说明。 */
public sealed interface WorkflowLoadResult permits WorkflowLoadResult.Success, WorkflowLoadResult.Error {

    record Success(WorkflowDefinition definition) implements WorkflowLoadResult {}

    record Error(String code, String message) implements WorkflowLoadResult {}
}
