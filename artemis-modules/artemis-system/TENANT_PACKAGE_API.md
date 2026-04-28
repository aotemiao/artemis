# 租户套餐 API

本文件描述 `artemis-system` 对外提供的租户套餐管理接口，并作为 `scripts/harness/check-api-doc-sync.sh` 的同步来源。

## 路由清单

- `ROUTE: POST /api/tenant-packages`
- `ROUTE: PUT /api/tenant-packages/{id}`
- `ROUTE: PUT /api/tenant-packages/{id}/status`
- `ROUTE: DELETE /api/tenant-packages/{id}`
- `ROUTE: GET /api/tenant-packages/{id}`
- `ROUTE: GET /api/tenant-packages`
- `ROUTE: GET /api/tenant-packages/select`

## 说明

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/tenant-packages` | 新增租户套餐，请求体包含套餐名称、菜单父子联动标记、启用状态、备注和菜单 ID 列表 |
| `PUT` | `/api/tenant-packages/{id}` | 更新指定租户套餐 |
| `PUT` | `/api/tenant-packages/{id}/status` | 启用或停用指定租户套餐 |
| `DELETE` | `/api/tenant-packages/{id}` | 逻辑删除指定租户套餐；已被租户使用的套餐不允许删除 |
| `GET` | `/api/tenant-packages/{id}` | 按 ID 查询租户套餐 |
| `GET` | `/api/tenant-packages` | 分页查询租户套餐，使用 `page`、`size` 参数 |
| `GET` | `/api/tenant-packages/select` | 查询启用状态的租户套餐下拉列表 |

导出能力后置到导入导出专项；首批补齐基础 CRUD、套餐名称唯一、菜单授权持久化和租户引用删除保护入口。
