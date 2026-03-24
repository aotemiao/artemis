-- System users (minimal account directory for auth and system management)
CREATE TABLE IF NOT EXISTS system_users (
    id           BIGSERIAL PRIMARY KEY,
    username     VARCHAR(64) NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    password     VARCHAR(128) NOT NULL,
    enabled      BOOLEAN NOT NULL DEFAULT TRUE,
    create_time  TIMESTAMP,
    update_time  TIMESTAMP,
    create_by    VARCHAR(64),
    update_by    VARCHAR(64),
    deleted      INT DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_system_users_username ON system_users (username) WHERE deleted = 0;

INSERT INTO system_users (username, display_name, password, enabled, create_time, update_time, create_by, update_by, deleted)
SELECT 'admin', '管理员', '123456', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
WHERE NOT EXISTS (SELECT 1 FROM system_users WHERE username = 'admin' AND deleted = 0);
