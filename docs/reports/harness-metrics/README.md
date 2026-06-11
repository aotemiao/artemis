# Harness Metrics Reports

Status: maintained
Last Reviewed: 2026-06-10
Review Cadence: 30 days

本目录存放 Harness Engineering 指标报告。它用于把 Symphony eval、agent run summary、验证结果和交付信号汇总为可复盘的 scorecard，避免只靠人工维护 `QUALITY_SCORE.md`。

## 报告用途

- 统计 agent eval 数量、通过率和 token 成本。
- 统计 Symphony agent run 成功率、重试率、平均耗时、平均 turn 和 token。
- 统计 Codex 事件类型计数，用于定位 session、token usage、turn 终态和错误事件分布。
- 统计稳定失败类别、外部副作用和可观测性缺口。
- 统计低敏运行环境分布，例如 Java major、OS 名称、CPU 架构和 Spring profile。
- 统计低敏权限姿态，例如远程 worker、网络访问、danger-full-access 许可、approval policy、sandbox 类型和可写 root 数量分布。
- 统计 workspace 产物 inventory 的文件数量、字节数、截断数和扫描错误码。
- 统计失败 run 生成的 eval dataset 草稿 backlog，包括人工复核数、失败分类和风险等级分布。
- 统计部署 / 回滚演练报告中的服务、状态、smoke 和失败阶段分布。
- 提供 GitHub Actions artifact 快照入口，并为后续接入 PR / merge / revert / review finding 指标保留稳定结构。

## 生成入口

```bash
scripts/harness/generate-harness-metrics-report.sh
```

默认输入：

- `artifacts/agent-evals/`
- `artifacts/agent-runs/`
- `artifacts/harness-delivery-signals/`
- `artifacts/agent-eval-drafts/`
- `docs/reports/deploy-drills/`
- `artifacts/agent-evals/**/summary.json`
- `artifacts/agent-evals/**/agent-runs/`
- `artifacts/harness-delivery-signals/**/*.json`
- `artifacts/agent-eval-drafts/*.yml`
- `docs/reports/deploy-drills/*.md`

默认输出：

- `docs/reports/harness-metrics/generated/latest.json`
- `docs/reports/harness-metrics/generated/latest.md`

`generated/` 下的 `latest.*` 是可再生成快照，默认不要求提交。需要长期沉淀趋势时，按日期复制为 `docs/reports/harness-metrics/YYYY-MM-DD-<topic>.md`，并在报告中说明数据来源、时间窗口和未覆盖指标。

CI 入口使用：

```bash
scripts/harness/collect-github-delivery-signal.sh
scripts/harness/generate-ci-harness-metrics.sh
```

`collect-github-delivery-signal.sh` 默认读取 GitHub Actions 的 `GITHUB_EVENT_PATH`，输出 `artifacts/harness-delivery-signals/github-event.json`。它不调用 GitHub API，不需要 token，只从事件 payload 中提取 PR opened / merged、push revert、review changes requested / review comment 等低敏计数信号。

`generate-ci-harness-metrics.sh` 默认输出到 `artifacts/harness-metrics/latest.json` 和 `artifacts/harness-metrics/latest.md`，供 GitHub Actions artifact 上传；也会在存在 `GITHUB_STEP_SUMMARY` 时写入本次 job 摘要。没有 eval / run / delivery signal artifacts 时，快照仍会生成，指标为 0；仓库内已有 deploy drill 报告会默认进入快照，用于证明 CI 路径和报告结构可用。

CI 包装脚本可通过 `HARNESS_METRICS_DEPLOY_DRILLS_DIR` 覆盖 deploy drill 报告目录，默认值为 `docs/reports/deploy-drills`。

`scripts/e2e/run-symphony-agent-eval.sh` 也会在每次通过后为本次 eval 生成 `artifacts/agent-evals/<run>/harness-metrics/latest.json` 与 `latest.md`，并把路径写入 eval `summary.json`，便于直接复盘单次运行。

运行完整 memory eval suite 时：

```bash
scripts/e2e/run-symphony-agent-eval.sh all
```

脚本会把每个 dataset 的单次产物写入 `artifacts/agent-evals/<suite>/cases/`，再生成 suite 级 `summary.json` 与 `harness-metrics/latest.*`。该模式用于本地回归或发布前演练，不默认进入轻量 governance。

默认指标生成器会递归聚合 suite 下的 case summary 与 case agent run summary，并把 suite 自身作为单独的 `eval_suites` 指标统计，避免把一个 suite 误算成单个 eval case。

Agent run 的重试率只统计 `retry.scheduled=true` 且 `retry.dispatch_kind=retry` 的失败重试；成功 attempt 后用于检查 issue 是否仍处 active 状态的 `continuation` 调度不会计入重试率。

Codex 事件统计来自 `summary_type=symphony_agent_run` 的 `codex.event_counts` 字段，并聚合到快照的 `agent_runs.codex_events`。该结构只保留事件类型和次数，例如 `session_started`、`thread/tokenUsage/updated`、`turn_completed`、`turn_failed` 或 `turn_ended_with_error`。报告不读取事件 payload、prompt、stdout 原文、工具输出或完整聊天记录。

外部副作用统计同时保留历史 boolean 标记和 `external_effects.events` 聚合；后者按低敏事件的 `type`、`status`、`type:status` 与稳定 `error_code` 统计 tracker 状态认领、Linear 评论等外部写操作。报告不聚合 `error_message`，避免把外部响应或敏感上下文带入长期指标。

运行环境统计来自 `summary_type=symphony_agent_run` 的 `environment` 字段，只聚合 Java major、OS name、OS arch 和 Spring profile 分布，用于解释不同本机 / CI 运行条件下的 eval 与 run 差异。报告不聚合用户名、home 目录、PATH、完整环境变量、Maven 本地仓库路径或其他容易泄露本机细节的字段。

权限姿态统计来自 `summary_type=symphony_agent_run` 的 `permissions` 字段，并聚合到快照的 `agent_runs.permission_posture`。该结构只保留 `remote_worker`、`network_access`、`danger_full_access_allowed` 的 run 数，`approval_policy`、`thread_sandbox`、`turn_sandbox_policy.type` 的分布，以及 `writable_roots` / `allowed_writable_roots` 的数量分布。报告不把具体 writable root、workspace 绝对路径、network access reason 或本机目录作为指标维度。

Workspace artifact inventory 来自 `summary_type=symphony_agent_run` 的 `workspace.artifact_inventory` 字段，只聚合文件数量、字节数、截断 run 数和稳定扫描错误码。报告不读取文件内容、不计算内容 hash，也不把完整 workspace 路径作为指标维度。

Agent eval 草稿统计来自 `artifacts/agent-eval-drafts/*.yml`，用于观察失败 run 生成回归草稿后的待复核 backlog。报告只聚合草稿数量、`manual_review_required` 数量、`expected_failure_category` 和 `risk_level` 分布，不读取或聚合完整 issue description、prompt、stdout、工具输出或外部响应。

Delivery signal 使用低敏计数 JSON 输入，默认从 `artifacts/harness-delivery-signals/` 递归读取 `summary_type=harness_delivery_signal` 的文档。CI 会自动通过 `scripts/harness/collect-github-delivery-signal.sh` 从 GitHub Actions event payload 生成一份信号，也可以由后续 GitHub API 采集器或人工复盘脚本按同一结构补充更多低敏计数。推荐结构：

```json
{
  "schema_version": 1,
  "summary_type": "harness_delivery_signal",
  "provider": "github",
  "pull_requests": {
    "created": 1,
    "merged": 1,
    "reverted": 0,
    "merge_time_seconds": [1800]
  },
  "review_findings": {
    "total": 2,
    "severity_counts": {
      "p1": 1,
      "p2": 1
    },
    "category_counts": {
      "bug": 1,
      "security": 1
    }
  }
}
```

该结构只保存计数、分类和耗时，不保存 PR 正文、review 评论正文、外部系统响应或人员私密上下文。

Deploy drill 使用 `docs/reports/deploy-drills/*.md` 中的 `summary_type=deploy_drill_report` JSON 摘要块，聚合 deploy / rollback 演练数量、服务分布、状态分布、smoke 分布和失败阶段分布。`Status: template` 的样例模板会接受结构检查但不会进入指标统计。`scripts/dev/deploy-drill.sh` 与 `scripts/dev/rollback-drill.sh` 会自动生成该摘要块；手写报告也必须通过 `scripts/harness/check-deploy-drill-reports.sh`。摘要只保存低敏计数信号，不保存日志全文、镜像仓库凭证或外部系统响应。

## 治理检查

```bash
scripts/harness/check-harness-metrics-report.sh
```

该检查使用临时低敏 fixture 验证生成器输出 JSON / Markdown 的结构，不依赖本机已有 artifacts，也不会读取完整 prompt、聊天记录、工具输出或外部系统响应。

## 当前边界

- 已覆盖本地低敏 eval summary、Symphony agent run summary、run environment 分布、权限姿态分布、eval draft backlog 和 deploy / rollback drill 摘要。
- 已接入 GitHub Actions artifact 上传入口，并支持从 GitHub Actions event 自动采集低敏 PR / merge / revert / review finding delivery signal；尚未内置 GitHub API 深度抓取、跨平台 dashboard 和 AI credits。
- 指标只能辅助判断，不能单独替代验收标准、代码 review、安全审查或真实 smoke。
