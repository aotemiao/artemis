# 系统服务内部认证 API

本文件描述 `artemis-system` 提供给内部调用方的认证 / 授权 REST 契约，并作为 `scripts/harness/check-api-doc-sync.sh` 的同步来源。

## 路由清单

- `ROUTE: POST /api/system/internal/auth/validate`
- `ROUTE: POST /api/system/internal/auth/clients/validate`
- `ROUTE: GET /api/system/internal/auth/users/{userId}/authorization`

## 说明

- `POST /api/system/internal/auth/validate`
  供内部调用方提交客户端、授权类型、用户名与密码，先校验客户端授权配置，再基于 `system_users` 表校验成功返回 `userId`，校验失败返回未授权错误。
- `POST /api/system/internal/auth/clients/validate`
  供内部调用方校验客户端存在、状态正常，并且支持指定授权类型。
- `GET /api/system/internal/auth/users/{userId}/authorization`
  供内部调用方按 `userId` 查询最小授权快照，返回 `userId`、`username`、`displayName`、启用中的 `roleKeys` 与 `permissionCodes`。
