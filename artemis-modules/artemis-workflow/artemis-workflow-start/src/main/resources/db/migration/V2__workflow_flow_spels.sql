CREATE TABLE IF NOT EXISTS workflow_flow_spels (
    id BIGSERIAL PRIMARY KEY,
    component_name VARCHAR(128) NOT NULL,
    method_name VARCHAR(128) NOT NULL,
    parameters VARCHAR(512),
    preview_expression VARCHAR(512) NOT NULL,
    remarks VARCHAR(500),
    status INTEGER NOT NULL DEFAULT 1,
    created_by VARCHAR(64),
    created_time TIMESTAMP,
    updated_by VARCHAR(64),
    updated_time TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_workflow_flow_spels_preview_alive
    ON workflow_flow_spels(preview_expression)
    WHERE deleted = 0;

CREATE INDEX IF NOT EXISTS idx_workflow_flow_spels_status_alive
    ON workflow_flow_spels(status, deleted);
