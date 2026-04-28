-- System menus and role-menu binding (minimal permission code directory)
CREATE TABLE IF NOT EXISTS system_menus (
    id              BIGSERIAL PRIMARY KEY,
    parent_id       BIGINT NOT NULL DEFAULT 0,
    menu_type       VARCHAR(32) NOT NULL,
    menu_name       VARCHAR(128) NOT NULL,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    path            VARCHAR(255),
    component       VARCHAR(255),
    permission_code VARCHAR(128),
    visible         BOOLEAN NOT NULL DEFAULT TRUE,
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by       VARCHAR(64),
    update_by       VARCHAR(64),
    deleted         INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_system_menus_parent_name
    ON system_menus (parent_id, menu_name)
    WHERE deleted = 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_system_menus_path
    ON system_menus (path)
    WHERE deleted = 0 AND menu_type <> 'BUTTON' AND path IS NOT NULL;

CREATE TABLE IF NOT EXISTS system_role_menus (
    id           BIGSERIAL PRIMARY KEY,
    role_id      BIGINT NOT NULL,
    menu_id      BIGINT NOT NULL,
    create_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by    VARCHAR(64),
    update_by    VARCHAR(64)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_system_role_menus_role_menu ON system_role_menus (role_id, menu_id);

INSERT INTO system_menus (
    parent_id, menu_type, menu_name, sort_order, path, component, permission_code,
    visible, enabled, create_time, update_time, create_by, update_by, deleted
)
SELECT 0, 'DIRECTORY', '系统管理', 10, '/system', NULL, NULL,
       TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
WHERE NOT EXISTS (
    SELECT 1 FROM system_menus WHERE parent_id = 0 AND menu_name = '系统管理' AND deleted = 0
);

INSERT INTO system_menus (
    parent_id, menu_type, menu_name, sort_order, path, component, permission_code,
    visible, enabled, create_time, update_time, create_by, update_by, deleted
)
SELECT p.id, 'MENU', '用户管理', 10, '/system/users', 'system/user/index', 'system:user:list',
       TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
FROM system_menus p
WHERE p.parent_id = 0
  AND p.menu_name = '系统管理'
  AND p.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM system_menus WHERE parent_id = p.id AND menu_name = '用户管理' AND deleted = 0
  );

INSERT INTO system_menus (
    parent_id, menu_type, menu_name, sort_order, path, component, permission_code,
    visible, enabled, create_time, update_time, create_by, update_by, deleted
)
SELECT p.id, 'MENU', '角色管理', 20, '/system/roles', 'system/role/index', 'system:role:list',
       TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
FROM system_menus p
WHERE p.parent_id = 0
  AND p.menu_name = '系统管理'
  AND p.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM system_menus WHERE parent_id = p.id AND menu_name = '角色管理' AND deleted = 0
  );

INSERT INTO system_menus (
    parent_id, menu_type, menu_name, sort_order, path, component, permission_code,
    visible, enabled, create_time, update_time, create_by, update_by, deleted
)
SELECT p.id, 'MENU', '菜单管理', 30, '/system/menus', 'system/menu/index', 'system:menu:list',
       TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
FROM system_menus p
WHERE p.parent_id = 0
  AND p.menu_name = '系统管理'
  AND p.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM system_menus WHERE parent_id = p.id AND menu_name = '菜单管理' AND deleted = 0
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
                 ('用户管理', '新增用户', 10, 'system:user:add'),
                 ('用户管理', '编辑用户', 20, 'system:user:edit'),
                 ('用户管理', '删除用户', 30, 'system:user:remove'),
                 ('角色管理', '新增角色', 10, 'system:role:add'),
                 ('角色管理', '编辑角色', 20, 'system:role:edit'),
                 ('角色管理', '删除角色', 30, 'system:role:remove'),
                 ('菜单管理', '新增菜单', 10, 'system:menu:add'),
                 ('菜单管理', '编辑菜单', 20, 'system:menu:edit'),
                 ('菜单管理', '删除菜单', 30, 'system:menu:remove')
         ) AS item(parent_name, menu_name, sort_order, permission_code)
              ON parent.menu_name = item.parent_name
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
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menus rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = m.id
  );
