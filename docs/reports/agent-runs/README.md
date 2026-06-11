# Agent Run Reports

Status: maintained
Last Reviewed: 2026-06-10
Review Cadence: 90 days

本目录说明 agent 运行轨迹摘要的留存规则。完整模型提示词、工具输出和敏感日志不应默认提交到仓库；仓库只沉淀可审计、可复盘、低敏的摘要。

## 摘要用途

agent run 摘要用于回答：

- agent 处理了哪个需求或 issue。
- agent 使用了哪些事实源、Feature Spec、执行计划和 OpenSpec。
- agent 实际执行了哪些验证命令。
- 哪些风险由自动化验证覆盖，哪些需要人工 review。
- 本次运行是否产生外部副作用。
- 本次 Codex 事件类型的低敏计数，例如 session、token usage、turn terminal 或错误事件出现次数。
- 本次运行所在的低敏环境快照，例如 Java、OS、架构和 Spring profile 分布。
- 本次 workspace 产物的低敏 inventory，例如 workspace 稳定引用、相对路径、文件数量和字节数。

## 推荐文件位置

- 一次性本地调试产物：`artifacts/agent-runs/`，Symphony 默认在每次 attempt 收尾时写入低敏 JSON summary，默认不提交。
- 需要沉淀的复盘摘要：`docs/reports/agent-runs/YYYY-MM-DD-<topic>.md`。

## 摘要模板

```md
# <Run Title>

Status: completed
Last Reviewed: YYYY-MM-DD
Review Cadence: 90 days

## 任务来源

- Issue / Feature Spec / 执行计划：

## 使用资产

- 事实源：
- Prompt / Skill：
- Runbook：

## 执行摘要

- 方案：
- 改动：
- 外部副作用：

## 验证证据

| 命令 | 结果 | 说明 |
|------|------|------|
| `<command>` | 通过 / 失败 | <关键断言> |

## 风险与审查

- 自动覆盖：
- 人工复核：
- 未覆盖风险：
```

## 禁止提交

- 完整 prompt、完整聊天记录或包含密钥的工具输出。
- token、密码、生产连接串、客户数据和个人敏感数据。
- 未脱敏的外部系统响应全文。

## 治理检查

```bash
scripts/harness/check-agent-run-summaries.sh
```

该检查扫描 `docs/reports/agent-runs/` 下沉淀的摘要，拦截疑似 Bearer token、JWT、云访问密钥、明文密码字段和带密码的连接串。对 `summary_type=symphony_agent_run` 的 JSON 摘要，它还会检查必需审计字段是否存在，包括 issue、attempt、workspace、codex usage、codex event_counts、permissions、environment、retry 和 external effects。`workspace.path`、`permissions.writable_roots`、`permissions.allowed_writable_roots` 和 `turn_sandbox_policy.writableRoots` 只能保存 `workspace/<key>` 或 `configured-writable-root/<n>` 这类低敏相对引用，不应写入本机绝对路径；`codex.event_counts` 只允许事件类型到非负整数次数的映射，不应包含事件 payload、prompt、工具输出或 stdout 原文；`workspace.artifact_inventory` 只允许 workspace 相对路径、文件数量、字节数、截断标志和扫描错误码，不应包含文件内容或绝对产物路径；`environment` 只允许 Java、Maven 版本占位、OS、CPU 架构、可用处理器数量和 Spring profile 等低敏字段，不应写入用户名、home 目录、PATH、完整环境变量或本地仓库路径。该检查不能替代人工脱敏审查，但可以阻止最常见的敏感内容误提交和结构漂移。

`retry.scheduled=true` 只表示失败 attempt 已排入故障重试；成功 attempt 后用于检查 issue 是否仍处 active 状态的续跑调度使用 `retry.dispatch_kind=continuation`，不计入重试率。

也可以传入一个或多个文件 / 目录，递归扫描本地 artifact 中的低敏 JSON summary：

```bash
scripts/harness/check-agent-run-summaries.sh artifacts/agent-evals/<run>/agent-runs
scripts/harness/check-agent-run-summaries.sh artifacts/agent-evals/<suite>
```

`scripts/e2e/run-symphony-agent-eval.sh` 会在单次 eval 和 suite eval 结束后自动调用该检查，避免生成的 run summary 带着疑似敏感内容进入后续指标聚合或人工沉淀流程。

失败 run 可以作为后续回归样例的线索，但不能直接自动进入正式 dataset。需要先生成草稿：

```bash
scripts/harness/generate-agent-eval-drafts.sh artifacts/agent-runs
```

草稿默认写入 `artifacts/agent-eval-drafts/`，只包含低敏字段，并带有 `manual_review_required: true`。人工复核后，才能把最小稳定复现移动到 `docs/agent-evals/datasets/` 并运行 `scripts/harness/run-agent-evals.sh` 与对应 e2e eval。
