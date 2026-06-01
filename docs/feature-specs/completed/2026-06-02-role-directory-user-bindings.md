# 角色目录与用户角色绑定 Feature Spec

Status: completed
Last Reviewed: 2026-06-02
Review Cadence: 90 days

## 背景

用户目录可用后，Artemis 需要最小角色目录和用户角色绑定能力，才能从“账号可登录”推进到“用户具备可管理的职责集合”。Phase 6 已在 `artemis-system` 中交付角色目录与用户角色关系。

本 Feature Spec 从 `docs/exec-plans/completed/2026-03-24-phase6-role-directory-checklist.md` 归档而来。

## 目标

- 建立系统角色的最小目录能力。
- 支持查询和替换用户角色绑定。
- 为内部授权快照、网关 RBAC 和权限点演进提供基础数据。

## 非目标

- 不实现菜单权限、按钮权限和数据权限。
- 不实现租户套餐授权。
- 不定义前端角色管理页面。

## 用户故事

| 编号 | 作为 | 我希望 | 以便 |
|------|------|--------|------|
| US-001 | 系统管理员 | 创建和维护系统角色 | 将用户职责抽象为可复用角色 |
| US-002 | 系统管理员 | 为用户批量替换角色 | 快速调整用户权限范围 |
| US-003 | 授权查询方 | 基于用户角色关系计算授权快照 | 支撑后续登录态和网关鉴权 |

## 业务规则

| 编号 | 规则 | 说明 |
|------|------|------|
| BR-001 | 角色 key 应保持唯一 | 作为授权快照和网关 RBAC 的稳定标识 |
| BR-002 | 用户角色绑定只能引用存在的用户和角色 | 避免悬挂授权关系 |
| BR-003 | 替换用户角色绑定应以目标集合为准 | 保证批量授权操作可重复执行 |
| BR-004 | 角色目录至少支持创建、更新、按 ID 查询和分页查询 | 满足后台管理 MVP |

## 数据与接口影响

| 类型 | 是否影响 | 说明 |
|------|----------|------|
| 数据模型 | 是 | 角色表、用户角色关系表、角色领域模型、绑定网关 |
| REST API | 是 | 角色 create / update / getById / page，用户角色 list / replace |
| 内部 RPC / client | 是 | 后续授权快照依赖角色 key 与用户角色关系 |
| 权限 / 审计 | 是 | 角色和绑定修改属于权限管理能力，后续应接入操作日志 |
| 配置 / 部署 | 否 | 不新增运行配置 |

## 验收标准

| 编号 | 验收标准 | 验证方式 |
|------|----------|----------|
| AC-001 | 角色目录支持新增、修改、按 ID 查询和分页查询 | system 模块测试 |
| AC-002 | 用户角色绑定支持按用户查询和批量替换 | system 模块测试 |
| AC-003 | 角色 key 可作为后续授权快照稳定输入 | 关键路径测试 |
| AC-004 | 角色 API 文档登记当前 REST 路由 | API 文档同步检查 |
| AC-005 | 角色目录与绑定实现符合 DDD/COLA 分层约束 | harness / Maven verify |

## 验证映射

| 验收编号 | 验证入口 | 通过标准 |
|----------|----------|----------|
| AC-001 | `scripts/harness/check-critical-path-tests.sh` | 角色目录 app / infra / adapter 关键路径测试基线存在并通过 |
| AC-002 | `scripts/harness/check-critical-path-tests.sh` | 用户角色绑定查询与替换测试基线存在并通过 |
| AC-003 | `scripts/harness/check-critical-path-tests.sh` | 授权快照相关角色 key 基线存在并通过 |
| AC-004 | `scripts/harness/check-api-doc-sync.sh` | `artemis-modules/artemis-system/ROLE_API.md` 与 Controller 路由保持同步 |
| AC-005 | `scripts/harness/verify-changed.sh staged` | 分层、契约、文档和测试守门通过 |

## 关联资产

- OpenSpec：`openspec/specs/ddd-cola-layering/spec.md`
- OpenSpec：`openspec/specs/lookup-tdd-testing/spec.md`
- 执行计划：`docs/exec-plans/completed/2026-03-24-phase6-role-directory-checklist.md`
- API 文档：`artemis-modules/artemis-system/ROLE_API.md`
- Runbook：无

## 决策记录

- `2026-06-02`：将 Phase 6 已交付能力补录为 completed Feature Spec，作为菜单权限、数据权限和租户套餐授权的前置业务基线。

## 遗留问题

- 角色数据范围、角色菜单授权、租户套餐同步和完整权限点规则需要在后续 Feature Spec 中独立描述。
