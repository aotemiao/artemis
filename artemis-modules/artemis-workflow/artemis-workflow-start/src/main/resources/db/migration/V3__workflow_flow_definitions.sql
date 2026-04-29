CREATE TABLE IF NOT EXISTS workflow_flow_definitions (
    id BIGSERIAL PRIMARY KEY,
    flow_code VARCHAR(128) NOT NULL,
    flow_name VARCHAR(128) NOT NULL,
    model_type VARCHAR(64) NOT NULL,
    category_id BIGINT,
    version INTEGER NOT NULL DEFAULT 1,
    publish_status INTEGER NOT NULL DEFAULT 0,
    custom_form BOOLEAN NOT NULL DEFAULT FALSE,
    form_path VARCHAR(512),
    active_status INTEGER NOT NULL DEFAULT 1,
    listener VARCHAR(512),
    ext_json TEXT,
    tenant_id VARCHAR(64) NOT NULL,
    definition_json TEXT,
    definition_xml TEXT,
    created_by VARCHAR(64),
    created_time TIMESTAMP,
    updated_by VARCHAR(64),
    updated_time TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_workflow_flow_definitions_tenant_code_alive
    ON workflow_flow_definitions(tenant_id, flow_code)
    WHERE deleted = 0;

CREATE INDEX IF NOT EXISTS idx_workflow_flow_definitions_publish_alive
    ON workflow_flow_definitions(publish_status, deleted);

CREATE INDEX IF NOT EXISTS idx_workflow_flow_definitions_tenant_alive
    ON workflow_flow_definitions(tenant_id, deleted);
