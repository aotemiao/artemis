# 系统用户 API

本文件描述 `artemis-system` 对外提供的系统用户目录接口，并作为 `scripts/harness/check-api-doc-sync.sh` 的同步来源。

## 路由清单

- `ROUTE: POST /api/users`
- `ROUTE: PUT /api/users/{id}`
- `ROUTE: GET /api/users/{id}`
- `ROUTE: GET /api/users`

## 说明

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/users` | 新增系统用户，请求体包含 `username`、`displayName`、`password` |
| `PUT` | `/api/users/{id}` | 更新指定用户的 `displayName`、`password` 与 `enabled` |
| `GET` | `/api/users/{id}` | 按 ID 查询系统用户 |
| `GET` | `/api/users` | 分页查询系统用户，使用 `page`、`size` 参数 |
