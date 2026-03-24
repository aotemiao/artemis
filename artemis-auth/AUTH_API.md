# 认证服务 API

本文件描述 `artemis-auth` 当前对外暴露的认证接口，并作为 `scripts/harness/check-api-doc-sync.sh` 的同步来源。

## 路由清单

- `ROUTE: POST /auth/login`
- `ROUTE: POST /auth/logout`
- `ROUTE: POST /auth/refresh`

## 说明

- `POST /auth/login`
  请求体为 `username`、`password`，成功返回当前登录 Token、`userId` 与 `roleKeys`，并把最小授权快照同步进当前登录会话。
- `POST /auth/logout`
  使当前 Token 对应会话失效，成功返回 `204 No Content`。
- `POST /auth/refresh`
  用于续期当前登录态，并刷新当前会话中的最小授权快照后返回 `userId` 与 `roleKeys`；未登录时返回鉴权失败状态。
