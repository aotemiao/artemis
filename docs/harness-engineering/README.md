# Harness Engineering

Status: maintained
Last Reviewed: 2026-03-25
Review Cadence: 90 days

这个目录用于沉淀 Artemis 在 Harness Engineering 方向上的仓库内结构、路线图和执行标准。

## 文件说明

- `CHECKLIST.md`
  直接落地的建议清单，标出已落仓、待补强、尚未开始的项目
- `ROADMAP.md`
  分阶段推进路线，说明当前所在阶段和后续目标
- `PROJECT_PROGRESS_REPORT.md`
  当前项目完成程度、阶段里程碑与后续演进路线
- `SERVICE_SMOKE_RUNBOOK.md`
  服务启动顺序、等待方式与 smoke 命令的标准入口
- `ADD_DOMAIN_SERVICE_RUNBOOK.md`
  新增领域服务的标准操作入口
- `ADD_DUBBO_CLIENT_RUNBOOK.md`
  新增 Dubbo client 的标准操作入口
- `ADD_ARCHUNIT_RULE_RUNBOOK.md`
  将工程约束落成 ArchUnit 测试的操作入口
- `AGENT_REVIEW_LOOP.md`
  agent 自评与 reviewer 复核回路
- `DOC_FRESHNESS_POLICY.md`
  核心文档审阅 cadence、过期标准与 CI 防漂移守门
- `DEPLOY_AND_ROLLBACK_RUNBOOK.md`
  服务打包、镜像构建、部署与回滚的标准入口
- `deploy-drills/`
  部署 / 回滚演练报告目录
- `SYMPHONY_TROUBLESHOOTING.md`
  Symphony 常见问题与排查入口
- `QUALITY_ISSUE_STANDARD.md`
  质量问题的建档、关闭与归档标准
- `quality-issues/`
  质量问题 active / archive 目录

## 如何使用

1. 新增工程能力前，先对照 `CHECKLIST.md`
2. 需要判断当前项目阶段、完成程度或后续演进路线时，先读 `PROJECT_PROGRESS_REPORT.md`
3. 涉及多步推进的工作，在 `docs/exec-plans/active/` 建计划
4. 需要套用标准动作时，优先从 runbook、`scripts/dev/new-domain-service.sh`、skills 和 prompts 中找现成入口
5. 完成一轮补强后，回写 `QUALITY_SCORE.md` 与相关 checklist 状态

## 与 OpenSpec 的分工

- Harness Engineering 负责仓库内的执行入口、验证回路、runbook、脚本和交付编排。
- OpenSpec 负责稳定约束的规范事实，例如模块边界、契约要求、质量门和默认 workflow 规则。
- `docs/exec-plans/` 用于承载复杂任务的实施计划，不替代 OpenSpec 规范变更。
- 如果任务只是按既有规则施工，通常建执行计划即可；如果任务会改变规则本身，就应同时更新 OpenSpec artifact。
- 如果任务既改规则又要分阶段落地，就同时使用两者：OpenSpec 管规则，执行计划管实施。
