# 系统角色 API

本文件描述 `artemis-system` 对外提供的系统角色目录接口，并作为 `scripts/harness/check-api-doc-sync.sh` 的同步来源。

## 路由清单

- `ROUTE: POST /api/roles`
- `ROUTE: PUT /api/roles/{id}`
- `ROUTE: GET /api/roles/{id}`
- `ROUTE: GET /api/roles`

## 说明

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/roles` | 新增系统角色，请求体包含 `roleKey`、`roleName` |
| `PUT` | `/api/roles/{id}` | 更新指定角色的 `roleKey`、`roleName` 与 `enabled` |
| `GET` | `/api/roles/{id}` | 按 ID 查询系统角色 |
| `GET` | `/api/roles` | 分页查询系统角色，使用 `page`、`size` 参数 |
