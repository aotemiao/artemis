-- System posts (organization positions)
CREATE TABLE IF NOT EXISTS system_posts (
    id              BIGSERIAL PRIMARY KEY,
    dept_id         BIGINT NOT NULL,
    post_code       VARCHAR(64) NOT NULL,
    post_category   VARCHAR(32),
    post_name       VARCHAR(128) NOT NULL,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    status          VARCHAR(32) NOT NULL,
    remarks         VARCHAR(512),
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by       VARCHAR(64),
    update_by       VARCHAR(64),
    deleted         INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_system_posts_code
    ON system_posts (post_code)
    WHERE deleted = 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_system_posts_dept_name
    ON system_posts (dept_id, post_name)
    WHERE deleted = 0;

CREATE TABLE IF NOT EXISTS system_user_posts (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    post_id         BIGINT NOT NULL,
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by       VARCHAR(64),
    update_by       VARCHAR(64)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_system_user_posts_user_post
    ON system_user_posts (user_id, post_id);

INSERT INTO system_posts (
    dept_id, post_code, post_category, post_name, sort_order, status, remarks,
    create_time, update_time, create_by, update_by, deleted
)
SELECT d.id, 'admin', 'MANAGEMENT', '管理员', 0, 'NORMAL', '默认管理员岗位',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
FROM system_departments d
WHERE d.parent_id = 0
  AND d.dept_name = 'Artemis'
  AND d.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM system_posts WHERE post_code = 'admin' AND deleted = 0
  );

INSERT INTO system_menus (
    parent_id, menu_type, menu_name, sort_order, path, component, permission_code,
    visible, enabled, create_time, update_time, create_by, update_by, deleted
)
SELECT p.id, 'MENU', '岗位管理', 70, '/system/posts', 'system/post/index', 'system:post:list',
       TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
FROM system_menus p
WHERE p.parent_id = 0
  AND p.menu_name = '系统管理'
  AND p.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM system_menus WHERE parent_id = p.id AND menu_name = '岗位管理' AND deleted = 0
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
                 ('查询岗位', 10, 'system:post:query'),
                 ('新增岗位', 20, 'system:post:add'),
                 ('编辑岗位', 30, 'system:post:edit'),
                 ('删除岗位', 40, 'system:post:remove'),
                 ('导出岗位', 50, 'system:post:export')
         ) AS item(menu_name, sort_order, permission_code)
              ON parent.menu_name = '岗位管理'
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
  AND (m.menu_name = '岗位管理' OR m.permission_code LIKE 'system:post:%')
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menus rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = m.id
  );
