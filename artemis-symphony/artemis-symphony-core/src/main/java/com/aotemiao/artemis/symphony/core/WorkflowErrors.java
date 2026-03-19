package com.aotemiao.artemis.symphony.core;

/**
 * Workflow/configuration error codes per SPEC Section 5.5.
 */
public final class WorkflowErrors {
    public static final String MISSING_WORKFLOW_FILE = "missing_workflow_file";
    public static final String WORKFLOW_PARSE_ERROR = "workflow_parse_error";
    public static final String WORKFLOW_FRONT_MATTER_NOT_A_MAP = "workflow_front_matter_not_a_map";
    public static final String TEMPLATE_PARSE_ERROR = "template_parse_error";
    public static final String TEMPLATE_RENDER_ERROR = "template_render_error";

    private WorkflowErrors() {}
}
