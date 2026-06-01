# 菜单权限 MVP Feature Spec

Status: completed
Last Reviewed: 2026-06-02
Review Cadence: 90 days

## 背景

用户、角色、授权快照和网关最小 RBAC 已形成主链路后，Artemis 需要把授权从角色级推进到权限点级。Phase 9 已交付菜单目录、角色菜单授权和权限码快照 MVP，为后续按钮权限、租户套餐和前端动态路由提供基础。

本 Feature Spec 从 `docs/exec-plans/completed/2026-04-28-phase9-menu-permission-mvp.md` 归档而来。

## 目标

- 建立菜单目录与角色菜单授权的最小闭环。
- 将授权快照从 `roleKeys` 扩展到 `permissionCodes`。
- 让认证服务和网关能够读取会话中的权限码。
- 为租户套餐菜单授权、按钮权限和前端路由生成保留稳定基础。

## 非目标

- 不实现完整前端动态路由生成。
- 不实现租户套餐菜单同步。
- 不实现部门、岗位、数据权限或角色部门授权。
- 不定义完整按钮级业务拦截矩阵。

## 用户故事

| 编号 | 作为 | 我希望 | 以便 |
|------|------|--------|------|
| US-001 | 系统管理员 | 维护菜单与权限码目录 | 管理后台功能入口和权限点 |
| US-002 | 系统管理员 | 为角色配置可访问菜单 | 控制不同角色可用能力 |
| US-003 | 网关 / 认证服务 | 在授权快照和会话中获取权限码 | 后续执行权限点级鉴权 |

## 业务规则

| 编号 | 规则 | 说明 |
|------|------|------|
| BR-001 | 菜单应支持目录、菜单、按钮等最小类型 | 覆盖权限点 MVP |
| BR-002 | 同父级菜单名称和路由应保持唯一 | 避免菜单树歧义 |
| BR-003 | 角色菜单授权只能引用存在的角色和菜单 | 避免悬挂授权关系 |
| BR-004 | 授权快照只返回启用角色下的有效权限码 | 避免无效授权进入运行态 |
| BR-005 | 认证服务登录 / 刷新时同步 `permissionCodes` 到会话 | 供网关和后续权限检查读取 |

## 数据与接口影响

| 类型 | 是否影响 | 说明 |
|------|----------|------|
| 数据模型 | 是 | 菜单表、角色菜单关系表、菜单领域模型、权限码快照 |
| REST API | 是 | 菜单 create / update / getById / tree，角色菜单 list / replace |
| 内部 RPC / client | 是 | 授权快照 DTO 扩展 `permissionCodes` |
| 权限 / 审计 | 是 | 菜单授权是权限管理能力，后续应接入操作日志 |
| 配置 / 部署 | 否 | 不新增运行配置 |

## 验收标准

| 编号 | 验收标准 | 验证方式 |
|------|----------|----------|
| AC-001 | 菜单目录支持最小维护与查询能力 | system 模块测试 |
| AC-002 | 角色菜单授权支持查询和替换 | system 模块测试 |
| AC-003 | 授权快照返回启用角色下的权限码 | system / client 测试 |
| AC-004 | 认证服务登录 / 刷新后会话包含 `permissionCodes` | auth 测试 |
| AC-005 | 网关 permission list 从会话读取权限码 | gateway 测试 |
| AC-006 | 菜单、角色、内部授权、认证和网关文档同步 | 文档 / 契约检查 |

## 验证映射

| 验收编号 | 验证入口 | 通过标准 |
|----------|----------|----------|
| AC-001 | `scripts/harness/check-critical-path-tests.sh` | 菜单目录 app / infra / adapter 测试基线存在并通过 |
| AC-002 | `scripts/harness/check-critical-path-tests.sh` | 角色菜单授权查询与替换测试基线存在并通过 |
| AC-003 | `scripts/harness/check-client-contracts.sh` | 授权快照 DTO 与 client contract 保持同步 |
| AC-004 | `scripts/harness/check-critical-path-tests.sh` | auth 登录 / 刷新权限码会话测试基线存在并通过 |
| AC-005 | `scripts/harness/check-critical-path-tests.sh` | gateway permission list 测试基线存在并通过 |
| AC-006 | `scripts/harness/check-api-doc-sync.sh` | `MENU_API.md`、`ROLE_API.md`、`INTERNAL_AUTH_API.md`、`AUTH_API.md`、`GATEWAY_AUTHORIZATION.md` 与实现保持同步 |

## 关联资产

- OpenSpec：`openspec/specs/ddd-cola-layering/spec.md`
- OpenSpec：`openspec/specs/repository-structure/spec.md`
- OpenSpec：`openspec/specs/lookup-tdd-testing/spec.md`
- 执行计划：`docs/exec-plans/completed/2026-04-28-phase9-menu-permission-mvp.md`
- API 文档：`artemis-modules/artemis-system/MENU_API.md`
- API 文档：`artemis-modules/artemis-system/ROLE_API.md`
- API 文档：`artemis-modules/artemis-system/INTERNAL_AUTH_API.md`
- API 文档：`artemis-auth/AUTH_API.md`
- API 文档：`artemis-gateway/GATEWAY_AUTHORIZATION.md`
- Runbook：无

## 决策记录

- `2026-06-02`：将 Phase 9 已交付能力补录为 completed Feature Spec，作为权限码、菜单授权和租户套餐菜单同步的业务基线。

## 遗留问题

- 前端动态路由生成、租户套餐菜单同步、按钮级业务拦截和数据权限仍需独立 Feature Spec。
