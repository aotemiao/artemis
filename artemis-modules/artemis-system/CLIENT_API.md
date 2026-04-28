# 客户端 API

本文件描述 `artemis-system` 对外提供的客户端管理接口，并作为 `scripts/harness/check-api-doc-sync.sh` 的同步来源。

## 路由清单

- `ROUTE: POST /api/clients`
- `ROUTE: PUT /api/clients/{id}`
- `ROUTE: DELETE /api/clients/{id}`
- `ROUTE: GET /api/clients/{id}`
- `ROUTE: GET /api/clients`

## 说明

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/clients` | 新增客户端，请求体包含客户端 ID、key、secret、授权类型、设备类型、token 超时、状态和备注 |
| `PUT` | `/api/clients/{id}` | 更新指定客户端，支持启停 |
| `DELETE` | `/api/clients/{id}` | 逻辑删除指定客户端 |
| `GET` | `/api/clients/{id}` | 按 ID 查询客户端 |
| `GET` | `/api/clients` | 分页查询客户端，使用 `page`、`size` 参数 |

登录入口会在校验用户名密码前验证客户端存在、状态正常，并且 `grantTypes` 包含本次登录的授权类型。
