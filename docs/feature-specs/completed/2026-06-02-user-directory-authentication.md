# 用户目录与真实凭证认证 Feature Spec

Status: completed
Last Reviewed: 2026-06-02
Review Cadence: 90 days

## 背景

Artemis 早期认证链路依赖固定账号 stub，只能证明认证服务和系统服务之间的调用路径存在，不能支撑真实后台管理用户。Phase 5 已将 `artemis-system` 升级为真实用户目录，并让 `artemis-auth` 继续通过内部契约完成认证。

本 Feature Spec 从 `docs/exec-plans/completed/2026-03-24-phase5-user-directory-checklist.md` 归档而来，用于把已交付的用户目录和真实凭证认证能力纳入业务需求级 Spec 体系。

## 目标

- 建立系统用户的最小目录能力。
- 用真实用户表替代硬编码账号。
- 保持 `artemis-auth -> artemis-system-client -> artemis-system` 认证契约稳定。

## 非目标

- 不实现完整组织、岗位、租户隔离和数据权限。
- 不定义前端用户管理页面。
- 不改变认证 token 的签发与刷新语义。

## 用户故事

| 编号 | 作为 | 我希望 | 以便 |
|------|------|--------|------|
| US-001 | 系统管理员 | 创建和维护后台系统用户 | 管理可登录 Artemis 后台的账号 |
| US-002 | 登录用户 | 使用数据库中的账号密码登录 | 不再依赖固定测试账号 |
| US-003 | 认证服务 | 通过系统服务契约校验用户凭证 | 保持认证与用户目录职责分离 |

## 业务规则

| 编号 | 规则 | 说明 |
|------|------|------|
| BR-001 | 用户名在系统用户目录中唯一 | 避免登录身份歧义 |
| BR-002 | 用户凭证校验必须读取系统用户目录 | 不允许继续依赖硬编码账号 stub |
| BR-003 | 用户目录至少支持创建、更新、按 ID 查询和分页查询 | 满足后台管理 MVP |
| BR-004 | 认证服务只能通过内部 client 契约读取用户凭证 | 不绕过系统服务访问数据库 |

## 数据与接口影响

| 类型 | 是否影响 | 说明 |
|------|----------|------|
| 数据模型 | 是 | 系统用户表、用户领域模型、用户持久化网关 |
| REST API | 是 | 用户 create / update / getById / page |
| 内部 RPC / client | 是 | 用户凭证校验继续经 `artemis-system-client` 暴露 |
| 权限 / 审计 | 是 | 用户目录是后台管理能力，后续应接入操作日志 |
| 配置 / 部署 | 否 | 不新增运行配置 |

## 验收标准

| 编号 | 验收标准 | 验证方式 |
|------|----------|----------|
| AC-001 | 系统用户目录支持新增、修改、按 ID 查询和分页查询 | system 模块测试 |
| AC-002 | 用户凭证校验读取真实用户表，不再依赖固定账号 stub | auth / system 关键路径测试 |
| AC-003 | 认证服务通过内部契约校验用户凭证 | client contract 检查 |
| AC-004 | 用户 API 文档登记当前 REST 路由 | API 文档同步检查 |
| AC-005 | 用户目录实现符合 DDD/COLA 分层约束 | harness / Maven verify |

## 验证映射

| 验收编号 | 验证入口 | 通过标准 |
|----------|----------|----------|
| AC-001 | `scripts/harness/check-critical-path-tests.sh` | 用户目录 app / infra / adapter 关键路径测试基线存在并通过 |
| AC-002 | `scripts/harness/check-critical-path-tests.sh` | 真实用户凭证校验相关测试基线存在并通过 |
| AC-003 | `scripts/harness/check-client-contracts.sh` | 用户凭证内部契约与 client 文档保持同步 |
| AC-004 | `scripts/harness/check-api-doc-sync.sh` | `artemis-modules/artemis-system/USER_API.md` 与 Controller 路由保持同步 |
| AC-005 | `scripts/harness/verify-changed.sh staged` | 分层、契约、文档和测试守门通过 |

## 关联资产

- OpenSpec：`openspec/specs/ddd-cola-layering/spec.md`
- OpenSpec：`openspec/specs/contract-doc-guardrails/spec.md`
- 执行计划：`docs/exec-plans/completed/2026-03-24-phase5-user-directory-checklist.md`
- API 文档：`artemis-modules/artemis-system/USER_API.md`
- API 文档：`artemis-modules/artemis-system/INTERNAL_AUTH_API.md`
- Runbook：无

## 决策记录

- `2026-06-02`：将 Phase 5 已交付能力补录为 completed Feature Spec，作为后续用户扩展、租户隔离和审计接入的业务基线。

## 遗留问题

- 用户目录仍需继续接入部门、岗位、租户隔离、操作日志和更完整的启停规则。
