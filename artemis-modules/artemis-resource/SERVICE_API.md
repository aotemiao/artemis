# Resource Service API

Status: maintained
Last Reviewed: 2026-03-24
Review Cadence: 90 days

- `ROUTE: GET /api/resource/ping`
- `ROUTE: GET /api/resource/oss-files`
- `ROUTE: POST /api/resource/oss-files/upload`
- `ROUTE: GET /api/resource/oss-files/{id}`
- `ROUTE: GET /api/resource/oss-files/by-ids`
- `ROUTE: GET /api/resource/oss-files/{id}/download`
- `ROUTE: DELETE /api/resource/oss-files/{id}`
- 用途：验证 `artemis-resource` 已经完成最小服务装配
- 返回：`serviceCode` 与 `message`

## OSS 文件管理

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/resource/oss-files` | 分页查询 OSS 文件记录 |
| `POST` | `/api/resource/oss-files/upload` | 上传非空文件，写入本地对象存储并保存文件记录 |
| `GET` | `/api/resource/oss-files/{id}` | 按 ID 查询文件记录 |
| `GET` | `/api/resource/oss-files/by-ids?ids=1&ids=2` | 按 ID 列表查询文件记录 |
| `GET` | `/api/resource/oss-files/{id}/download` | 下载文件内容；文件记录或对象不存在时返回业务错误 |
| `DELETE` | `/api/resource/oss-files/{id}` | 删除文件记录，并尝试删除对象存储中的实际对象 |

当前最小闭环默认使用本地文件系统适配器，`provider` 固定为 `LOCAL`。真实云厂商切换和 OSS 配置管理归入 `RES-002`。
