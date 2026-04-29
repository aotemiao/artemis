CREATE TABLE IF NOT EXISTS workflow_flow_categories (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT NOT NULL DEFAULT 0,
    ancestors VARCHAR(512) NOT NULL DEFAULT '0',
    category_name VARCHAR(100) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    remarks VARCHAR(500),
    created_by VARCHAR(64),
    created_time TIMESTAMP,
    updated_by VARCHAR(64),
    updated_time TIMESTAMP,
    deleted INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_workflow_flow_categories_parent_name_alive
    ON workflow_flow_categories(parent_id, category_name)
    WHERE deleted = 0;

CREATE INDEX IF NOT EXISTS idx_workflow_flow_categories_parent_alive
    ON workflow_flow_categories(parent_id, deleted);
