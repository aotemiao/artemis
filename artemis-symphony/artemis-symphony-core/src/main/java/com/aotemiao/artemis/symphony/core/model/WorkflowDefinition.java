package com.aotemiao.artemis.symphony.core.model;

import java.util.Map;

/**
 * 解析后的 WORKFLOW.md：配置（YAML 根）+ 提示模板正文（Markdown）。
 *
 * @see <a href="https://github.com/openai/symphony/blob/main/SPEC.md">Symphony SPEC</a> 第 4.1.2 节
 */
public record WorkflowDefinition(Map<String, Object> config, String promptTemplate) {}
