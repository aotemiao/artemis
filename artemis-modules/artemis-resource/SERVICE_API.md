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
- `ROUTE: GET /api/resource/oss-configs`
- `ROUTE: GET /api/resource/oss-configs/{id}`
- `ROUTE: POST /api/resource/oss-configs`
- `ROUTE: PUT /api/resource/oss-configs/{id}`
- `ROUTE: PUT /api/resource/oss-configs/{id}/status`
- `ROUTE: PUT /api/resource/oss-configs/{id}/default`
- `ROUTE: DELETE /api/resource/oss-configs/{id}`
- `ROUTE: POST /api/resource/messages/user`
- `ROUTE: POST /api/resource/messages/broadcast`
- `ROUTE: GET /api/resource/messages/inbox`
- `ROUTE: PUT /api/resource/messages/{id}/read`
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

当前文件上传最小闭环默认使用本地文件系统适配器，`provider` 固定为 `LOCAL`。真实云厂商 SDK 切换、签名直传和预览 URL 策略仍后置推进。

## OSS 配置管理

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/resource/oss-configs` | 分页查询 OSS 配置 |
| `GET` | `/api/resource/oss-configs/{id}` | 查询 OSS 配置详情 |
| `POST` | `/api/resource/oss-configs` | 创建 OSS 配置，`configKey` 唯一 |
| `PUT` | `/api/resource/oss-configs/{id}` | 修改 OSS 配置 |
| `PUT` | `/api/resource/oss-configs/{id}/status` | 启用或停用 OSS 配置；停用时自动取消默认 |
| `PUT` | `/api/resource/oss-configs/{id}/default` | 将启用配置设为默认，并取消其他默认配置 |
| `DELETE` | `/api/resource/oss-configs/{id}` | 删除 OSS 配置；内置配置不允许删除 |

配置字段包含 `configKey`、`accessKey`、`secretKey`、`bucket`、`prefix`、`endpoint`、`customDomain`、`httpsEnabled`、`region`、`accessPolicy`、`status`、`defaultFlag`、`builtIn`、`provider` 和 `extJson`。

## 站内消息

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/resource/messages/user` | 向指定用户发布站内消息 |
| `POST` | `/api/resource/messages/broadcast` | 按用户 ID 列表发布全员/批量站内消息 |
| `GET` | `/api/resource/messages/inbox?recipientUserId=1` | 查询用户收件箱 |
| `PUT` | `/api/resource/messages/{id}/read?recipientUserId=1` | 将指定用户的消息标记为已读 |

远程 `ResourceMessageService` 同步提供指定用户发布与批量发布能力，供工作流、登录欢迎消息等服务复用。
