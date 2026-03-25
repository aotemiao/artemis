# Doc Freshness Policy

Status: maintained
Last Reviewed: 2026-03-25
Review Cadence: 90 days

本文件用于把“文档不要漂移”这件事变成仓库内的明确规则，而不是依赖维护者记忆。

## 适用范围

当前纳入 freshness 守门的核心文档包括：

- `README.md`
- `AGENTS.md`
- `ARCHITECTURE.md`
- `QUALITY_SCORE.md`
- `docs/README.md`
- `docs/exec-plans/README.md`
- `docs/harness-engineering/README.md`
- `docs/harness-engineering/CHECKLIST.md`
- `docs/harness-engineering/ROADMAP.md`
- `docs/harness-engineering/PROJECT_PROGRESS_REPORT.md`
- `docs/harness-engineering/SERVICE_SMOKE_RUNBOOK.md`
- `docs/harness-engineering/DEPLOY_AND_ROLLBACK_RUNBOOK.md`
- `docs/harness-engineering/SYMPHONY_TROUBLESHOOTING.md`

## 必须满足的规则

1. 每份核心文档都要在文件头部包含：
   `Status:`
   `Last Reviewed: YYYY-MM-DD`
   `Review Cadence: <days> days`
2. `Last Reviewed` 不能早于当前日期，也不能超过声明的 `Review Cadence`
3. `Review Cadence` 当前上限固定为 `90 days`
4. 相关脚本、CI、runbook 或入口文档发生结构变化时，同步回写对应文档的审阅日期

## 守门入口

- 本地：`scripts/harness/check-doc-freshness.sh`
- 全量验证：`scripts/harness/full-verify.sh`
- CI：`.github/workflows/verify.yml`

## 周期性巡检

GitHub Actions `verify` 工作流已增加每周巡检触发，用于重复执行文档 freshness、链接、一致性、Maven 验证与镜像构建检查。
