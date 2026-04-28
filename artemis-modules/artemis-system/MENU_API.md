# 系统菜单 API

本文件描述 `artemis-system` 对外提供的系统菜单与权限点接口，并作为 `scripts/harness/check-api-doc-sync.sh` 的同步来源。

## 路由清单

- `ROUTE: POST /api/menus`
- `ROUTE: PUT /api/menus/{id}`
- `ROUTE: GET /api/menus/{id}`
- `ROUTE: GET /api/menus`

## 说明

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/menus` | 新增系统菜单或按钮权限点，请求体包含 `menuType`、`menuName`、`parentId`、`path`、`component`、`permissionCode` 等字段 |
| `PUT` | `/api/menus/{id}` | 更新指定菜单的父级、类型、名称、排序、路由、组件、权限码、可见状态与启用状态 |
| `GET` | `/api/menus/{id}` | 按 ID 查询系统菜单 |
| `GET` | `/api/menus` | 查询系统菜单列表，按父级、排序和 ID 稳定排序 |

当前 MVP 使用 `system_menus` 与 `system_role_menus` 表承载需求文档中的 `sys_menu` / `sys_role_menu` 能力。`menuType` 支持 `DIRECTORY`、`MENU`、`BUTTON`，其中非按钮菜单的 `path` 需要保持唯一。
