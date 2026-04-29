-- Role data scope and role-department binding
ALTER TABLE system_roles
    ADD COLUMN IF NOT EXISTS data_scope VARCHAR(32) NOT NULL DEFAULT 'ALL';

CREATE TABLE IF NOT EXISTS system_role_departments (
    id             BIGSERIAL PRIMARY KEY,
    role_id        BIGINT NOT NULL,
    department_id  BIGINT NOT NULL,
    create_time    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by      VARCHAR(64),
    update_by      VARCHAR(64)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_system_role_departments_role_department
    ON system_role_departments (role_id, department_id);
