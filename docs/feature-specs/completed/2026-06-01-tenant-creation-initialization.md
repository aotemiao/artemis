# 租户创建与初始化 Feature Spec

Status: completed
Last Reviewed: 2026-06-01
Review Cadence: 90 days

## 背景

Artemis 面向多租户后台管理场景。租户创建不仅是单表 CRUD，还需要初始化租户管理员角色、根部门、首个用户和角色绑定，并与租户套餐授权联动。

本 Feature Spec 从 `docs/system-requirements-ddd.md` 的 `TENANT-002` 与 `docs/exec-plans/active/2026-04-28-system-requirements-checklist.md` 中收敛而来，用于把已交付的租户基础能力纳入业务需求级 Spec 体系。

## 目标

- 明确租户创建与初始化的用户故事、业务规则和验收标准。
- 将已交付能力映射到 API 文档、测试和 harness 验证入口。
- 为后续租户同步、套餐变更和租户数据隔离增强提供需求基线。

## 非目标

- 不补齐租户套餐同步、字典同步、参数同步和工作流定义同步。
- 不定义前端页面交互。
- 不改变现有 DDD/COLA 分层和内部 client 契约规则。

## 用户故事

| 编号 | 作为 | 我希望 | 以便 |
|------|------|--------|------|
| US-001 | 超级管理员 | 创建一个新租户并选择租户套餐 | 为新客户开通后台管理空间 |
| US-002 | 超级管理员 | 创建租户时自动初始化管理员、根部门和角色授权 | 避免人工补齐基础组织与权限数据 |
| US-003 | 租户管理员 | 使用初始化账号登录自己的租户 | 开始维护本租户的组织、用户和业务数据 |

## 业务规则

| 编号 | 规则 | 说明 |
|------|------|------|
| BR-001 | 租户企业名称全局唯一 | 避免重复租户主体 |
| BR-002 | 创建租户时自动生成 6 位租户编号 | 租户编号由系统生成，不能由普通修改接口变更 |
| BR-003 | 创建租户时必须引用存在且可用的租户套餐 | 套餐决定初始化角色的菜单授权范围 |
| BR-004 | 创建租户时初始化租户管理员角色 | 管理员角色应写入套餐菜单授权 |
| BR-005 | 创建租户时初始化根部门和首个系统用户 | 首个用户应归属新租户并设为根部门负责人 |
| BR-006 | 创建租户时建立首个系统用户与租户管理员角色关系 | 初始化用户登录后具备套餐授权范围内的管理权限 |
| BR-007 | 默认管理租户 `000000` 不允许被普通写操作修改、停用或删除 | 保持平台默认管理租户稳定 |

## 数据与接口影响

| 类型 | 是否影响 | 说明 |
|------|----------|------|
| 数据模型 | 是 | 租户、租户套餐、部门、用户、角色、用户角色关系、角色菜单关系 |
| REST API | 是 | 租户创建、修改、启停、删除、查询接口 |
| 内部 RPC / client | 是 | 登录校验和授权快照依赖租户编号与用户角色数据 |
| 权限 / 审计 | 是 | 租户创建和修改属于超级管理员能力，应接入操作日志 |
| 配置 / 部署 | 否 | 不新增环境配置 |

## 验收标准

| 编号 | 验收标准 | 验证方式 |
|------|----------|----------|
| AC-001 | 创建租户时生成唯一 6 位租户编号，并持久化租户主体信息 | App / Infra 测试 |
| AC-002 | 创建租户时基于套餐创建租户管理员角色，并写入菜单授权 | App / Infra 测试 |
| AC-003 | 创建租户时创建根部门、首个系统用户，并将首个用户设为根部门负责人 | App / Infra 测试 |
| AC-004 | 创建租户时建立首个系统用户与租户管理员角色关系 | App / Infra 测试 |
| AC-005 | 默认管理租户 `000000` 的修改、启停和删除写操作被拒绝 | App / Controller 测试 |
| AC-006 | 租户 API 文档登记当前租户管理 REST 路由 | API 文档同步检查 |
| AC-007 | 租户创建能力符合仓库 DDD/COLA 分层与契约守门 | harness 治理检查 |

## 验证映射

| 验收编号 | 验证入口 | 通过标准 |
|----------|----------|----------|
| AC-001 | `scripts/harness/check-critical-path-tests.sh` | 租户创建相关关键路径测试基线存在并通过 |
| AC-002 | `scripts/harness/check-critical-path-tests.sh` | 租户套餐授权与角色初始化测试基线存在并通过 |
| AC-003 | `scripts/harness/check-critical-path-tests.sh` | 根部门和首个用户初始化测试基线存在并通过 |
| AC-004 | `scripts/harness/check-critical-path-tests.sh` | 用户角色绑定初始化测试基线存在并通过 |
| AC-005 | `scripts/harness/check-critical-path-tests.sh` | 默认租户保护相关测试基线存在并通过 |
| AC-006 | `scripts/harness/check-api-doc-sync.sh` | `artemis-modules/artemis-system/TENANT_API.md` 与 Controller 路由保持同步 |
| AC-007 | `scripts/harness/verify-changed.sh working-tree` | 分层、契约、文档和治理检查通过 |

## 关联资产

- OpenSpec：`openspec/specs/ddd-cola-layering/spec.md`
- OpenSpec：`openspec/specs/contract-doc-guardrails/spec.md`
- 执行计划：`docs/exec-plans/active/2026-04-28-system-requirements-checklist.md`
- API 文档：`artemis-modules/artemis-system/TENANT_API.md`
- API 文档：`artemis-modules/artemis-system/TENANT_PACKAGE_API.md`
- Runbook：无

## 决策记录

- `2026-06-01`：本文件作为已交付租户创建与初始化能力的业务需求级 Spec 归档，用于验证 Feature Spec 体系能承载真实业务能力。
- `2026-06-01`：租户套餐同步、字典同步、参数同步和工作流定义同步不纳入本 Spec，后续应独立建 Feature Spec。

## 遗留问题

- 需要为租户套餐同步、租户字典同步、租户参数同步建立独立 Feature Spec。
- 需要在真实环境中补充租户创建后的登录 smoke 或端到端验收。
