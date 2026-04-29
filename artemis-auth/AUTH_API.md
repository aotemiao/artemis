# 认证服务 API

本文件描述 `artemis-auth` 当前对外暴露的认证接口，并作为 `scripts/harness/check-api-doc-sync.sh` 的同步来源。

## 路由清单

- `ROUTE: POST /auth/login`
- `ROUTE: POST /auth/logout`
- `ROUTE: GET /auth/online-users`
- `ROUTE: POST /auth/online-users/{userId}/force-logout`
- `ROUTE: POST /auth/register`
- `ROUTE: POST /auth/refresh`
- `ROUTE: POST /auth/tenant/switch`
- `ROUTE: POST /auth/tenant/clear`

## 说明

- `POST /auth/login`
  请求体为 `tenantId`、`clientId`、`grantType`、`username`、`password`，成功返回当前登录 Token、`userId`、`roleKeys` 与 `permissionCodes`，并把最小授权快照同步进当前登录会话。`tenantId` 可由请求体、`X-Tenant-Id` 请求头或超级管理员动态租户会话提供。
- `POST /auth/logout`
  使当前 Token 对应会话失效，成功返回 `204 No Content`。
- `GET /auth/online-users`
  按 `username`、`ipaddr` 查询当前认证实例内的在线用户。
- `POST /auth/online-users/{userId}/force-logout`
  强退指定用户并移除在线用户索引。
- `POST /auth/register`
  请求体为 `tenantId`、`clientId`、`grantType`、`username`、`password`、`userType`，系统服务校验注册开关和用户类型后创建用户，并记录注册日志。
- `POST /auth/refresh`
  用于续期当前登录态，并刷新当前会话中的最小授权快照后返回 `userId`、`roleKeys` 与 `permissionCodes`；未登录时返回鉴权失败状态。
- `POST /auth/tenant/switch`
  超级管理员通过 `tenantId` 参数在当前登录会话中设置动态租户编号。
- `POST /auth/tenant/clear`
  超级管理员清除当前登录会话中的动态租户编号。
