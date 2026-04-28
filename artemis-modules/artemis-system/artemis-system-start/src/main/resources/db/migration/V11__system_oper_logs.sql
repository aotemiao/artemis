-- System operation audit records
CREATE TABLE IF NOT EXISTS system_oper_logs (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(128) NOT NULL,
    business_type   VARCHAR(32) NOT NULL,
    method          VARCHAR(255),
    request_method  VARCHAR(32),
    operator_type   VARCHAR(32) NOT NULL DEFAULT 'MANAGE',
    oper_name       VARCHAR(64) NOT NULL,
    dept_name       VARCHAR(128),
    oper_url        VARCHAR(512),
    oper_ip         VARCHAR(128),
    oper_location   VARCHAR(255),
    oper_param      TEXT,
    json_result     TEXT,
    status          VARCHAR(32) NOT NULL,
    error_msg       TEXT,
    cost_time       BIGINT NOT NULL DEFAULT 0,
    oper_time       TIMESTAMP NOT NULL,
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by       VARCHAR(64),
    update_by       VARCHAR(64),
    deleted         INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_system_oper_logs_title
    ON system_oper_logs (title);

CREATE INDEX IF NOT EXISTS idx_system_oper_logs_oper_time
    ON system_oper_logs (oper_time);

CREATE INDEX IF NOT EXISTS idx_system_oper_logs_status
    ON system_oper_logs (status);

INSERT INTO system_menus (
    parent_id, menu_type, menu_name, sort_order, path, component, permission_code,
    visible, enabled, create_time, update_time, create_by, update_by, deleted
)
SELECT p.id, 'MENU', '操作日志', 10, '/monitor/oper-logs', 'monitor/operlog/index', 'monitor:operlog:list',
       TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
FROM system_menus p
WHERE p.parent_id = 0
  AND p.menu_name = '系统监控'
  AND p.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM system_menus WHERE parent_id = p.id AND menu_name = '操作日志' AND deleted = 0
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
                 ('查询操作日志', 10, 'monitor:operlog:query'),
                 ('删除操作日志', 20, 'monitor:operlog:remove'),
                 ('导出操作日志', 30, 'monitor:operlog:export')
         ) AS item(menu_name, sort_order, permission_code)
              ON parent.menu_name = '操作日志'
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
  AND (m.menu_name = '操作日志' OR m.permission_code LIKE 'monitor:operlog:%')
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menus rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = m.id
  );
