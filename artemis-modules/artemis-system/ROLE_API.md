# 系统角色 API

本文件描述 `artemis-system` 对外提供的系统角色目录接口，并作为 `scripts/harness/check-api-doc-sync.sh` 的同步来源。

## 路由清单

- `ROUTE: POST /api/roles`
- `ROUTE: PUT /api/roles/{id}`
- `ROUTE: GET /api/roles/{id}`
- `ROUTE: GET /api/roles`
- `ROUTE: GET /api/roles/{roleId}/menus`
- `ROUTE: PUT /api/roles/{roleId}/menus`
- `ROUTE: GET /api/roles/{roleId}/departments`
- `ROUTE: PUT /api/roles/{roleId}/data-scope`

## 说明

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/roles` | 新增系统角色，请求体包含 `roleKey`、`roleName` 与可选 `dataScope` |
| `PUT` | `/api/roles/{id}` | 更新指定角色的 `roleKey`、`roleName`、`dataScope` 与 `enabled` |
| `GET` | `/api/roles/{id}` | 按 ID 查询系统角色 |
| `GET` | `/api/roles` | 分页查询系统角色，使用 `page`、`size` 参数 |
| `GET` | `/api/roles/{roleId}/menus` | 查询指定角色当前绑定的菜单与权限点列表 |
| `PUT` | `/api/roles/{roleId}/menus` | 批量替换指定角色的菜单绑定，请求体包含 `menuIds` |
| `GET` | `/api/roles/{roleId}/departments` | 查询指定角色当前绑定的数据权限部门 ID 列表 |
| `PUT` | `/api/roles/{roleId}/data-scope` | 设置指定角色的数据权限范围，并替换角色部门绑定；请求体包含 `dataScope`、`departmentIds` |
