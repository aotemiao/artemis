# Phase 8 Checklist

## 背景

本 checklist 对应 Artemis 在“内部授权快照可用”之后的下一条主线：让 `artemis-gateway` 真正消费 `roleKeys`，形成最小 RBAC 闭环，而不是只做“登录态存在”校验。

范围参考：

- 本地 `RuoYi-Cloud-Plus` 的 `ruoyi-gateway/AuthFilter` 与 `ruoyi-common-satoken/SaPermissionImpl`
- 本地 `COLA` 的最小闭环原则：优先形成端到端价值，再逐步扩展细粒度权限点

本阶段先做最小治理闭环，不一次性扩展到菜单树、按钮权限和数据权限。

## Checklist

- [x] 让 `artemis-auth` 在登录 / 刷新时把最小授权快照同步到 Sa-Token 会话
- [x] 为 `artemis-gateway` 新增基于会话的角色读取与角色请求头透传
- [x] 为 `artemis-gateway` 新增最小 RBAC 路由策略，至少覆盖用户 / 角色目录并阻断内部接口经网关暴露
- [x] 新增 gateway 授权文档、smoke 入口，并回写架构与 docs 索引
- [x] 补齐 auth / gateway 关键路径测试并通过验证

## 完成标准

- 登录态中的 `roleKeys` 不只存在于登录响应，也能被后续请求稳定复用
- `artemis-gateway` 能基于统一角色来源做最小 RBAC，而不是把鉴权散落到下游服务
- 用户 / 角色目录至少具备一个“登录可过、无权限被拒、管理员可访问”的清晰闭环
- 内部接口不会再通过网关默认暴露给外部调用方

## 已交付产物

- `artemis-auth/src/main/java/com/aotemiao/artemis/auth/web/AuthController.java`
- `artemis-gateway/src/main/java/com/aotemiao/artemis/gateway/config/GatewayAuthorizationPolicy.java`
- `artemis-gateway/src/main/java/com/aotemiao/artemis/gateway/config/SessionBackedStpInterface.java`
- `artemis-gateway/src/main/java/com/aotemiao/artemis/gateway/config/SaTokenGatewayConfig.java`
- `artemis-gateway/src/main/java/com/aotemiao/artemis/gateway/filter/RoleKeysHeaderGatewayFilter.java`
- `artemis-gateway/src/test/java/com/aotemiao/artemis/gateway/config/GatewayAuthorizationPolicyTest.java`
- `artemis-gateway/src/test/java/com/aotemiao/artemis/gateway/filter/RoleKeysHeaderGatewayFilterTest.java`
- `artemis-gateway/GATEWAY_AUTHORIZATION.md`
- `scripts/smoke/gateway-system-admin.sh`

## 验证

- `scripts/harness/verify-changed.sh working-tree`

## 结果

Phase 8 已完成当前仓库范围内的 checklist。`artemis-auth` 现在会把最小授权快照同步进 Sa-Token 会话，`artemis-gateway` 则可以直接基于同一份会话角色数据完成最小 RBAC、阻断内部接口并向下游透传 `X-Role-Keys`，从而把此前的 `roleKeys` 响应字段真正变成运行态能力。
