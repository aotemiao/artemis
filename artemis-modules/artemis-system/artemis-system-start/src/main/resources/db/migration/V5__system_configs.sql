-- System configs (platform parameters used by auth, user initialization and feature switches)
CREATE TABLE IF NOT EXISTS system_configs (
    id               BIGSERIAL PRIMARY KEY,
    config_name      VARCHAR(128) NOT NULL,
    config_key       VARCHAR(128) NOT NULL,
    config_value     VARCHAR(1024) NOT NULL,
    system_built_in  BOOLEAN NOT NULL DEFAULT FALSE,
    remarks          VARCHAR(512),
    create_time      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by        VARCHAR(64),
    update_by        VARCHAR(64),
    deleted          INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_system_configs_key ON system_configs (config_key) WHERE deleted = 0;

INSERT INTO system_configs (
    config_name, config_key, config_value, system_built_in, remarks,
    create_time, update_time, create_by, update_by, deleted
)
SELECT '账号注册开关', 'sys.account.registerUser', 'false', TRUE, '是否允许用户自行注册',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
WHERE NOT EXISTS (
    SELECT 1 FROM system_configs WHERE config_key = 'sys.account.registerUser' AND deleted = 0
);

INSERT INTO system_configs (
    config_name, config_key, config_value, system_built_in, remarks,
    create_time, update_time, create_by, update_by, deleted
)
SELECT '用户初始密码', 'sys.user.initPassword', '123456', TRUE, '管理员创建用户时的默认初始密码',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
WHERE NOT EXISTS (
    SELECT 1 FROM system_configs WHERE config_key = 'sys.user.initPassword' AND deleted = 0
);

INSERT INTO system_menus (
    parent_id, menu_type, menu_name, sort_order, path, component, permission_code,
    visible, enabled, create_time, update_time, create_by, update_by, deleted
)
SELECT p.id, 'MENU', '参数配置', 40, '/system/configs', 'system/config/index', 'system:config:list',
       TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
FROM system_menus p
WHERE p.parent_id = 0
  AND p.menu_name = '系统管理'
  AND p.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM system_menus WHERE parent_id = p.id AND menu_name = '参数配置' AND deleted = 0
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
                 ('新增参数', 10, 'system:config:add'),
                 ('编辑参数', 20, 'system:config:edit'),
                 ('删除参数', 30, 'system:config:remove'),
                 ('刷新缓存', 40, 'system:config:refresh')
         ) AS item(menu_name, sort_order, permission_code)
              ON parent.menu_name = '参数配置'
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
  AND (m.menu_name = '参数配置' OR m.permission_code LIKE 'system:config:%')
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menus rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = m.id
  );
