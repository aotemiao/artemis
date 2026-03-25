package com.aotemiao.artemis.symphony.orchestrator;

import com.aotemiao.artemis.symphony.config.ServiceConfig;
import com.aotemiao.artemis.symphony.core.model.WorkflowDefinition;
import com.aotemiao.artemis.symphony.tracker.TrackerClient;

/** 单次运行时代的不可变快照：工作流定义 + 类型化配置 + 工单客户端。 */
public record SymphonyRuntimeSnapshot(
        WorkflowDefinition definition, ServiceConfig config, TrackerClient trackerClient) {}
