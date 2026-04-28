# 通知公告 API

本文件描述 `artemis-system` 对外提供的通知公告接口，并作为 `scripts/harness/check-api-doc-sync.sh` 的同步来源。

## 路由清单

- `ROUTE: POST /api/notices`
- `ROUTE: PUT /api/notices/{id}`
- `ROUTE: DELETE /api/notices/{id}`
- `ROUTE: GET /api/notices/{id}`
- `ROUTE: GET /api/notices`

## 说明

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/notices` | 新增通知公告，请求体包含 `noticeTitle`、`noticeType`、`noticeContent`、`status`、`remarks` |
| `PUT` | `/api/notices/{id}` | 更新指定通知公告 |
| `DELETE` | `/api/notices/{id}` | 逻辑删除指定通知公告 |
| `GET` | `/api/notices/{id}` | 按 ID 查询通知公告 |
| `GET` | `/api/notices` | 分页查询通知公告，使用 `page`、`size` 参数 |

表结构由 Flyway 在应用启动时自动应用（`db/migration` 下的脚本），无需手动执行 SQL 或 Flyway CLI。
