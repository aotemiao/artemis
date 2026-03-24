-- System roles (minimal role directory and user-role binding for system management)
CREATE TABLE IF NOT EXISTS system_roles (
    id           BIGSERIAL PRIMARY KEY,
    role_key     VARCHAR(64) NOT NULL,
    role_name    VARCHAR(128) NOT NULL,
    enabled      BOOLEAN NOT NULL DEFAULT TRUE,
    create_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by    VARCHAR(64),
    update_by    VARCHAR(64),
    deleted      INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_system_roles_role_key ON system_roles (role_key) WHERE deleted = 0;
CREATE UNIQUE INDEX IF NOT EXISTS uk_system_roles_role_name ON system_roles (role_name) WHERE deleted = 0;

CREATE TABLE IF NOT EXISTS system_user_roles (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL,
    role_id      BIGINT NOT NULL,
    create_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by    VARCHAR(64),
    update_by    VARCHAR(64)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_system_user_roles_user_role ON system_user_roles (user_id, role_id);

INSERT INTO system_roles (role_key, role_name, enabled, create_time, update_time, create_by, update_by, deleted)
SELECT 'super-admin', '超级管理员', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
WHERE NOT EXISTS (SELECT 1 FROM system_roles WHERE role_key = 'super-admin' AND deleted = 0);

INSERT INTO system_user_roles (user_id, role_id, create_time, update_time, create_by, update_by)
SELECT u.id, r.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'
FROM system_users u
         JOIN system_roles r ON r.role_key = 'super-admin' AND r.deleted = 0
WHERE u.username = 'admin'
  AND u.deleted = 0
  AND NOT EXISTS (
    SELECT 1
    FROM system_user_roles ur
    WHERE ur.user_id = u.id
      AND ur.role_id = r.id
);
