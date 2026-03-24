# Phase 7 Checklist

## 背景

本 checklist 对应 Artemis 在“用户目录 + 角色目录”之后的下一条主线：补齐最小内部授权快照，让内部调用方可以拿到用户基础信息与 `roleKeys`，并让 `artemis-auth` 登录响应直接复用这份结果。

范围参考：

- 本地 `RuoYi-Cloud-Plus` 的 `RemoteUserService` / `LoginUser`
- 本地 `COLA` 的 `client / adapter / app / domain / infra / start` 分层边界

本阶段先做最小授权快照闭环，不一次性扩展到菜单权限、按钮权限和数据权限。

## Checklist

- [ ] 为 `artemis-system-client` 新增内部授权快照接口与 DTO
- [ ] 在 `artemis-system` 新增用户授权快照查询用例，聚合用户目录与用户-角色绑定
- [ ] 新增内部认证 REST / Dubbo 授权查询入口
- [ ] 让 `artemis-auth` 新增授权快照客户端，并在登录响应返回 `userId` 与 `roleKeys`
- [ ] 回写内部认证 API、client contract、架构与 docs 索引
- [ ] 补齐 system / auth 关键路径测试并通过验证

## 完成标准

- 内部调用方可以通过稳定契约获取 `userId`、`username`、`displayName` 与 `roleKeys`
- `artemis-auth` 登录响应在保留现有 token 语义的同时，补充最小授权快照
- 新增契约继续通过 `artemis-api-system` 暴露，不绕开 client / bridge 结构
- 本阶段产物可以直接作为后续菜单、权限点和网关鉴权的基础
