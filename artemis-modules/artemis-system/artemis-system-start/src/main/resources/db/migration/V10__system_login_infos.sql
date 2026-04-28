-- System login info audit records
CREATE TABLE IF NOT EXISTS system_login_infos (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       VARCHAR(64) NOT NULL DEFAULT '000000',
    username        VARCHAR(64) NOT NULL,
    client_id       VARCHAR(64),
    device_type     VARCHAR(32),
    ipaddr          VARCHAR(128),
    login_location  VARCHAR(255),
    browser         VARCHAR(128),
    os              VARCHAR(128),
    status          VARCHAR(32) NOT NULL,
    msg             VARCHAR(512),
    login_time      TIMESTAMP NOT NULL,
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by       VARCHAR(64),
    update_by       VARCHAR(64),
    deleted         INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_system_login_infos_username
    ON system_login_infos (username);

CREATE INDEX IF NOT EXISTS idx_system_login_infos_login_time
    ON system_login_infos (login_time);

CREATE INDEX IF NOT EXISTS idx_system_login_infos_status
    ON system_login_infos (status);

INSERT INTO system_menus (
    parent_id, menu_type, menu_name, sort_order, path, component, permission_code,
    visible, enabled, create_time, update_time, create_by, update_by, deleted
)
SELECT p.id, 'MENU', '登录日志', 20, '/monitor/login-infos', 'monitor/logininfor/index', 'monitor:logininfor:list',
       TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
FROM system_menus p
WHERE p.parent_id = 0
  AND p.menu_name = '系统监控'
  AND p.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM system_menus WHERE parent_id = p.id AND menu_name = '登录日志' AND deleted = 0
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
                 ('查询登录日志', 10, 'monitor:logininfor:query'),
                 ('删除登录日志', 20, 'monitor:logininfor:remove'),
                 ('导出登录日志', 30, 'monitor:logininfor:export'),
                 ('账户解锁', 40, 'monitor:logininfor:unlock')
         ) AS item(menu_name, sort_order, permission_code)
              ON parent.menu_name = '登录日志'
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
  AND (m.menu_name = '登录日志' OR m.permission_code LIKE 'monitor:logininfor:%')
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menus rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = m.id
  );
