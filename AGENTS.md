# Artemis Agent Guide

Status: maintained
Last Reviewed: 2026-06-10
Review Cadence: 90 days

本文件是仓库内 agent 的稳定入口，目标是让任何自动化执行都先找到地图，再开始改代码。

## 先看什么

1. `README.md`
   了解仓库模块、启动方式与基础设施依赖。
2. `ARCHITECTURE.md`
   了解微服务拓扑、DDD/COLA 分层、模块边界与常见变更入口。
3. `QUALITY_SCORE.md`
   了解当前质量短板、优先修复方向与“不要继续放大的问题”。
4. `docs/governance/CHECKLIST.md`
   了解本仓库对 Harness Engineering 的落地清单与当前完成度。
5. `docs/agent-workflow/AGENT_DEVELOPMENT_WORKFLOW.md`
   了解默认的 agent 开发分流方式。
6. `docs/feature-specs/README.md`
   了解业务需求级 Spec、验收标准和验证映射。
7. `openspec/specs/`
   读取与本次改动直接相关的规范，避免只看局部代码做出越界修改。

## 标准工作回路

1. 先读相关规范与架构文档，再决定改动范围。
2. 若需求描述还不够结构化，先用 `artemis-symphony/prompts/agent-requirement-intake.md` 整理成最小需求模板。
3. 若需求涉及业务规则、数据模型、API、内部契约或跨模块协作，先建立或更新 `docs/feature-specs/`，写清验收标准和验证映射。
4. 优先做最小闭环改动：代码、测试、文档、脚本一并补齐。
5. 能复用 `scripts/` 下入口时，不要把命令散落在 issue 评论或聊天里。
6. 修改跨模块行为时，同步检查 OpenSpec 是否需要更新。
7. 若需求涉及权限、幂等、锁、事务、异常处理、SQL 性能、日志或可观测性，使用 `docs/patterns/security-review-checklist.md` 和 `docs/runbooks/AGENT_PERMISSION_RUNBOOK.md` 做风险审查。
8. 提交前至少执行一个可解释的验证动作：
   `scripts/harness/verify-changed.sh`
   或
   `scripts/harness/full-verify.sh`

## 如何选择上下文

如果你修改的是：

- `artemis-system` 业务能力：
  先读 `ARCHITECTURE.md`、`openspec/specs/ddd-cola-layering/spec.md`、`openspec/specs/lookup-tdd-testing/spec.md`
- 工程规则、测试约束、容器化、环境配置：
  先读 `openspec/specs/engineering-constraints/spec.md`
- 内部客户端聚合、BOM 或新增领域服务模板：
  先读 `openspec/specs/repository-structure/spec.md`、`docs/runbooks/ADD_DOMAIN_SERVICE_RUNBOOK.md`
- 契约文档、覆盖率、readiness 或治理守门：
  先读 `openspec/specs/contract-doc-guardrails/spec.md`、`openspec/specs/service-readiness-automation/spec.md`、`openspec/specs/harness-governance/spec.md`
- 项目进度、阶段汇报或演进路线：
  先读 `QUALITY_SCORE.md`、`docs/reports/ROADMAP.md`、`docs/reports/PROJECT_PROGRESS_REPORT.md`、相关 `docs/exec-plans/`
- 业务需求、验收标准或 Spec 驱动交付：
  先读 `docs/feature-specs/README.md`、`docs/patterns/spec-to-validation-mapping.md`、`docs/patterns/security-review-checklist.md`、`docs/security/THREAT_MODEL.md`、`openspec/specs/spec-driven-delivery/spec.md`
- 编码代理编排与自动化：
  先读 `artemis-symphony/README.md`、`docs/agent-workflow/AGENT_DEVELOPMENT_WORKFLOW.md`、`docs/reports/ROADMAP.md`、`openspec/specs/agent-task-assets/spec.md`
- 复杂任务或多步迁移：
  先在 `docs/exec-plans/active/` 建立或更新执行计划

## 何时使用执行计划与 OpenSpec 变更

- 在现有约束内交付复杂实现时，优先使用 `docs/exec-plans/active/` 记录背景、范围、风险、步骤与验证。
- 当任务会新增、修改或废弃稳定约束时，必须同步更新相关 OpenSpec artifact；典型场景包括模块边界、契约治理、质量门和默认 workflow 规则。
- 当任务既涉及规范变化，又需要分阶段推进实现时，两条线一起使用：OpenSpec 负责描述“规则如何变化”，执行计划负责推进“实现如何落地”。
- 不按“人类开发”或“agent 开发”区分流程；人和 agent 都使用同一套判定标准。
- 如果仓库中没有进行中的 `openspec/changes/`，且本次只是按既有规则施工，可以只建执行计划，不必额外创建规范变更目录。

## 改动守则

- 新增或修改代码注释、配置注释、贡献者文档时使用中文。
- 命名保持英文，符合英语母语习惯。
- 构建与质量门默认要求使用 JDK 21；优先通过仓库脚本运行 Maven。
- 不要让 `adapter` 直接依赖 `infra`，不要让 `app` 直接依赖 `infra`。
- 不要在没有验证入口的情况下引入新的工程约束。
- 不要把关键知识只留在聊天、任务系统或个人笔记里；需要沉淀到仓库。

## 优先使用的脚本

脚本按目录自描述，优先复用，不要把命令散落在 issue 评论或聊天里。各脚本的职责与参数见脚本头部注释。

- 本地起停与运维：`scripts/dev/`（如 `up.sh` / `down.sh` / `start-all.sh` / `stop-all.sh` / `health.sh` / `run-<service>.sh` / `new-domain-service.sh` / `package-service.sh` / `build-image.sh`）
- 验证与治理守门：`scripts/harness/`（增量验证 `verify-changed.sh`、全量验证 `full-verify.sh`、聚合治理 `run-governance-checks.sh`、OpenSpec 同步 `check-openspec-sync.sh`）
- 服务冒烟：`scripts/smoke/`
- 真实链路 e2e：`scripts/e2e/run-symphony-live-e2e.sh`
- 常见任务 runbook：`docs/runbooks/`

## 产出要求

- 复杂工作优先落到 `docs/exec-plans/active/`，完成后迁移到 `docs/exec-plans/completed/`
- 业务需求优先落到 `docs/feature-specs/active/`，交付完成后迁移到 `docs/feature-specs/completed/`
- 新增工程能力时，至少补一个“入口文档 + 执行脚本 + 验证方式”
- 改动完成后，说明做了什么、如何验证、还剩什么风险
