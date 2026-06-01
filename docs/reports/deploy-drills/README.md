# Deploy Drills

Status: maintained
Last Reviewed: 2026-06-01
Review Cadence: 90 days

本目录存放部署与回滚演练报告，用于把“演练过什么、结论如何、下一次复用什么命令”沉淀到仓库里。

推荐入口：

- `scripts/dev/deploy-drill.sh`
- `scripts/dev/rollback-drill.sh`
- `scripts/dev/service-status.sh`
- `scripts/harness/check-deploy-drill-reports.sh`

## 报告要求

每份部署或回滚演练报告至少包含：

- `## 演练范围`
- `## 执行命令`
- `## 验证结果`
- `## 结论`

模板见 `2026-06-01-sample-report-template.md`。脚本生成的报告会自动使用该结构；手写报告也应通过 `scripts/harness/check-deploy-drill-reports.sh`。
