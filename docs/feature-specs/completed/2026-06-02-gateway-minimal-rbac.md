# 网关最小 RBAC Feature Spec

Status: completed
Last Reviewed: 2026-06-02
Review Cadence: 90 days

## 背景

授权快照只停留在登录响应中并不能形成运行时保护。Phase 8 已让 `artemis-auth` 把角色信息写入 Sa-Token 会话，并让 `artemis-gateway` 基于同一份会话角色数据执行最小 RBAC、阻断内部接口并透传 `X-Role-Keys`。

本 Feature Spec 从 `docs/exec-plans/completed/2026-03-25-phase8-gateway-rbac-checklist.md` 归档而来。

## 目标

- 让登录态中的角色数据成为网关运行时鉴权输入。
- 对用户 / 角色目录形成最小 RBAC 闭环。
- 阻断内部系统接口经网关对外暴露。
- 向下游透传角色上下文。

## 非目标

- 不实现完整权限码、按钮权限和数据权限拦截。
- 不定义所有业务路由的授权矩阵。
- 不改变 Sa-Token 的基础登录态机制。

## 用户故事

| 编号 | 作为 | 我希望 | 以便 |
|------|------|--------|------|
| US-001 | 平台管理员 | 使用管理员角色访问系统管理接口 | 完成用户和角色维护 |
| US-002 | 非授权用户 | 访问受保护系统管理接口时被拒绝 | 避免越权访问 |
| US-003 | 下游服务 | 接收网关透传的角色 key | 在必要时做服务内补充判断 |

## 业务规则

| 编号 | 规则 | 说明 |
|------|------|------|
| BR-001 | 网关从统一会话读取 `roleKeys` | 不在下游服务重复解析 token |
| BR-002 | 用户 / 角色目录至少受管理员角色保护 | 形成最小 RBAC 闭环 |
| BR-003 | 内部认证接口不得经网关默认暴露 | 防止绕过内部调用边界 |
| BR-004 | 网关向下游透传 `X-Role-Keys` | 保留服务内扩展判断能力 |

## 数据与接口影响

| 类型 | 是否影响 | 说明 |
|------|----------|------|
| 数据模型 | 否 | 复用授权快照与会话角色数据 |
| REST API | 是 | 网关路由授权行为变化 |
| 内部 RPC / client | 否 | 不新增内部 client |
| 权限 / 审计 | 是 | 网关承担最小 RBAC 与内部接口阻断 |
| 配置 / 部署 | 是 | 网关授权策略与 smoke 验证入口 |

## 验收标准

| 编号 | 验收标准 | 验证方式 |
|------|----------|----------|
| AC-001 | 登录 / 刷新后的会话可被网关读取到 `roleKeys` | auth / gateway 测试 |
| AC-002 | 管理员可以访问用户 / 角色目录保护路由 | gateway smoke / 测试 |
| AC-003 | 无权限访问受保护路由会被拒绝 | gateway 策略测试 |
| AC-004 | 内部系统认证接口经网关访问时被阻断 | gateway smoke / 测试 |
| AC-005 | 网关授权文档登记白名单、透传头和内部接口阻断规则 | 文档同步检查 |

## 验证映射

| 验收编号 | 验证入口 | 通过标准 |
|----------|----------|----------|
| AC-001 | `scripts/harness/check-critical-path-tests.sh` | auth / gateway 会话角色读取测试基线存在并通过 |
| AC-002 | `scripts/smoke/gateway-system-admin.sh` | 管理员登录后访问系统管理路由返回 `200` |
| AC-003 | `scripts/harness/check-critical-path-tests.sh` | 网关授权策略拒绝无权限访问的测试基线存在并通过 |
| AC-004 | `scripts/smoke/gateway-system-admin.sh` | 内部系统认证接口经网关访问返回 `403` |
| AC-005 | `scripts/harness/check-api-doc-sync.sh` | `artemis-gateway/GATEWAY_AUTHORIZATION.md` 与网关规则保持同步 |

## 关联资产

- OpenSpec：`openspec/specs/engineering-constraints/spec.md`
- 执行计划：`docs/exec-plans/completed/2026-03-25-phase8-gateway-rbac-checklist.md`
- API 文档：`artemis-gateway/GATEWAY_AUTHORIZATION.md`
- Runbook：`docs/runbooks/SERVICE_SMOKE_RUNBOOK.md`
- Smoke：`scripts/smoke/gateway-system-admin.sh`

## 决策记录

- `2026-06-02`：将 Phase 8 已交付能力补录为 completed Feature Spec，作为网关统一鉴权和后续权限码检查的运行时基线。

## 遗留问题

- 仍需在权限码、菜单按钮权限和数据权限完成后扩展更完整的网关授权矩阵。
