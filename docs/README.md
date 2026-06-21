# Docs

Status: maintained
Last Reviewed: 2026-06-10
Review Cadence: 90 days

这个目录存放 Artemis 的工程说明、执行说明、治理记录和阶段性计划。本文不是简单导航，而是说明 `docs/` 下每类文档资产的职责、内容边界和放置规则，让人和 agent 都能判断“应该读哪里、应该把新文档放哪里”。

## 先读什么

- `../README.md`
  项目定位、技术栈、模块结构、启动方式和 Harness Engineering 总说明。
- `../AGENTS.md`
  agent 的稳定入口，说明默认工作回路、上下文选择和验证要求。
- `../ARCHITECTURE.md`
  服务拓扑、DDD/COLA 分层、模块边界、关键调用链。
- `../artemis-gateway/GATEWAY_AUTHORIZATION.md`
  网关最小 RBAC、内部接口阻断和上下文头透传约定。
- `../QUALITY_SCORE.md`
  当前工程质量状态、已封板能力和优先补强方向。

## 目录职责

| 目录 | 里面有什么 | 什么时候读 | 不放什么 |
|------|------------|------------|----------|
| `api/` | 对外 REST API 入口、通用联调约定、模块级 API 文档链接 | 前端联调、接口排障、补 API 文档 | 长期工程规则；这些应放 `openspec/` |
| `agent-workflow/` | agent 需求受理、默认分流、自评和 reviewer 回路 | 让 agent 或人按统一流程处理任务 | 具体任务执行计划；这些放 `exec-plans/` |
| `feature-specs/` | 业务需求级 Spec、模板、示例、验收标准和验证映射 | PRD、issue 或口头需求需要先澄清业务规则与验收标准 | 长期工程规则；这些放 `openspec/` |
| `exec-plans/` | 复杂任务的背景、范围、步骤、风险、验证和完成归档 | 跨模块、多阶段、迁移、重构、基础设施任务 | 长期规范本身；这些放 `openspec/` |
| `governance/` | Harness 概念源、checklist、文档 freshness、质量问题标准和质量问题记录 | 做治理检查、关闭质量问题、维护文档新鲜度 | 一次性任务过程；这些放 `exec-plans/` |
| `patterns/` | 可复用工程模式、验收映射、handoff 模板和安全审查清单 | 多个需求会重复使用同一模式时 | 单次任务记录 |
| `reports/` | 项目进度、路线图、部署/回滚演练报告、agent run 摘要规则 | 看当前完成度、阶段路线、演练结果、低敏运行摘要 | 操作步骤；这些放 `runbooks/` |
| `runbooks/` | 可重复执行的操作步骤和排障路径 | 启动、smoke、部署、回滚、新增服务、新增契约、排查 Symphony | 抽象规则和路线图 |
| `security/` | 威胁模型和安全边界说明 | 涉及权限、认证、敏感信息、外部副作用或高风险 review | 单次任务审查记录 |

根目录下的独立文件：

- `system-requirements-ddd.md`
  系统功能需求和 DDD 领域划分草案，偏业务建模输入。
- `requirements-acceptance-checklist.md`
  需求验收补充清单，偏业务用例验收。

## 目录内容

### `api/`

- `api/README.md`
  API 文档的统一入口，集中说明鉴权、分页、状态码、错误响应、示例补齐规则，并链接各模块 API 文档。

### `agent-workflow/`

- `AGENT_DEVELOPMENT_WORKFLOW.md`
  判断一个需求应该只施工、更新 OpenSpec、建立执行计划，还是调整 Symphony 编排资产。
- `AGENT_REVIEW_LOOP.md`
  agent 完成后如何自评，reviewer 如何复核风险、验证和遗漏。


### 根部治理资产

- `../scripts/harness/check-openspec-change-state.sh`
  OpenSpec active change 状态守门，要求进行中 change 具备 `proposal.md`、`tasks.md` 和未完成任务；已完成 change 在规则合入 `openspec/specs/` 后及时删除。

### `feature-specs/`

- `README.md`
  Feature Spec 的职责、目录约定和最低结构要求。
- `templates/feature-spec-template.md`
  业务需求 Spec 模板。
- `examples/menu-permission-feature-spec.md`
  菜单权限 MVP 的示例 Spec，用于展示验收标准和验证映射写法。
- `completed/2026-06-01-tenant-creation-initialization.md`
  租户创建与初始化能力的真实业务 Feature Spec 归档。
- `completed/2026-06-02-user-directory-authentication.md`
  用户目录与真实凭证认证能力的 Feature Spec 归档。
- `completed/2026-06-02-role-directory-user-bindings.md`
  角色目录与用户角色绑定能力的 Feature Spec 归档。
- `completed/2026-06-02-internal-authorization-snapshot.md`
  内部授权快照能力的 Feature Spec 归档。
- `completed/2026-06-02-gateway-minimal-rbac.md`
  网关最小 RBAC 能力的 Feature Spec 归档。
- `completed/2026-06-02-menu-permission-mvp.md`
  菜单权限 MVP 的真实业务 Feature Spec 归档。
- `active/`
  进行中业务需求 Spec。
- `completed/`
  已交付业务需求 Spec 归档。

### `exec-plans/`

- `README.md`
  执行计划的使用规则，说明它和 OpenSpec 的边界。
- `active/`
  进行中的复杂任务计划，例如系统需求补齐、代码生成器、工作流运行时等。
- `completed/`
  已完成计划归档，例如 OpenSpec 对齐、阶段 checklist、Symphony 对齐、网关 RBAC、移除聚合层等。
- `templates/execution-plan-template.md`
  新计划模板。

`exec-plans/` 回答“这次复杂工作怎么交付”。如果任务改变长期规则，同时更新 `openspec/changes/` 或 `openspec/specs/`。

### `patterns/`

- `README.md`
  可复用工程模式目录说明。
- `spec-to-validation-mapping.md`
  如何把 Feature Spec 的验收标准映射到测试、smoke、harness 脚本或人工验收。
- `agent-delivery-handoff.md`
  agent 交付时如何说明需求来源、验证证据、验收映射和剩余风险。
- `security-review-checklist.md`
  高风险改动的权限、幂等、并发、事务、SQL、日志和可观测性审查清单。

### `governance/`

- `HARNESS.md`
  Harness Engineering 的唯一概念源，记录当前有效能力与已下线能力。
- `CHECKLIST.md`
  Harness Engineering 落地清单。
- `QUALITY_ISSUE_STANDARD.md`
  质量问题建档、关闭和归档标准。
- `quality-issues/`
  质量问题记录目录。活动问题放 `active/`，已关闭或已接受的问题放 `archive/`；当前没有活动问题时不要求保留空目录。

### `security/`

- `THREAT_MODEL.md`
  默认威胁模型，覆盖保护目标、信任边界、主要威胁和高风险 review 要求。

### `reports/`

- `ROADMAP.md`
  Harness Engineering 阶段路线图。
- `PROJECT_PROGRESS_REPORT.md`
  项目完成度、阶段状态、能力维度和后续演进路线。
- `deploy-drills/`
  部署与回滚演练报告目录。当前保留 `README.md` 是为了让空报告目录也有可追踪入口；后续演练报告直接按日期写入该目录。
- `agent-runs/README.md`
  agent 运行轨迹摘要的低敏留存规则和模板。

### `runbooks/`

- `SERVICE_SMOKE_RUNBOOK.md`
  服务启动顺序、readiness 检查和 smoke 组合。
- `ADD_DOMAIN_SERVICE_RUNBOOK.md`
  新增领域服务的标准模块拆分、脚本入口和验证要求。
- `ADD_DUBBO_CLIENT_RUNBOOK.md`
  新增内部 Dubbo client 契约的放置位置、步骤和验证要求。
- `ADD_ARCHUNIT_RULE_RUNBOOK.md`
  新增 ArchUnit 约束的切入点、写法和验证要求。
- `DEPLOY_AND_ROLLBACK_RUNBOOK.md`
  打包、镜像、部署演练和回滚演练步骤。
- `SYMPHONY_TROUBLESHOOTING.md`
  Symphony 状态页、workflow 重载、Linear 调度和基础 smoke 排障路径。
- `AGENT_PERMISSION_RUNBOOK.md`
  agent sandbox、approval、外部副作用和敏感信息处理策略。
- `RISK_BASED_VERIFICATION_RUNBOOK.md`
  按变更类型选择最小验证集的规则。

## README 规则

`docs/README.md` 是人类阅读的总入口，负责概括所有文档目录。不要为了“每个目录都有 README”而新增占位文件。

子目录 README 只在以下场景保留：

- 它本身是专题入口，例如 `api/README.md`。
- 它定义该目录的工作流或格式约定，例如 `exec-plans/README.md`、`governance/quality-issues/README.md`。
- 目录当前没有实际记录，但仓库需要保留这个未来报告入口，例如 `reports/deploy-drills/README.md`。

以下目录不需要单独 README：

- 文件名已经表达职责的集合目录，例如 `runbooks/`、`agent-workflow/`、`governance/`、`reports/`。
- 状态分区目录，例如 `exec-plans/active/`、`exec-plans/completed/`、`quality-issues/active/`、`quality-issues/archive/`。这些分区规则应写在父级 README 或本文中。

## 放置规则

- 长期规则、模块边界、契约约束、质量门：放 `../openspec/specs/`，变更中规则放 `../openspec/changes/`。
- 业务需求、用户故事、验收标准、验证映射：放 `feature-specs/`。
- 一次复杂任务的实施计划：放 `exec-plans/active/`，完成后迁到 `exec-plans/completed/`。
- 可复用模式、模板和示例：放 `patterns/`。
- 可重复操作步骤：放 `runbooks/`，并优先链接 `../scripts/` 下的入口。
- 项目阶段状态和对外汇报：放 `reports/`。
- 安全威胁模型：放 `security/`。
- 质量问题记录：放 `governance/quality-issues/`。
- REST API 说明：入口放 `api/README.md`，模块级文档继续随服务模块放置。
