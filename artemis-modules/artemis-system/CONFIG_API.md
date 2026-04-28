# 系统参数 API

本文件描述 `artemis-system` 对外提供的系统参数配置接口，并作为 `scripts/harness/check-api-doc-sync.sh` 的同步来源。

## 路由清单

- `ROUTE: POST /api/configs`
- `ROUTE: PUT /api/configs/{id}`
- `ROUTE: PUT /api/configs/key/{configKey}`
- `ROUTE: DELETE /api/configs/{id}`
- `ROUTE: GET /api/configs/{id}`
- `ROUTE: GET /api/configs`
- `ROUTE: GET /api/configs/key/{configKey}`
- `ROUTE: POST /api/configs/cache/refresh`

## 说明

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/configs` | 新增系统参数，请求体包含 `configName`、`configKey`、`configValue`、`systemBuiltIn`、`remarks` |
| `PUT` | `/api/configs/{id}` | 更新指定系统参数 |
| `PUT` | `/api/configs/key/{configKey}` | 按参数 key 更新参数值 |
| `DELETE` | `/api/configs/{id}` | 逻辑删除指定系统参数；系统内置参数不允许删除 |
| `GET` | `/api/configs/{id}` | 按 ID 查询系统参数 |
| `GET` | `/api/configs` | 分页查询系统参数，使用 `page`、`size` 参数 |
| `GET` | `/api/configs/key/{configKey}` | 按参数 key 查询参数值 |
| `POST` | `/api/configs/cache/refresh` | 刷新系统参数本地缓存 |

表结构由 Flyway 在应用启动时自动应用（`db/migration` 下的脚本），无需手动执行 SQL 或 Flyway CLI。
