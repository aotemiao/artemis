-- System tenant package configuration
CREATE TABLE IF NOT EXISTS system_tenant_packages (
    id                     BIGSERIAL PRIMARY KEY,
    package_name           VARCHAR(128) NOT NULL,
    menu_check_strictly    BOOLEAN NOT NULL DEFAULT TRUE,
    enabled                BOOLEAN NOT NULL DEFAULT TRUE,
    remarks                VARCHAR(512),
    create_time            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by              VARCHAR(64),
    update_by              VARCHAR(64),
    deleted                INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_system_tenant_packages_name
    ON system_tenant_packages (package_name)
    WHERE deleted = 0;

CREATE TABLE IF NOT EXISTS system_tenant_package_menus (
    id             BIGSERIAL PRIMARY KEY,
    package_id     BIGINT NOT NULL,
    menu_id        BIGINT NOT NULL,
    create_time    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by      VARCHAR(64),
    update_by      VARCHAR(64)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_system_tenant_package_menus_package_menu
    ON system_tenant_package_menus (package_id, menu_id);

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
SELECT p.id, 'MENU', '租户套餐', 10, '/tenant/packages', 'tenant/package/index', 'system:tenantPackage:list',
       TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system', 0
FROM system_menus p
WHERE p.parent_id = 0
  AND p.menu_name = '租户管理'
  AND p.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM system_menus WHERE parent_id = p.id AND menu_name = '租户套餐' AND deleted = 0
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
                 ('查询租户套餐', 10, 'system:tenantPackage:query'),
                 ('新增租户套餐', 20, 'system:tenantPackage:add'),
                 ('编辑租户套餐', 30, 'system:tenantPackage:edit'),
                 ('删除租户套餐', 40, 'system:tenantPackage:remove'),
                 ('导出租户套餐', 50, 'system:tenantPackage:export')
         ) AS item(menu_name, sort_order, permission_code)
              ON parent.menu_name = '租户套餐'
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
  AND (m.menu_name IN ('租户管理', '租户套餐') OR m.permission_code LIKE 'system:tenantPackage:%')
  AND NOT EXISTS (
      SELECT 1
      FROM system_role_menus rm
      WHERE rm.role_id = r.id
        AND rm.menu_id = m.id
  );
