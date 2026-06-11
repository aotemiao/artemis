# Deploy Drills

Status: maintained
Last Reviewed: 2026-06-10
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
- `## 指标摘要`
- `## 结论`

`## 指标摘要` 必须包含一个低敏 JSON 块，供 Harness metrics 聚合部署 / 回滚演练趋势。推荐结构：

```json
{
  "schema_version": 1,
  "summary_type": "deploy_drill_report",
  "kind": "deploy",
  "service": "system",
  "services": ["system"],
  "status": "completed",
  "smoke": "passed",
  "rollback": false,
  "failure_stage": ""
}
```

`kind` 使用 `deploy` 或 `rollback`；`smoke` 使用 `passed`、`failed`、`skipped`、`pending` 或 `partial`。摘要只保存服务、状态、smoke 结果和失败阶段，不保存日志全文、镜像仓库凭证、外部系统响应或机器私密上下文。

模板见 `2026-06-01-sample-report-template.md`。脚本生成的报告会自动使用该结构；手写报告也应通过 `scripts/harness/check-deploy-drill-reports.sh`。
