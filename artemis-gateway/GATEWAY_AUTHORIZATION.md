# 网关授权约定

本文件描述 `artemis-gateway` 当前的最小 RBAC 与请求头透传约定，供开发者、agent 与下游服务统一参考。

## 白名单

- `/auth/**`
- `/public/**`

白名单路径不要求登录，也不会注入用户上下文头。

## 登录后透传头

- `X-User-Id`
  当前登录用户 ID，由 `UserIdHeaderGatewayFilter` 注入。
- `X-Role-Keys`
  当前登录用户角色键列表，按逗号拼接，由 `RoleKeysHeaderGatewayFilter` 注入。

角色来源不是每次请求都重新查 `artemis-system`，而是复用 `artemis-auth` 在登录 / 刷新时同步到 Sa-Token 会话中的 `roleKeys`。

## 最小 RBAC

- `/api/system/users/**`
  要求 `super-admin`
- `/api/system/roles/**`
  要求 `super-admin`
- `/api/system/internal/**`
  不经网关对外暴露，统一返回 `403`

其他非白名单路由仍要求登录，但暂不做更细粒度角色 / 权限点判定。

## Smoke

- `scripts/smoke/gateway-auth-refresh.sh`
  验证网关认证路由可达
- `scripts/smoke/gateway-system-admin.sh`
  验证管理员可访问系统管理路由，且内部接口经网关被阻断
