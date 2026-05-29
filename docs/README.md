# Docs Index

Status: maintained
Last Reviewed: 2026-05-29
Review Cadence: 90 days

这个目录是 Artemis 的文档总入口，目标是让人和 agent 都能快速找到“现在该读哪份文档”。

## 首先阅读

- `../README.md`
  仓库总览、快速开始、Harness Engineering 总说明
- `../AGENTS.md`
  agent 的最小稳定入口
- `../ARCHITECTURE.md`
  架构地图、模块边界、运行拓扑
- `../artemis-gateway/GATEWAY_AUTHORIZATION.md`
  网关最小 RBAC、内部接口阻断与上下文头透传约定
- `../QUALITY_SCORE.md`
  当前工程质量状态与优先补强方向

## API Docs

- `api/README.md`
  对外 REST API 统一入口、联调通用约定、各模块 API 文档导航

## Governance

- `governance/CHECKLIST.md`
  当前落地清单
- `governance/DOC_FRESHNESS_POLICY.md`
  核心文档审阅 cadence 与防漂移守门
- `governance/QUALITY_ISSUE_STANDARD.md`
  质量问题归档与关闭标准
- `governance/quality-issues/`
  质量问题记录目录

## Runbooks

- `runbooks/SERVICE_SMOKE_RUNBOOK.md`
  服务启动顺序与 smoke 标准入口
- `runbooks/ADD_DOMAIN_SERVICE_RUNBOOK.md`
  新增领域服务 runbook
- `runbooks/ADD_DUBBO_CLIENT_RUNBOOK.md`
  新增 Dubbo client runbook
- `runbooks/ADD_ARCHUNIT_RULE_RUNBOOK.md`
  ArchUnit 约束 runbook
- `runbooks/DEPLOY_AND_ROLLBACK_RUNBOOK.md`
  打包、镜像、部署与回滚 runbook
- `runbooks/SYMPHONY_TROUBLESHOOTING.md`
  Symphony 常见故障与排查路径

## Agent Workflow

- `agent-workflow/AGENT_DEVELOPMENT_WORKFLOW.md`
  默认 agent 开发分流方式与需求受理入口
- `agent-workflow/AGENT_REVIEW_LOOP.md`
  agent 自评与 reviewer 回路

## Reports

- `reports/ROADMAP.md`
  阶段路线图
- `reports/PROJECT_PROGRESS_REPORT.md`
  当前项目完成程度与后续演进路线
- `reports/deploy-drills/`
  部署与回滚演练报告目录

## Execution Plans

- `exec-plans/README.md`
  执行计划目录说明
- `exec-plans/active/`
  进行中的复杂任务计划
- `exec-plans/completed/`
  已完成计划归档目录
- `exec-plans/completed/2026-03-24-artemis-evolution-with-local-references.md`
  基于本地参考源的阶段演进计划
- `exec-plans/completed/2026-03-24-phase1-checklist.md`
  Phase 1 落地 checklist 与交付结果
- `exec-plans/completed/2026-03-24-phase2-checklist.md`
  Phase 2 落地 checklist 与交付结果
- `exec-plans/completed/2026-03-24-phase3-checklist.md`
  Phase 3 落地 checklist 与交付结果
- `exec-plans/completed/2026-03-24-phase4-checklist.md`
  Phase 4 落地 checklist 与交付结果
- `exec-plans/completed/2026-03-24-phase5-user-directory-checklist.md`
  Phase 5 落地 checklist 与交付结果
- `exec-plans/completed/2026-03-24-phase6-role-directory-checklist.md`
  Phase 6 落地 checklist 与交付结果
- `exec-plans/completed/2026-03-24-phase7-internal-authorization-checklist.md`
  Phase 7 落地 checklist 与交付结果
- `exec-plans/completed/2026-03-25-phase8-gateway-rbac-checklist.md`
  Phase 8 落地 checklist 与交付结果
- `exec-plans/templates/execution-plan-template.md`
  计划模板
