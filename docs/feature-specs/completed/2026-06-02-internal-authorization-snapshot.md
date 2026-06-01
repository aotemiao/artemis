# 内部授权快照 Feature Spec

Status: completed
Last Reviewed: 2026-06-02
Review Cadence: 90 days

## 背景

用户目录和角色绑定完成后，认证服务和网关需要一份稳定的内部授权快照，而不是各自拼装用户、角色和权限上下文。Phase 7 已让 `artemis-system-client` 暴露用户授权快照，并让 `artemis-auth` 登录与刷新响应复用该结果。

本 Feature Spec 从 `docs/exec-plans/completed/2026-03-24-phase7-internal-authorization-checklist.md` 归档而来。

## 目标

- 提供稳定的内部授权快照契约。
- 聚合用户基础信息与角色 key。
- 让认证服务登录 / 刷新响应包含最小授权上下文。

## 非目标

- 不实现完整菜单权限、按钮权限和数据权限。
- 不改变 token 签发、刷新和失效语义。
- 不允许认证服务直接访问系统服务数据库。

## 用户故事

| 编号 | 作为 | 我希望 | 以便 |
|------|------|--------|------|
| US-001 | 认证服务 | 通过内部契约获取用户授权快照 | 在登录响应和会话中写入稳定授权上下文 |
| US-002 | 网关 | 复用认证阶段产生的角色信息 | 执行最小 RBAC |
| US-003 | 后续权限能力 | 基于同一份授权快照扩展权限码 | 避免多个服务重复计算授权上下文 |

## 业务规则

| 编号 | 规则 | 说明 |
|------|------|------|
| BR-001 | 授权快照必须包含 `userId`、`username`、`displayName` 和 `roleKeys` | 满足认证与最小 RBAC 输入 |
| BR-002 | 授权快照由系统服务聚合用户目录与用户角色绑定 | 系统服务是用户与角色事实来源 |
| BR-003 | 认证服务只能通过 `artemis-system-client` 获取授权快照 | 保持服务边界 |
| BR-004 | 登录和刷新语义应保持兼容 | 新增字段不能破坏既有 token 响应 |

## 数据与接口影响

| 类型 | 是否影响 | 说明 |
|------|----------|------|
| 数据模型 | 是 | 用户授权快照领域模型与 DTO |
| REST API | 是 | 内部认证授权查询入口 |
| 内部 RPC / client | 是 | `UserAuthorizationService` 与快照 DTO |
| 权限 / 审计 | 是 | 授权快照是后续权限判断输入 |
| 配置 / 部署 | 否 | 不新增运行配置 |

## 验收标准

| 编号 | 验收标准 | 验证方式 |
|------|----------|----------|
| AC-001 | 内部调用方可以获取用户基础信息与 `roleKeys` | system / client 测试 |
| AC-002 | 认证登录响应包含 `userId` 与 `roleKeys` | auth 关键路径测试 |
| AC-003 | 刷新 token 时保留授权快照语义 | auth smoke / 测试 |
| AC-004 | 内部授权 API 与 client contract 文档同步 | 契约检查 |
| AC-005 | 授权快照实现符合服务边界与 DDD/COLA 分层约束 | harness / Maven verify |

## 验证映射

| 验收编号 | 验证入口 | 通过标准 |
|----------|----------|----------|
| AC-001 | `scripts/harness/check-critical-path-tests.sh` | 内部授权快照查询测试基线存在并通过 |
| AC-002 | `scripts/harness/check-critical-path-tests.sh` | 登录响应授权字段测试基线存在并通过 |
| AC-003 | `scripts/smoke/auth-refresh.sh` | auth refresh 端点 smoke 可达并返回允许的状态码 |
| AC-004 | `scripts/harness/check-client-contracts.sh` | `CLIENT_CONTRACT.md` 与 client DTO / API 保持同步 |
| AC-005 | `scripts/harness/verify-changed.sh staged` | 分层、契约、文档和测试守门通过 |

## 关联资产

- OpenSpec：`openspec/specs/repository-structure/spec.md`
- OpenSpec：`openspec/specs/contract-doc-guardrails/spec.md`
- 执行计划：`docs/exec-plans/completed/2026-03-24-phase7-internal-authorization-checklist.md`
- API 文档：`artemis-modules/artemis-system/INTERNAL_AUTH_API.md`
- API 文档：`artemis-modules/artemis-system/artemis-system-client/CLIENT_CONTRACT.md`
- API 文档：`artemis-auth/AUTH_API.md`
- Runbook：`docs/runbooks/SERVICE_SMOKE_RUNBOOK.md`

## 决策记录

- `2026-06-02`：将 Phase 7 已交付能力补录为 completed Feature Spec，作为认证、网关 RBAC 与权限码扩展的统一授权上下文基线。

## 遗留问题

- 授权快照已扩展权限码，但按钮权限、数据权限和租户维度仍需后续 Feature Spec 继续细化。
