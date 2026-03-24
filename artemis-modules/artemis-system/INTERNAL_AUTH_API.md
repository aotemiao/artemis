# 系统服务内部认证 API

本文件描述 `artemis-system` 提供给内部调用方的认证校验 REST 契约，并作为 `scripts/harness/check-api-doc-sync.sh` 的同步来源。

## 路由清单

- `ROUTE: POST /api/system/internal/auth/validate`

## 说明

- `POST /api/system/internal/auth/validate`
  供内部调用方提交用户名与密码，校验成功返回 `userId`，校验失败返回未授权错误。
