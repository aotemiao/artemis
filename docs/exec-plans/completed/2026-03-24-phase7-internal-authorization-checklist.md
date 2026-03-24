# Phase 7 Checklist

## 背景

本 checklist 对应 Artemis 在“用户目录 + 角色目录”之后的下一条主线：补齐最小内部授权快照，让内部调用方可以拿到用户基础信息与 `roleKeys`，并让 `artemis-auth` 登录响应直接复用这份结果。

范围参考：

- 本地 `RuoYi-Cloud-Plus` 的 `RemoteUserService` / `LoginUser`
- 本地 `COLA` 的 `client / adapter / app / domain / infra / start` 分层边界

本阶段先做最小授权快照闭环，不一次性扩展到菜单权限、按钮权限和数据权限。

## Checklist

- [x] 为 `artemis-system-client` 新增内部授权快照接口与 DTO
- [x] 在 `artemis-system` 新增用户授权快照查询用例，聚合用户目录与用户-角色绑定
- [x] 新增内部认证 REST / Dubbo 授权查询入口
- [x] 让 `artemis-auth` 新增授权快照客户端，并在登录响应返回 `userId` 与 `roleKeys`
- [x] 回写内部认证 API、client contract、架构与 docs 索引
- [x] 补齐 system / auth 关键路径测试并通过验证

## 完成标准

- 内部调用方可以通过稳定契约获取 `userId`、`username`、`displayName` 与 `roleKeys`
- `artemis-auth` 登录响应在保留现有 token 语义的同时，补充最小授权快照
- 新增契约继续通过 `artemis-api-system` 暴露，不绕开 client / bridge 结构
- 本阶段产物可以直接作为后续菜单、权限点和网关鉴权的基础

## 已交付产物

- `artemis-modules/artemis-system/artemis-system-client/src/main/java/com/aotemiao/artemis/system/client/api/UserAuthorizationService.java`
- `artemis-modules/artemis-system/artemis-system-client/src/main/java/com/aotemiao/artemis/system/client/dto/UserAuthorizationSnapshotDTO.java`
- `artemis-modules/artemis-system/artemis-system-domain/src/main/java/com/aotemiao/artemis/system/domain/model/UserAuthorizationSnapshot.java`
- `artemis-modules/artemis-system/artemis-system-app/src/main/java/com/aotemiao/artemis/system/app/query/GetUserAuthorizationQry.java`
- `artemis-modules/artemis-system/artemis-system-app/src/main/java/com/aotemiao/artemis/system/app/query/GetUserAuthorizationQryExe.java`
- `artemis-modules/artemis-system/artemis-system-adapter/src/main/java/com/aotemiao/artemis/system/adapter/web/InternalAuthController.java`
- `artemis-modules/artemis-system/artemis-system-adapter/src/main/java/com/aotemiao/artemis/system/adapter/dubbo/UserAuthorizationServiceDubboImpl.java`
- `artemis-auth/src/main/java/com/aotemiao/artemis/auth/client/SystemUserAuthorizationClient.java`
- `artemis-auth/src/main/java/com/aotemiao/artemis/auth/web/dto/LoginResponse.java`
- `artemis-auth/src/main/java/com/aotemiao/artemis/auth/web/AuthController.java`
- `artemis-modules/artemis-system/INTERNAL_AUTH_API.md`
- `artemis-modules/artemis-system/artemis-system-client/CLIENT_CONTRACT.md`
- `artemis-auth/AUTH_API.md`

## 验证

- `scripts/harness/verify-changed.sh working-tree`

## 结果

Phase 7 已完成当前仓库范围内的 checklist。`artemis-system` 现在可以对内部调用方稳定提供最小授权快照，`artemis-auth` 也已经把 `userId` 与 `roleKeys` 纳入登录 / 刷新响应，为后续网关 RBAC 和权限点目录演进提供了统一输入。
