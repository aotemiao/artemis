# Spec Driven Harness Convergence

Status: completed
Last Reviewed: 2026-06-01
Review Cadence: 90 days

## 背景

Artemis 已具备仓库级 Harness Engineering 骨架，但与完整 SDD + Harness 形态相比，仍缺少业务需求级 Spec、Spec 到验证的明确映射、agent 交付 handoff 证据、真实交付演练报告守门和可复用资产库。

本计划用于把这些缺口收敛为仓库内可读、可执行、可验证的资产。

## 关联需求与规范

- Feature Spec：无。本计划改变的是仓库交付规则和 Harness 资产，不是单个业务需求。
- OpenSpec：`openspec/specs/spec-driven-delivery/spec.md`
- OpenSpec change：`openspec/changes/spec-driven-harness-convergence/`
- Runbook / API / 其它资产：
  - `docs/feature-specs/README.md`
  - `docs/patterns/spec-to-validation-mapping.md`
  - `docs/patterns/agent-delivery-handoff.md`
  - `artemis-symphony/skills/spec-driven-delivery.md`
  - `artemis-symphony/prompts/spec-driven-delivery.md`

## 目标

- 建立业务需求级 Feature Spec 层。
- 将租户创建与初始化这类真实业务能力沉淀为 completed Feature Spec。
- 建立验收标准到验证入口的结构化映射。
- 强化执行计划模板，使其包含任务输入输出、验收映射和回滚策略。
- 将 Feature Spec 与部署演练报告检查接入治理脚本。
- 为 Symphony 补 Spec 驱动交付的 skill / prompt。
- 将 Spec 驱动交付接入 Symphony 运行时，使配置、首轮 prompt 和状态接口可观察。
- 形成至少一份真实部署演练报告。

## 非目标

- 不补齐所有历史业务需求的 Feature Spec。
- 不引入外部 SDD 平台。
- 不自动生成业务测试代码。
- 不改变微服务代码行为。

## 范围

- 新增 `docs/feature-specs/` 与模板 / 示例。
- 新增 `docs/patterns/` 可复用资产。
- 更新 `docs/exec-plans/` 模板。
- 更新 `README.md`、`AGENTS.md`、`docs/README.md`、`docs/agent-workflow/`、治理文档和报告。
- 新增 `scripts/harness/check-feature-specs.sh`。
- 新增 `scripts/harness/check-spec-driven-delivery-chain.sh`。
- 新增 `scripts/harness/check-deploy-drill-reports.sh`。
- 更新部署 / 回滚演练脚本的报告格式。
- 更新 Symphony prompt / skill / workflow 资产。
- 新增 `openspec/specs/spec-driven-delivery/`。

## 风险

- 历史执行计划和历史需求没有立即补齐 Feature Spec，因此本轮只约束新增和后续复杂需求。
- Feature Spec 守门目前只做结构与验收编号映射检查，不判断业务语义正确性。
- 部署演练本轮只对 `symphony --skip-smoke` 跑通配置检查与打包链路，未覆盖真实服务启动后的 smoke。

## 任务拆解

| 编号 | 任务 | 输入 | 输出 | 验收标准 |
|------|------|------|------|----------|
| T-001 | 建立 Feature Spec 层 | Harness 缺口分析 | `docs/feature-specs/` | 有 README、模板、示例和 active/completed 目录 |
| T-001A | 迁移真实业务能力 Spec | 系统需求与租户 checklist | 租户创建与初始化 Feature Spec | completed Feature Spec 通过验收映射检查 |
| T-002 | 建立可复用模式资产 | SDD 到验证链路 | `docs/patterns/` | 有验收映射和 handoff 模式 |
| T-003 | 强化执行计划模板 | 现有 exec-plan 模板 | 新模板字段 | 包含关联需求、任务拆解、验收映射、回滚策略 |
| T-004 | 接入治理守门 | harness 脚本 | 新增 Feature Spec 和部署演练报告检查 | governance 可执行并失败可定位 |
| T-005 | 更新 Symphony 编排资产 | 现有 prompt / skill | spec-driven delivery prompt / skill | WORKFLOW 和资产检查可发现它们 |
| T-006 | 同步 OpenSpec | 默认交付规则变化 | `spec-driven-delivery` spec | 规则定义清楚 Feature Spec、验证映射、handoff |
| T-007 | 执行真实演练 | deploy drill runbook | 部署演练报告 | 报告通过结构检查 |
| T-008 | 接入 Symphony 运行时 | `WORKFLOW.md.example` 与 ServiceConfig | 首轮 prompt 注入与 `/api/v1/state` delivery 快照 | 模块测试和链路检查覆盖运行时配置 |

## 验收映射

| 验收编号 | 来源 | 验证入口 | 通过标准 |
|----------|------|----------|----------|
| AC-001 | 本计划 | `scripts/harness/check-feature-specs.sh` | Feature Spec 模板、示例、验收编号映射检查通过 |
| AC-002 | 本计划 | `scripts/harness/check-deploy-drill-reports.sh` | 演练报告包含范围、命令、验证结果和结论 |
| AC-003 | 本计划 | `scripts/harness/check-symphony-assets.sh` | Spec 驱动 prompt / skill 被纳入 Symphony 资产检查 |
| AC-004 | 本计划 | `scripts/harness/check-spec-driven-delivery-chain.sh` | Feature Spec、执行计划、Symphony、OpenSpec 和治理入口已串联 |
| AC-005 | 本计划 | `scripts/harness/verify-changed.sh working-tree` | OpenSpec、文档、治理和关键结构检查全部通过 |
| AC-006 | 本计划 | `scripts/dev/deploy-drill.sh symphony --skip-smoke` | Symphony 配置检查和打包链路通过，并生成演练报告 |
| AC-007 | 本计划 | `mvn -B -pl artemis-symphony/artemis-symphony-config,artemis-symphony/artemis-symphony-orchestrator,artemis-symphony/artemis-symphony-start -am test` | Spec-driven delivery 配置、prompt 注入与状态接口测试通过 |

## 验证计划

- `scripts/harness/check-feature-specs.sh`
- `scripts/harness/check-spec-driven-delivery-chain.sh`
- `scripts/harness/check-deploy-drill-reports.sh`
- `scripts/harness/run-governance-checks.sh`
- `scripts/harness/verify-changed.sh working-tree`
- `scripts/dev/deploy-drill.sh symphony --skip-smoke`
- `mvn -B -pl artemis-symphony/artemis-symphony-config,artemis-symphony/artemis-symphony-orchestrator,artemis-symphony/artemis-symphony-start -am test`

## 回滚策略

- 如果 Feature Spec 或部署演练报告检查误伤，可先从 `scripts/harness/run-governance-checks.sh` 移除对应入口，再保留脚本单独调试。
- 如果新的文档分层造成导航混乱，回滚 `docs/README.md`、`README.md`、`AGENTS.md` 中对应段落，并保留 OpenSpec change 记录问题。
- 如果部署 / 回滚脚本报告格式影响既有使用方，可恢复旧输出，同时保留 `docs/reports/deploy-drills/2026-06-01-sample-report-template.md` 作为目标格式。

## 决策记录

- `2026-06-01`：Feature Spec 与 OpenSpec 分离。Feature Spec 描述单个业务需求，OpenSpec 描述长期规则。
- `2026-06-01`：Feature Spec 守门先检查结构、元数据、AC 编号覆盖和验证入口非空，不做自然语言语义判断。
- `2026-06-01`：部署演练报告进入治理检查，避免真实交付演练重新变成聊天或临时终端输出。
- `2026-06-01`：Spec 驱动交付进入 Symphony 运行时，但仍只作为执行现场上下文注入；OpenSpec 和 Feature Spec 继续作为规范事实来源。

## 遗留问题

- 已完成系统主链路 Feature Spec 回填：用户目录、角色绑定、内部授权快照、网关 RBAC、菜单权限 MVP 与租户创建初始化均已归档到 `docs/feature-specs/completed/`。
- 后续新增业务能力仍需按 Feature Spec 标准补齐验收映射，尤其是部门、岗位、配置、审计、租户同步、数据权限、前端动态路由和租户套餐菜单同步。
- 部署演练还需要覆盖启动后的 smoke 和回滚演练。
