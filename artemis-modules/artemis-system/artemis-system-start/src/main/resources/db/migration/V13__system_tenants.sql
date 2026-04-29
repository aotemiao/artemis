-- System tenants
CREATE TABLE IF NOT EXISTS system_tenants (
    id               BIGSERIAL PRIMARY KEY,
    tenant_no        VARCHAR(6) NOT NULL,
    company_name     VARCHAR(128) NOT NULL,
    contact_name     VARCHAR(64),
    contact_phone    VARCHAR(32),
    social_credit_code VARCHAR(64),
    address          VARCHAR(255),
    domain           VARCHAR(255),
    intro            VARCHAR(512),
    package_id       BIGINT,
    expire_time      TIMESTAMP,
    user_limit       INTEGER,
    status           VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    remarks          VARCHAR(512),
    create_time      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by        VARCHAR(64),
    update_by        VARCHAR(64),
    deleted          INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_system_tenants_tenant_no
    ON system_tenants (tenant_no)
    WHERE deleted = 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_system_tenants_company_name
    ON system_tenants (company_name)
    WHERE deleted = 0;

INSERT INTO system_menus (
    parent_id, menu_type, menu_name, sort_order, path, component, permission_code,
    visible, enabled, create_time, update_time, create_by, update_by, deleted
)
SELECT 0, 'DIRECTORY', '租户管理', 30, '/tenant', NULL, NULL,
       TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
WHERE NOT EXISTS (
    SELECT 1 FROM system_menus WHERE parent_id = 0 AND menu_name = '租户管理' AND deleted = 0
);

INSERT INTO system_menus (
    parent_id, menu_type, menu_name, sort_order, path, component, permission_code,
    visible, enabled, create_time, update_time, create_by, update_by, deleted
)
SELECT p.id, 'MENU', '租户列表', 10, '/tenant/tenants', 'tenant/index', 'system:tenant:list',
       TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
FROM system_menus p
WHERE p.parent_id = 0
  AND p.menu_name = '租户管理'
  AND p.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM system_menus WHERE parent_id = p.id AND menu_name = '租户列表' AND deleted = 0
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
                 ('查询租户', 10, 'system:tenant:query'),
                 ('新增租户', 20, 'system:tenant:add'),
                 ('编辑租户', 30, 'system:tenant:edit'),
                 ('删除租户', 40, 'system:tenant:remove'),
                 ('导出租户', 50, 'system:tenant:export')
         ) AS item(menu_name, sort_order, permission_code)
              ON parent.menu_name = '租户列表'
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
  AND (m.menu_name = '租户管理' OR m.permission_code LIKE 'system:tenant:%')
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menus rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = m.id
  );
