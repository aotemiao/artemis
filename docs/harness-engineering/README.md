# Harness Engineering

Status: maintained
Last Reviewed: 2026-03-23
Review Cadence: 90 days

这个目录用于沉淀 Artemis 在 Harness Engineering 方向上的仓库内结构、路线图和执行标准。

## 文件说明

- `CHECKLIST.md`
  直接落地的建议清单，标出已落仓、待补强、尚未开始的项目
- `ROADMAP.md`
  分阶段推进路线，说明当前所在阶段和后续目标
- `SERVICE_SMOKE_RUNBOOK.md`
  服务启动顺序、等待方式与 smoke 命令的标准入口
- `DOC_FRESHNESS_POLICY.md`
  核心文档审阅 cadence、过期标准与 CI 防漂移守门
- `DEPLOY_AND_ROLLBACK_RUNBOOK.md`
  服务打包、镜像构建、部署与回滚的标准入口
- `SYMPHONY_TROUBLESHOOTING.md`
  Symphony 常见问题与排查入口

## 如何使用

1. 新增工程能力前，先对照 `CHECKLIST.md`
2. 涉及多步推进的工作，在 `docs/exec-plans/active/` 建计划
3. 完成一轮补强后，回写 `QUALITY_SCORE.md` 与相关 checklist 状态
