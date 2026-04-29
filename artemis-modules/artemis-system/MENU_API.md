# 系统菜单 API

本文件描述 `artemis-system` 对外提供的系统菜单与权限点接口，并作为 `scripts/harness/check-api-doc-sync.sh` 的同步来源。

## 路由清单

- `ROUTE: POST /api/menus`
- `ROUTE: PUT /api/menus/{id}`
- `ROUTE: DELETE /api/menus/{id}`
- `ROUTE: GET /api/menus/{id}`
- `ROUTE: GET /api/menus`
- `ROUTE: GET /api/menus/tree`
- `ROUTE: GET /api/menus/tree/roles/{roleId}`
- `ROUTE: GET /api/menus/tree/tenant-packages/{packageId}`
- `ROUTE: GET /api/menus/routes/users/{userId}`

## 说明

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/menus` | 新增系统菜单或按钮权限点，请求体包含 `menuType`、`menuName`、`parentId`、`sortOrder`、`path`、`component`、`queryParam`、`externalLink`、`cacheable`、`permissionCode`、`icon`、`visible`、`remarks` 等字段 |
| `PUT` | `/api/menus/{id}` | 更新指定菜单的父级、类型、名称、排序、路由、组件、查询参数、外链标记、缓存标记、权限码、图标、可见状态、启用状态与备注 |
| `DELETE` | `/api/menus/{id}` | 级联逻辑删除指定菜单及其子菜单，并清理角色菜单和租户套餐菜单绑定 |
| `GET` | `/api/menus/{id}` | 按 ID 查询系统菜单 |
| `GET` | `/api/menus` | 查询系统菜单列表，按父级、排序和 ID 稳定排序 |
| `GET` | `/api/menus/tree` | 查询完整菜单树，用于菜单树下拉与授权配置 |
| `GET` | `/api/menus/tree/roles/{roleId}` | 查询角色菜单授权树，返回完整菜单树并标记角色已选菜单 |
| `GET` | `/api/menus/tree/tenant-packages/{packageId}` | 查询租户套餐菜单授权树，返回完整菜单树并标记套餐已选菜单 |
| `GET` | `/api/menus/routes/users/{userId}` | 按用户角色授权生成前端路由树，仅输出启用、可见、非按钮菜单，并补齐已授权菜单的父级路径 |

当前 MVP 使用 `system_menus` 与 `system_role_menus` 表承载需求文档中的 `sys_menu` / `sys_role_menu` 能力。`menuType` 支持 `DIRECTORY`、`MENU`、`BUTTON`，其中非按钮菜单的 `path` 需要保持唯一。

按钮权限点不进入前端路由树，仍通过用户授权快照中的 `permissionCodes` 输出给前端按钮级鉴权使用。
