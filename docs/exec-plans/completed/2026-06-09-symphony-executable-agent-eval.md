# Symphony Executable Agent Eval

Status: completed
Last Reviewed: 2026-06-09
Review Cadence: 90 days

## 背景

`2026-06-09-harness-engineering-market-gap-research.md` 将真实运行闭环列为 Artemis 与成熟 agentic engineering harness 的主要差距。前序工作已经补强 API 文档自动发现、OpenSpec active 状态守门和低敏 run 摘要样例，但 Symphony 仍需要一条无需外部凭证、可重复运行、会真实启动编排链路的 memory eval。

## 目标

- 新增可执行 Symphony memory agent eval，验证 memory tracker、workspace、Codex app-server 协议、SQLite run history 和 JSON run summary。
- 将 eval dataset 纳入 `docs/agent-evals/`，并接入 agentic harness 资产守门。
- 修正 eval 构建方式，确保新增运行时代码进入 Spring Boot 启动包。
- 基于 SQLite history 提供近期运行指标 API 与 `/runs` 页面摘要，覆盖状态分布、稳定失败类别、成功率、重试数、token 和平均耗时。
- 提供 Harness metrics report 生成器，把低敏 eval / run artifacts 聚合为 Markdown / JSON scorecard。
- 同步质量信号，避免 roadmap / scorecard 继续把已落地能力描述为未来事项。

## 非目标

- 不在默认治理入口中启动真实模型。
- 不接入 Linear / live eval；外部凭证场景仍由显式 live e2e 脚本保护。
- 不接入 GitHub PR、merge time、review finding、CI artifact 或 AI credits 指标。

## 范围

- `scripts/e2e/run-symphony-agent-eval.sh`
- `docs/agent-evals/datasets/memory-requirement-intake.yml`
- `docs/agent-evals/datasets/memory-feature-spec-required.yml`
- `docs/agent-evals/datasets/memory-execution-plan-required.yml`
- `docs/agent-evals/datasets/memory-openspec-change-required.yml`
- `docs/agent-evals/datasets/memory-docs-governance.yml`
- `docs/agent-evals/datasets/memory-permission-preflight.yml`
- `docs/agent-evals/datasets/memory-writable-root-preflight.yml`
- `docs/agent-evals/datasets/memory-after-create-hook-failure.yml`
- `docs/agent-evals/datasets/memory-before-run-hook-failure.yml`
- `docs/agent-evals/datasets/memory-codex-turn-failure.yml`
- `docs/agent-evals/datasets/memory-codex-turn-response-timeout.yml`
- `docs/agent-evals/datasets/memory-codex-turn-timeout.yml`
- `docs/agent-evals/datasets/memory-codex-turn-cancelled.yml`
- `docs/agent-evals/datasets/memory-codex-approval-required.yml`
- `docs/agent-evals/datasets/memory-codex-dynamic-tool-failure.yml`
- `docs/agent-evals/datasets/memory-codex-user-input-required.yml`
- `docs/agent-evals/datasets/memory-codex-malformed-event.yml`
- `docs/agent-evals/datasets/memory-codex-startup-failure.yml`
- `scripts/harness/run-agent-evals.sh`
- `scripts/harness/check-agentic-harness-assets.sh`
- `scripts/harness/generate-harness-metrics-report.sh`
- `scripts/harness/check-harness-metrics-report.sh`
- `docs/agent-evals/README.md`
- `docs/reports/harness-metrics/README.md`
- `docs/asset-manifest.yml`
- `openspec/specs/harness-governance/spec.md`
- `openspec/specs/symphony-run-history/spec.md`
- 质量信号文档：`QUALITY_SCORE.md`、`docs/reports/ROADMAP.md`、`docs/governance/CHECKLIST.md`、`docs/README.md`

## 交付内容

- `scripts/e2e/run-symphony-agent-eval.sh` 会生成临时 workflow、memory issue、workspace、fake Codex app-server 和 SQLite history 路径。
- 脚本会启动本地 Symphony，调用 `POST /api/v1/refresh`，并验证：
  - workspace 中存在期望 marker 文件；
  - JSON run summary 状态为 `completed`；
  - `/api/v1/history/runs` 返回对应 completed run；
  - `/api/v1/history/runs/{runId}/events` 包含 `run_started`、`session_started`、`thread/tokenUsage/updated` 和 `turn_completed`；
  - `/api/v1/history/metrics` 返回至少 1 条 completed run、状态分布和 token 汇总；
  - 本次 eval artifact 包含 `harness-metrics/latest.json` 与 `harness-metrics/latest.md`；
  - token 汇总可从历史接口读取。
- `memory-requirement-intake` dataset 覆盖报告 P0-1 首批建议中的模糊需求 intake 场景：fake Codex 会断言 turn prompt 携带 `artemis-symphony/prompts/agent-requirement-intake.md`、模糊需求分流规则和原始 issue 语境，并在 workspace 写入低敏需求受理模板。
- `memory-feature-spec-required` dataset 覆盖报告 P0-1 首批建议中的 Feature Spec 需求场景：fake Codex 会断言 turn prompt 携带 Spec-driven delivery guidance、`docs/feature-specs/README.md`、`artemis-symphony/prompts/spec-driven-delivery.md` 和原始业务需求语境，并在 workspace 写入低敏 Feature Spec 模板产物。
- `memory-execution-plan-required` dataset 覆盖报告 P0-1 首批建议中的跨模块执行计划场景：fake Codex 会断言 turn prompt 携带 `docs/exec-plans/templates/execution-plan-template.md`、复杂任务分阶段规则和原始迁移语境，并在 workspace 写入低敏执行计划模板产物。
- `memory-openspec-change-required` dataset 覆盖稳定规则变更场景：fake Codex 会断言 turn prompt 携带 `openspec/changes/` 分流规则、稳定规则变更说明和原始 workflow 规则变更语境，并在 workspace 写入低敏 OpenSpec proposal 产物。
- `memory-permission-preflight` dataset 覆盖权限预检失败路径：workflow 请求 `networkAccess: true` 但不提供 `permissions.network_access_reason`，脚本验证 Codex app-server 未启动、workspace marker 未写入、summary / history 为 `failed`，历史事件包含 `run_started` / `run_failed`，且无 turn / token 事件。
- `memory-writable-root-preflight` dataset 覆盖文件系统越界失败路径：workflow 请求 issue workspace 之外的 `writableRoots` 但未配置 `permissions.allowed_writable_roots`，脚本验证 Codex app-server 未启动、workspace marker 未写入、summary / history 为 `failed`，summary 权限快照包含越界 root，且历史事件中无 turn / token 事件。
- `memory-after-create-hook-failure` dataset 覆盖 workspace 创建后 hook 失败路径：workflow 配置失败的 `after_create` hook，脚本验证 issue workspace 已创建、Codex app-server 未启动、workspace marker 未写入、summary / history 为 `failed`，历史事件包含 `run_started` 和 `run_failed`，且无 session / turn / token 事件。
- `memory-before-run-hook-failure` dataset 覆盖 worker 生命周期 hook 失败路径：workflow 配置失败的 `before_run` hook，脚本验证 Codex app-server 未启动、workspace marker 未写入、summary / history 为 `failed`，历史事件包含 `run_started` 和 `run_failed`，且无 session / turn / token 事件。
- `memory-codex-turn-failure` dataset 覆盖 Codex runtime 失败路径：fake app-server 成功启动并写入 token usage 后发出 `turn/failed`，脚本验证 workspace marker 未写入、summary / history 为 `failed`，历史事件包含 `session_started`、`thread/tokenUsage/updated`、`turn_failed` 和 `run_failed`，并验证 summary / history / metrics 中保留 token 汇总。
- `memory-codex-turn-response-timeout` dataset 覆盖 Codex turn/start 响应超时路径：fake app-server 成功启动后不响应 `turn/start`，脚本验证 workspace marker 未写入、summary / history 为 `failed`，历史事件包含 `session_started`、`turn_ended_with_error` 和 `run_failed`，且不包含 token usage 或终态 turn 事件。
- `memory-codex-turn-timeout` dataset 覆盖 Codex turn 超时路径：fake app-server 成功启动并写入 token usage 后不再发出终态事件，脚本验证 workspace marker 未写入、summary / history 为 `failed`，历史事件包含 `session_started`、`thread/tokenUsage/updated`、`turn_ended_with_error` 和 `run_failed`，且不包含 `turn_completed` / `turn_failed` / `turn_cancelled`。
- `memory-codex-turn-cancelled` dataset 覆盖 Codex turn 取消路径：fake app-server 成功启动并写入 token usage 后发出 `turn/cancelled`，脚本验证 workspace marker 未写入、summary / history 为 `failed`，历史事件包含 `session_started`、`thread/tokenUsage/updated`、`turn_cancelled` 和 `run_failed`，并验证 summary / history / metrics 中保留 token 汇总。
- `memory-codex-approval-required` dataset 覆盖 Codex 审批请求失败路径：fake app-server 成功启动并写入 token usage 后发出 command approval request，workflow 使用 `codex.approval_policy: on-request`，脚本验证 workspace marker 未写入、summary / history 为 `failed`，历史事件包含 `approval_required`、`turn_ended_with_error` 和 `run_failed`，且不包含 `approval_auto_approved`。
- `memory-codex-dynamic-tool-failure` dataset 覆盖 Codex 动态工具失败路径：fake app-server 成功启动并写入 token usage 后发出未知动态工具调用，Symphony 回传结构化失败结果并中止 turn，脚本验证 workspace marker 未写入、summary / history 为 `failed`，历史事件包含 `tool_call_failed`、`turn_ended_with_error` 和 `run_failed`。
- `memory-codex-user-input-required` dataset 覆盖 Codex 非交互用户输入阻断路径：fake app-server 成功启动并写入 token usage 后发出无法自动回答的 `item/tool/requestUserInput` 请求，脚本验证 workspace marker 未写入、summary / history 为 `failed`，历史事件包含 `turn_input_required`、`turn_ended_with_error` 和 `run_failed`，且不包含 `tool_input_auto_answered`。
- `memory-codex-malformed-event` dataset 覆盖 Codex malformed stdout 观测路径：fake app-server 成功启动并写入 token usage，随后输出一行非法 JSON 再正常完成 turn，脚本验证 workspace marker 已写入、summary / history 为 `completed`，历史事件包含 `malformed` 和 `turn_completed`。
- `memory-codex-startup-failure` dataset 覆盖 Codex 启动握手失败路径：fake app-server 进程已启动但 initialize 返回错误，脚本验证 workspace marker 未写入、summary / history 为 `failed`，历史事件包含 `startup_failed` 和 `run_failed`，且无 session / turn / token 事件。
- `/api/v1/history/metrics?limit=100` 基于最近 N 条本机 SQLite history 记录实时汇总状态、重试数、token、平均耗时和时间窗口。
- `/api/v1/history/metrics?limit=100` 同时输出稳定失败类别分布，避免 operator 只能按完整错误文本复盘失败趋势。
- `/runs` 页面展示运行指标摘要、状态分布、失败类别分布、成功率、平均耗时，并保留最近 run 与事件查看入口。
- `scripts/harness/generate-harness-metrics-report.sh` 默认聚合 `artifacts/agent-evals/` 与 `artifacts/agent-runs/`，输出 `latest.json` 和 `latest.md`，并优先使用低敏 summary 中的 `failure_category`。
- `scripts/harness/check-harness-metrics-report.sh` 使用临时低敏 fixture 验证报告生成器，不依赖本机 artifacts。
- eval 构建使用 `clean package -DskipTests`，避免启动包复用旧 target 时漏带新增类。
- eval 产物写入 `artifacts/agent-evals/`，并通过 `.gitignore` 排除。

## 风险与处理

- memory issue 在完成后仍可能保持 active 状态，脚本断言按完成态 summary 的 run id 对齐历史记录，避免后续 tick 覆盖本次通过证据。
- fake Codex 只写低敏 marker 文件，不输出完整 prompt、密钥或外部响应。
- 当前 dataset 已覆盖需求受理成功、Feature Spec 分流成功、执行计划分流成功、OpenSpec 规则变更分流成功、docs-only 成功、权限预检失败、after_create hook 失败、before_run hook 失败、Codex 启动失败、Codex turn 失败、Codex turn/start 响应超时、Codex turn 超时、Codex turn 取消、Codex 审批请求失败、动态工具失败、非交互用户输入阻断和 malformed stdout 观测场景，但仍不能代表真实模型质量或 Linear 外部副作用质量。

## 验收映射

| 验收编号 | 验证入口 | 通过标准 |
|----------|----------|----------|
| AC-001 | `scripts/e2e/run-symphony-agent-eval.sh` | 输出 `status: passed`，并写入 `artifacts/agent-evals/<run>/summary.json` 与本次 `harness-metrics/latest.*` |
| AC-001A | `scripts/e2e/run-symphony-agent-eval.sh memory-requirement-intake` | 输出 `status: passed`，被评测 run 为 `completed`，workspace 写入需求受理模板，且 fake Codex 已验证 prompt 中存在需求受理资产和原始模糊需求语境 |
| AC-001AA | `scripts/e2e/run-symphony-agent-eval.sh memory-feature-spec-required` | 输出 `status: passed`，被评测 run 为 `completed`，workspace 写入 Feature Spec 模板产物，且 fake Codex 已验证 prompt 中存在 SDD 指引、Feature Spec 入口和原始业务需求语境 |
| AC-001AB | `scripts/e2e/run-symphony-agent-eval.sh memory-execution-plan-required` | 输出 `status: passed`，被评测 run 为 `completed`，workspace 写入执行计划模板产物，且 fake Codex 已验证 prompt 中存在执行计划模板、复杂任务规则和原始迁移语境 |
| AC-001AC | `scripts/e2e/run-symphony-agent-eval.sh memory-openspec-change-required` | 输出 `status: passed`，被评测 run 为 `completed`，workspace 写入 OpenSpec proposal 产物，且 fake Codex 已验证 prompt 中存在 `openspec/changes/` 分流规则和原始 workflow 规则变更语境 |
| AC-001B | `scripts/e2e/run-symphony-agent-eval.sh memory-permission-preflight` | 输出 `status: passed`，但被评测 run 本身为预期 `failed`，失败原因包含网络权限预检信息且 Codex 未启动 |
| AC-001C | `scripts/e2e/run-symphony-agent-eval.sh memory-writable-root-preflight` | 输出 `status: passed`，但被评测 run 本身为预期 `failed`，失败原因包含 writable root 越界信息且 Codex 未启动 |
| AC-001D | `scripts/e2e/run-symphony-agent-eval.sh memory-after-create-hook-failure` | 输出 `status: passed`，但被评测 run 本身为预期 `failed`，失败原因包含 after_create hook 失败信息，workspace 目录已创建且 Codex 未启动 |
| AC-001E | `scripts/e2e/run-symphony-agent-eval.sh memory-before-run-hook-failure` | 输出 `status: passed`，但被评测 run 本身为预期 `failed`，失败原因包含 before_run hook 失败信息，且 Codex 未启动 |
| AC-001F | `scripts/e2e/run-symphony-agent-eval.sh memory-codex-turn-failure` | 输出 `status: passed`，但被评测 run 本身为预期 `failed`，失败原因包含 Codex turn 失败信息，且保留 session、token、turn_failed 与 metrics 证据 |
| AC-001G | `scripts/e2e/run-symphony-agent-eval.sh memory-codex-turn-response-timeout` | 输出 `status: passed`，但被评测 run 本身为预期 `failed`，失败原因包含 Codex turn 响应超时信息，且保留 session 与 turn_ended_with_error 证据但无 token 事件 |
| AC-001H | `scripts/e2e/run-symphony-agent-eval.sh memory-codex-turn-timeout` | 输出 `status: passed`，但被评测 run 本身为预期 `failed`，失败原因包含 Codex turn 超时信息，且保留 session、token、turn_ended_with_error 与 metrics 证据 |
| AC-001I | `scripts/e2e/run-symphony-agent-eval.sh memory-codex-turn-cancelled` | 输出 `status: passed`，但被评测 run 本身为预期 `failed`，失败原因包含 Codex turn 取消信息，且保留 session、token、turn_cancelled 与 metrics 证据 |
| AC-001J | `scripts/e2e/run-symphony-agent-eval.sh memory-codex-approval-required` | 输出 `status: passed`，但被评测 run 本身为预期 `failed`，失败原因包含 Codex 审批请求信息，且保留 session、token、approval_required、turn_ended_with_error 与 metrics 证据 |
| AC-001K | `scripts/e2e/run-symphony-agent-eval.sh memory-codex-dynamic-tool-failure` | 输出 `status: passed`，但被评测 run 本身为预期 `failed`，失败原因包含动态工具失败信息，且保留 session、token、tool_call_failed、turn_ended_with_error 与 metrics 证据 |
| AC-001L | `scripts/e2e/run-symphony-agent-eval.sh memory-codex-user-input-required` | 输出 `status: passed`，但被评测 run 本身为预期 `failed`，失败原因包含用户输入阻断信息，且保留 session、token、turn_input_required、turn_ended_with_error 与 metrics 证据 |
| AC-001M | `scripts/e2e/run-symphony-agent-eval.sh memory-codex-malformed-event` | 输出 `status: passed`，被评测 run 为 `completed`，workspace marker 已写入，且历史事件包含 malformed、token usage 和 turn_completed |
| AC-001N | `scripts/e2e/run-symphony-agent-eval.sh memory-codex-startup-failure` | 输出 `status: passed`，但被评测 run 本身为预期 `failed`，失败原因包含启动握手失败信息，且无 session、turn 或 token 事件 |
| AC-002 | `scripts/harness/run-agent-evals.sh` | fixture 与 dataset 结构检查通过 |
| AC-003 | `scripts/harness/check-agentic-harness-assets.sh` | 新 dataset 与 e2e 脚本被资产守门覆盖 |
| AC-004 | `scripts/harness/verify-changed.sh working-tree` | 治理检查与变更范围 Maven verify 通过 |
| AC-005 | `bash -lc 'source scripts/lib/common.sh; run_mvn -B -pl artemis-symphony/artemis-symphony-persistence,artemis-symphony/artemis-symphony-start -am test'` | SQLite history metrics 聚合、失败类别分类、HTTP metrics API 和 `/runs` 页面测试通过 |
| AC-006 | `scripts/harness/check-harness-metrics-report.sh` | Harness metrics report 生成器可输出结构化 JSON / Markdown，并聚合稳定失败类别 |

## 验证记录

- `scripts/e2e/run-symphony-agent-eval.sh`
- `scripts/e2e/run-symphony-agent-eval.sh memory-requirement-intake`
- `scripts/e2e/run-symphony-agent-eval.sh memory-feature-spec-required`
- `scripts/e2e/run-symphony-agent-eval.sh memory-execution-plan-required`
- `scripts/e2e/run-symphony-agent-eval.sh memory-openspec-change-required`
- `scripts/e2e/run-symphony-agent-eval.sh memory-permission-preflight`
- `scripts/e2e/run-symphony-agent-eval.sh memory-writable-root-preflight`
- `scripts/e2e/run-symphony-agent-eval.sh memory-after-create-hook-failure`
- `scripts/e2e/run-symphony-agent-eval.sh memory-before-run-hook-failure`
- `scripts/e2e/run-symphony-agent-eval.sh memory-codex-turn-failure`
- `scripts/e2e/run-symphony-agent-eval.sh memory-codex-turn-response-timeout`
- `scripts/e2e/run-symphony-agent-eval.sh memory-codex-turn-timeout`
- `scripts/e2e/run-symphony-agent-eval.sh memory-codex-turn-cancelled`
- `scripts/e2e/run-symphony-agent-eval.sh memory-codex-approval-required`
- `scripts/e2e/run-symphony-agent-eval.sh memory-codex-dynamic-tool-failure`
- `scripts/e2e/run-symphony-agent-eval.sh memory-codex-user-input-required`
- `scripts/e2e/run-symphony-agent-eval.sh memory-codex-malformed-event`
- `scripts/e2e/run-symphony-agent-eval.sh memory-codex-startup-failure`
- `scripts/harness/run-agent-evals.sh`
- `scripts/harness/check-agentic-harness-assets.sh`
- `scripts/harness/check-harness-metrics-report.sh`
- `bash -lc 'source scripts/lib/common.sh; run_mvn -B -pl artemis-symphony/artemis-symphony-persistence,artemis-symphony/artemis-symphony-start -am test'`
- `bash -n scripts/e2e/run-symphony-agent-eval.sh scripts/harness/run-agent-evals.sh scripts/harness/check-agentic-harness-assets.sh`
- `git diff --check -- .gitignore artemis-symphony/WORKFLOW.md.example scripts/e2e/run-symphony-agent-eval.sh scripts/harness/run-agent-evals.sh scripts/harness/check-agentic-harness-assets.sh docs/agent-evals/README.md docs/agent-evals/datasets/memory-requirement-intake.yml docs/agent-evals/datasets/memory-feature-spec-required.yml docs/agent-evals/datasets/memory-execution-plan-required.yml docs/agent-evals/datasets/memory-openspec-change-required.yml docs/agent-evals/datasets/memory-docs-governance.yml docs/agent-evals/datasets/memory-after-create-hook-failure.yml docs/agent-evals/datasets/memory-codex-turn-response-timeout.yml docs/agent-evals/datasets/memory-codex-malformed-event.yml docs/asset-manifest.yml openspec/specs/harness-governance/spec.md openspec/specs/symphony-run-history/spec.md`
- `scripts/harness/verify-changed.sh working-tree`

## 后续

- 增加更多 memory eval case，覆盖更多失败样例和 Linear / live 外部副作用保护场景。
- 将 Linear / live eval 保持在显式凭证与开关保护下。
- 将 metrics 快照继续接入 GitHub Actions artifact、PR / merge / revert / review finding 和部署演练趋势。
