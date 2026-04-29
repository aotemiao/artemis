-- Bind system users to tenant numbers for runtime tenant constraints
ALTER TABLE system_users
    ADD COLUMN IF NOT EXISTS tenant_no VARCHAR(6) NOT NULL DEFAULT '000000';

CREATE INDEX IF NOT EXISTS idx_system_users_tenant_no
    ON system_users (tenant_no);
