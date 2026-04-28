-- System clients (auth client configuration)
CREATE TABLE IF NOT EXISTS system_clients (
    id                      BIGSERIAL PRIMARY KEY,
    client_id               VARCHAR(64) NOT NULL,
    client_key              VARCHAR(64) NOT NULL,
    client_secret           VARCHAR(256) NOT NULL,
    grant_types             VARCHAR(256) NOT NULL,
    device_type             VARCHAR(32) NOT NULL,
    active_timeout_seconds  BIGINT NOT NULL,
    fixed_timeout_seconds   BIGINT NOT NULL,
    status                  VARCHAR(32) NOT NULL,
    remarks                 VARCHAR(512),
    create_time             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by               VARCHAR(64),
    update_by               VARCHAR(64),
    deleted                 INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_system_clients_client_id
    ON system_clients (client_id)
    WHERE deleted = 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_system_clients_client_key
    ON system_clients (client_key)
    WHERE deleted = 0;

INSERT INTO system_clients (
    client_id, client_key, client_secret, grant_types, device_type,
    active_timeout_seconds, fixed_timeout_seconds, status, remarks,
    create_time, update_time, create_by, update_by, deleted
)
SELECT 'artemis-admin', 'artemis-admin-web', 'change-me', 'password,refresh_token', 'PC',
       1800, 86400, 'NORMAL', '默认后台管理客户端',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
WHERE NOT EXISTS (
    SELECT 1 FROM system_clients WHERE client_id = 'artemis-admin' AND deleted = 0
);

INSERT INTO system_menus (
    parent_id, menu_type, menu_name, sort_order, path, component, permission_code,
    visible, enabled, create_time, update_time, create_by, update_by, deleted
)
SELECT p.id, 'MENU', '客户端管理', 100, '/system/clients', 'system/client/index', 'system:client:list',
       TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
FROM system_menus p
WHERE p.parent_id = 0
  AND p.menu_name = '系统管理'
  AND p.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM system_menus WHERE parent_id = p.id AND menu_name = '客户端管理' AND deleted = 0
  );

INSERT INTO system_menus (
    parent_id, menu_type, menu_name, sort_order, path, component, permission_code,
    visible, enabled, create_time, update_time, create_by, update_by, deleted
)
SELECT parent.id, 'BUTTON', item.menu_name, item.sort_order, NULL, NULL, item.permission_code,
       TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
FROM system_menus parent
         JOIN (
             VALUES
                 ('查询客户端', 10, 'system:client:query'),
                 ('新增客户端', 20, 'system:client:add'),
                 ('编辑客户端', 30, 'system:client:edit'),
                 ('删除客户端', 40, 'system:client:remove'),
                 ('导出客户端', 50, 'system:client:export')
         ) AS item(menu_name, sort_order, permission_code)
              ON parent.menu_name = '客户端管理'
WHERE parent.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM system_menus WHERE parent_id = parent.id AND menu_name = item.menu_name AND deleted = 0
  );

INSERT INTO system_role_menus (role_id, menu_id, create_time, update_time, create_by, update_by)
SELECT r.id, m.id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system'
FROM system_roles r
         CROSS JOIN system_menus m
WHERE r.role_key = 'super-admin'
  AND r.deleted = 0
  AND m.deleted = 0
  AND (m.menu_name = '客户端管理' OR m.permission_code LIKE 'system:client:%')
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menus rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = m.id
  );
