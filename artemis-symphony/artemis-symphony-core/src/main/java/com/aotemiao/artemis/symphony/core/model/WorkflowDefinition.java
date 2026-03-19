package com.aotemiao.artemis.symphony.core.model;

import java.util.Map;

/**
 * Parsed WORKFLOW.md payload: config (YAML front matter root) + prompt_template (markdown body).
 *
 * @see <a href="https://github.com/openai/symphony/blob/main/SPEC.md">Symphony SPEC</a> Section 4.1.2
 */
public record WorkflowDefinition(Map<String, Object> config, String promptTemplate) {}
