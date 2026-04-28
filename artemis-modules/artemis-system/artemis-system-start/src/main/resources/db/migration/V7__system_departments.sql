-- System departments (organization tree)
CREATE TABLE IF NOT EXISTS system_departments (
    id              BIGSERIAL PRIMARY KEY,
    parent_id       BIGINT NOT NULL DEFAULT 0,
    ancestors       VARCHAR(512) NOT NULL DEFAULT '0',
    dept_name       VARCHAR(128) NOT NULL,
    dept_category   VARCHAR(32),
    sort_order      INTEGER NOT NULL DEFAULT 0,
    leader_user_id  BIGINT,
    phone           VARCHAR(32),
    email           VARCHAR(128),
    status          VARCHAR(32) NOT NULL,
    remarks         VARCHAR(512),
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by       VARCHAR(64),
    update_by       VARCHAR(64),
    deleted         INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_system_departments_parent_name
    ON system_departments (parent_id, dept_name)
    WHERE deleted = 0;

INSERT INTO system_departments (
    parent_id, ancestors, dept_name, dept_category, sort_order, status, remarks,
    create_time, update_time, create_by, update_by, deleted
)
SELECT 0, '0', 'Artemis', 'COMPANY', 0, 'NORMAL', '默认根部门',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
WHERE NOT EXISTS (
    SELECT 1 FROM system_departments WHERE parent_id = 0 AND dept_name = 'Artemis' AND deleted = 0
);

INSERT INTO system_menus (
    parent_id, menu_type, menu_name, sort_order, path, component, permission_code,
    visible, enabled, create_time, update_time, create_by, update_by, deleted
)
SELECT p.id, 'MENU', '部门管理', 60, '/system/departments', 'system/dept/index', 'system:dept:list',
       TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
FROM system_menus p
WHERE p.parent_id = 0
  AND p.menu_name = '系统管理'
  AND p.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM system_menus WHERE parent_id = p.id AND menu_name = '部门管理' AND deleted = 0
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
                 ('查询部门', 10, 'system:dept:query'),
                 ('新增部门', 20, 'system:dept:add'),
                 ('编辑部门', 30, 'system:dept:edit'),
                 ('删除部门', 40, 'system:dept:remove')
         ) AS item(menu_name, sort_order, permission_code)
              ON parent.menu_name = '部门管理'
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
  AND (m.menu_name = '部门管理' OR m.permission_code LIKE 'system:dept:%')
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menus rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = m.id
  );
