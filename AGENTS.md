# Artemis Agent Guide

Status: maintained
Last Reviewed: 2026-03-25
Review Cadence: 90 days

本文件是仓库内 agent 的稳定入口，目标是让任何自动化执行都先找到地图，再开始改代码。

## 先看什么

1. `README.md`
   了解仓库模块、启动方式与基础设施依赖。
2. `ARCHITECTURE.md`
   了解微服务拓扑、DDD/COLA 分层、模块边界与常见变更入口。
3. `QUALITY_SCORE.md`
   了解当前质量短板、优先修复方向与“不要继续放大的问题”。
4. `docs/harness-engineering/CHECKLIST.md`
   了解本仓库对 Harness Engineering 的落地清单与当前完成度。
5. `openspec/specs/`
   读取与本次改动直接相关的规范，避免只看局部代码做出越界修改。

## 标准工作回路

1. 先读相关规范与架构文档，再决定改动范围。
2. 优先做最小闭环改动：代码、测试、文档、脚本一并补齐。
3. 能复用 `scripts/` 下入口时，不要把命令散落在 issue 评论或聊天里。
4. 修改跨模块行为时，同步检查 OpenSpec 是否需要更新。
5. 提交前至少执行一个可解释的验证动作：
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
  先读 `openspec/specs/repository-structure/spec.md`、`docs/harness-engineering/ADD_DOMAIN_SERVICE_RUNBOOK.md`
- 契约文档、覆盖率、readiness 或治理守门：
  先读 `openspec/specs/contract-doc-guardrails/spec.md`、`openspec/specs/service-readiness-automation/spec.md`、`openspec/specs/harness-governance/spec.md`
- 项目进度、阶段汇报或演进路线：
  先读 `QUALITY_SCORE.md`、`docs/harness-engineering/ROADMAP.md`、`docs/harness-engineering/PROJECT_PROGRESS_REPORT.md`、相关 `docs/exec-plans/`
- 编码代理编排与自动化：
  先读 `artemis-symphony/README.md`、`docs/harness-engineering/ROADMAP.md`、`openspec/specs/agent-task-assets/spec.md`
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

- 启动基础设施：`scripts/dev/up.sh`
- 关闭基础设施：`scripts/dev/down.sh`
- 启动系统服务：`scripts/dev/run-system.sh`
- 启动认证服务：`scripts/dev/run-auth.sh`
- 启动网关：`scripts/dev/run-gateway.sh`
- 启动 Symphony：`scripts/dev/run-symphony.sh`
- 新增领域服务模板：`scripts/dev/new-domain-service.sh <domain>`
- 打包服务：`scripts/dev/package-service.sh <gateway|auth|system|symphony|all|<domain>>`
- 构建镜像：`scripts/dev/build-image.sh <gateway|auth|system|all|<domain>>`
- 服务状态总览：`scripts/dev/service-status.sh [all|<service>]`
- 部署演练：`scripts/dev/deploy-drill.sh <all|service> [tag_suffix] [--skip-smoke]`
- 回滚演练：`scripts/dev/rollback-drill.sh <service> <image-tag|jar-path>`
- 等待 HTTP 端点：`scripts/dev/wait-http.sh`
- 检查服务配置：`scripts/dev/check-service-config.sh <system|auth|gateway|symphony|<domain>>`
- 检查服务就绪：`scripts/dev/check-service-readiness.sh <system|auth|gateway|symphony|<domain>>`
- 健康检查：`scripts/dev/health.sh`
- 查看服务日志：`scripts/dev/tail-log.sh <gateway|auth|system|<domain>>`
- 检查 OpenSpec 同步：`scripts/harness/check-openspec-sync.sh`
- 检查 API 文档同步：`scripts/harness/check-api-doc-sync.sh`
- 检查 Dubbo client 契约：`scripts/harness/check-client-contracts.sh`
- 检查领域服务脚手架：`scripts/harness/check-domain-service-scaffold.sh`
- 检查领域服务运行资产：`scripts/harness/check-service-catalog.sh`
- 检查 Symphony 任务资产：`scripts/harness/check-symphony-assets.sh`
- 检查关键路径测试基线：`scripts/harness/check-critical-path-tests.sh`
- 扫描重复模式：`scripts/harness/check-duplicate-patterns.sh`
- 执行治理检查：`scripts/harness/run-governance-checks.sh`
- 增量验证：`scripts/harness/verify-changed.sh`
- 全量验证：`scripts/harness/full-verify.sh`
- 系统服务 smoke：`scripts/smoke/system-lookup.sh`
- 认证服务 smoke：`scripts/smoke/auth-refresh.sh`
- 网关路由 smoke：`scripts/smoke/gateway-auth-refresh.sh`
- 网关 RBAC smoke：`scripts/smoke/gateway-system-admin.sh`
- Symphony 状态 smoke：`scripts/smoke/symphony-state.sh`
- 聚合 smoke：`scripts/smoke/all-services.sh`
- 服务 smoke runbook：`docs/harness-engineering/SERVICE_SMOKE_RUNBOOK.md`
- 新增领域服务 runbook：`docs/harness-engineering/ADD_DOMAIN_SERVICE_RUNBOOK.md`
- 新增 Dubbo client runbook：`docs/harness-engineering/ADD_DUBBO_CLIENT_RUNBOOK.md`
- ArchUnit 约束 runbook：`docs/harness-engineering/ADD_ARCHUNIT_RULE_RUNBOOK.md`
- Agent review loop：`docs/harness-engineering/AGENT_REVIEW_LOOP.md`
- 质量问题标准：`docs/harness-engineering/QUALITY_ISSUE_STANDARD.md`
- 部署 / 回滚 runbook：`docs/harness-engineering/DEPLOY_AND_ROLLBACK_RUNBOOK.md`
- Symphony 故障 runbook：`docs/harness-engineering/SYMPHONY_TROUBLESHOOTING.md`

## 产出要求

- 复杂工作优先落到 `docs/exec-plans/active/`，完成后迁移到 `docs/exec-plans/completed/`
- 新增工程能力时，至少补一个“入口文档 + 执行脚本 + 验证方式”
- 改动完成后，说明做了什么、如何验证、还剩什么风险
