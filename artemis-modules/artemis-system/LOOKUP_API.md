# 字典类型 API

本文件描述 `artemis-system` 对外提供的字典类型接口，并作为 `scripts/harness/check-api-doc-sync.sh` 的同步来源。

## 路由清单

- `ROUTE: POST /api/lookup-types`
- `ROUTE: PUT /api/lookup-types/{id}`
- `ROUTE: DELETE /api/lookup-types/{id}`
- `ROUTE: GET /api/lookup-types/{id}`
- `ROUTE: GET /api/lookup-types`
- `ROUTE: GET /api/lookup-types/{typeCode}/items`

## 说明

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/lookup-types` | 新增字典类型，请求体包含 `code`、`name`、`description` 与 `items[]` |
| `PUT` | `/api/lookup-types/{id}` | 更新指定字典类型 |
| `DELETE` | `/api/lookup-types/{id}` | 逻辑删除指定字典类型 |
| `GET` | `/api/lookup-types/{id}` | 按 ID 查询字典类型 |
| `GET` | `/api/lookup-types` | 分页查询字典类型，使用 `page`、`size` 参数 |
| `GET` | `/api/lookup-types/{typeCode}/items` | 按 `typeCode` 查询字典项列表 |

表结构由 Flyway 在应用启动时自动应用（`db/migration` 下的脚本），无需手动执行 SQL 或 Flyway CLI。
