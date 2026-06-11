# Agent Evals

Status: maintained
Last Reviewed: 2026-06-10
Review Cadence: 90 days

本目录存放 agentic 开发流程的评测资产，用于验证 agent 是否按仓库规则工作，而不只验证代码是否能编译。

## 评测目标

当前评测先覆盖流程合规性：

- 模糊需求是否先进入需求受理模板。
- 业务需求是否要求 Feature Spec 和验收映射。
- 复杂任务是否要求执行计划。
- 稳定规则变化是否要求 OpenSpec。
- 高风险改动是否触发安全审查和权限策略。
- 权限姿态是否覆盖网络访问拒绝与有理由放行路径。
- 最终 handoff 是否包含验证证据、验收映射和剩余风险。

## 目录约定

- `fixtures/`
  评测用例。每个文件描述一个 issue 风格任务、期望 agent 使用的资产和必须触发的验证入口。
- `datasets/`
  可执行评测数据集。每个文件描述一个可由脚本启动的 Symphony memory tracker 场景、期望产物和必须观察到的运行事件。
- `README.md`
  评测目的、目录结构和运行方式。

## 运行方式

```bash
scripts/harness/run-agent-evals.sh
```

该脚本执行轻量结构检查：检查 fixture 与 dataset 是否完整、期望资产是否存在、期望验证入口是否真实可执行。它不启动模型或服务，适合 governance / CI 默认路径。

需要验证真实 Symphony 编排链路时，运行：

```bash
scripts/e2e/run-symphony-agent-eval.sh
```

该脚本默认运行 `memory-docs-governance`。需要运行单个指定 dataset 时传入 id：

```bash
scripts/e2e/run-symphony-agent-eval.sh memory-permission-preflight
```

需要验证完整 memory regression suite 时，运行：

```bash
scripts/e2e/run-symphony-agent-eval.sh all
```

`all` 会自动发现 `docs/agent-evals/datasets/*.yml`，只构建一次 Symphony start jar，然后顺序运行每个 memory dataset。也可以传入显式子集：

```bash
scripts/e2e/run-symphony-agent-eval.sh all memory-docs-governance memory-permission-preflight
```

该脚本会启动本地 Symphony、memory tracker 和可控 fake Codex app-server，触发 `POST /api/v1/refresh`，并验证 workspace 产物、SQLite 运行历史、运行指标摘要、Codex 事件和低敏 agent run summary。低敏 summary 必须包含 `codex.event_counts`、`environment` 与 `external_effects.events`；`codex.event_counts` 只记录 Codex 事件类型及次数，脚本会把 dataset `expected_events` 中的非 `run_*` 事件映射到该字段做断言；`environment` 记录 Java、OS、架构和 Spring profile 等低敏运行环境；`external_effects.events` 用于审计 tracker 状态认领、Linear 评论等外部写操作的类型、目标、状态和低敏错误元数据。dataset 可以声明成功路径，也可以声明预期失败路径；例如需求受理样例会断言 turn prompt 携带 `artemis-symphony/prompts/agent-requirement-intake.md` 和模糊需求语境；Feature Spec 样例会断言 turn prompt 携带 Spec-driven delivery guidance、`docs/feature-specs/README.md` 和业务需求语境，并在 workspace 写入低敏 Feature Spec 模板产物；真实租户套餐同步样例会引用已完成的 `docs/feature-specs/completed/2026-06-01-tenant-creation-initialization.md` 与 `TENANT_PACKAGE_API.md`，验证真实业务上下文也能触发 Feature Spec、验收标准和验证映射要求；执行计划样例会断言 turn prompt 携带 `docs/exec-plans/templates/execution-plan-template.md`、复杂任务分阶段规则和迁移语境，并在 workspace 写入低敏执行计划模板产物；OpenSpec 样例会断言 turn prompt 携带稳定规则变更需要 `openspec/changes/` 的分流规则和原始 workflow 规则变更语境，并在 workspace 写入低敏 OpenSpec proposal 产物；高风险 adversarial review 样例会断言 turn prompt 携带安全审查清单、权限 runbook、`artemis-symphony/prompts/adversarial-review.md`、`artemis-symphony/skills/adversarial-review.md` 和 `artemis-symphony/tools/registry.json`；独立 adversarial review 样例会开启 `delivery.adversarial_review.enabled`，并断言同一 issue 产生 `implementation` 与 `adversarial_review` 两类 run summary；Linear 评论写回样例会在临时 workflow 中开启 `reporting.linear_comments.enabled`，并断言 memory tracker comment 写回对应的 `linear_comment:succeeded` 外部副作用事件；Linear 评论失败审计样例会断言 comment 写回失败只进入 `linear_comment:failed` 外部副作用事件和稳定错误码，不会让 worker attempt 失败；权限预检失败、after_create hook 失败或 before_run hook 失败时会断言 Codex app-server 未启动、无 turn / token 事件，并记录失败摘要与指标；Codex 启动握手失败时会断言 app-server 已启动但无 session / turn / token 事件；Codex turn 响应超时时会断言 app-server 已启动、`turn_ended_with_error` 被保留且无 token 事件；Codex turn 失败、取消或超时时会断言 app-server 已启动、token 事件已落库、summary / history / metrics 都记录失败，并保留 `turn_failed`、`turn_cancelled` 或 `turn_ended_with_error` 事件；审批请求失败时会断言 `approval_required` 和 `turn_ended_with_error` 事件被保留，且未发生自动批准；动态工具失败时会断言 `tool_call_failed` 和 `turn_ended_with_error` 被保留；非交互用户输入阻断时会断言 `turn_input_required` 和 `turn_ended_with_error` 被保留；malformed stdout 样例会断言 `malformed` 事件进入 run history 且不破坏后续成功 turn。所有可执行 dataset 都应观察到 `run_started`，确保 run trace 有明确起点；所有会启动 Codex 的 dataset 都应至少声明一个 Codex 事件，确保 summary 事件计数断言不会空转。通过后会为本次 eval 或 suite 生成 `harness-metrics/latest.json` 与 `harness-metrics/latest.md`，并把路径写入 `summary.json`；脚本还会用本次 agent run summaries 重新计算 `agent_runs.permission_posture`，确认 Harness metrics 权限姿态快照没有遗漏或漂移。默认产物写入 `artifacts/agent-evals/`，该目录不提交。

## Fixture 结构

每个 fixture 必须包含：

- `id`
- `title`
- `risk_level`
- `issue`
- `expected_assets`
- `expected_validations`
- `review_focus`

`expected_assets` 和 `expected_validations` 中引用的仓库路径必须真实存在。

## Dataset 结构

每个可执行 dataset 必须包含：

- `id`
- `title`
- `risk_level`
- `tracker`
- `issue_id`
- `issue_identifier`
- `issue_title`
- `expected_workspace_file`
- `expected_workspace_contains`
- `expected_summary_status`
- `expected_history_status`
- `expected_events`
- `required_validations`

dataset 的 `id` 必须等于文件名去掉 `.yml` 后的值，并且在 `docs/agent-evals/datasets/` 下唯一。`scripts/e2e/run-symphony-agent-eval.sh <id>` 使用文件名作为可执行入口；静态检查会拒绝 id / 文件名不一致或 id 重复的 dataset。

dataset 可额外包含：

- `expected_workspace_present`
- `expected_workspace_directory_present`
- `expected_codex_started`
- `expected_agent_run_count`
- `expected_failure_reason_contains`
- `expected_failure_category`
- `expected_summary_dispatch_kind`
- `expected_summary_dispatch_kinds`
- `expected_summary_workspace_files`
- `expected_permission_network_access`
- `expected_permission_thread_sandbox`
- `expected_permission_network_access_reason`
- `expected_permission_writable_root_contains`
- `expected_permission_allowed_writable_root_contains`
- `expected_permission_danger_full_access_allowed`
- `expected_min_total_tokens`
- `expected_metrics_total_runs`
- `expected_external_effects`
- `expected_external_effect_details`
- `codex_read_timeout_ms`
- `codex_turn_timeout_ms`
- `codex_thread_sandbox`
- `codex_turn_sandbox_network_access`
- `codex_turn_sandbox_extra_writable_root`
- `permissions_network_access_reason`
- `permissions_allowed_writable_root`
- `permissions_allow_danger_full_access`
- `codex_startup_outcome`
- `codex_turn_outcome`
- `codex_approval_policy`
- `agent_max_retry_backoff_ms`
- `hook_after_create_outcome`
- `hook_before_run_outcome`
- `spec_driven_delivery_enabled`
- `adversarial_review_enabled`
- `adversarial_review_issue_title_regex`
- `linear_comment_reporting_enabled`
- `linear_comment_success_template`
- `linear_comment_failure_template`
- `linear_comment_issue_title_regex`
- `prompt_must_contain`
- `forbidden_events`

`codex_turn_outcome` 当前支持 `completed`、`completed_with_malformed`、`failed`、`cancelled`、`approval_required`、`dynamic_tool_failure`、`user_input_required`、`timeout` 和 `response_timeout`。

`prompt_must_contain` 用于成功路径流程评测，fake Codex 会在 `turn/start` 时检查 prompt 是否包含这些低敏标记；若缺失则让该 turn 失败，从而暴露 workflow prompt 注入或需求分流规则漂移。

`spec_driven_delivery_enabled` 用于 Feature Spec / SDD 流程评测，脚本会在临时 workflow 中打开 `delivery.spec_driven.enabled` 并注入与默认配置对齐的 Feature Spec、执行计划、安全审查、adversarial review 和工具注册表资产。

`adversarial_review_enabled` 用于在临时 workflow 中开启 `delivery.adversarial_review.enabled`。配合 `adversarial_review_issue_title_regex` 可以验证实现 run 完成后自动派生独立只读 adversarial review run，并通过 `expected_summary_dispatch_kinds` 断言 summary 中出现 `adversarial_review`。

`expected_failure_category` 用于失败路径评测，脚本会同时断言低敏 run summary 和 history metrics 中的稳定失败分类，避免 dashboard 只能按完整错误文本聚合。

`expected_permission_network_access` 和 `expected_permission_network_access_reason` 用于断言低敏 run summary 中的权限姿态。需要验证允许网络访问的成功路径时，同时配置 `codex_turn_sandbox_network_access: true` 与 `permissions_network_access_reason`，脚本会确认 preflight 放行、summary 保留 reason，并由 `harness-metrics/latest.json` 的 `agent_runs.permission_posture` 计入网络访问 run。

`codex_thread_sandbox` 和 `permissions_allow_danger_full_access` 用于验证高风险 sandbox 的显式放行路径。需要覆盖 `danger-full-access` 成功路径时，同时配置 `codex_thread_sandbox: danger-full-access`、`permissions_allow_danger_full_access: true`、`expected_permission_thread_sandbox` 和 `expected_permission_danger_full_access_allowed`；脚本会确认 permission preflight 放行、summary 记录高风险姿态，并由 `agent_runs.permission_posture.danger_full_access_allowed_runs` 计数。

`permissions_allowed_writable_root` 用于在临时 workflow 的 `permissions.allowed_writable_roots` 中声明额外可写目录，目前支持 `artifact` 作为低敏合成目录标记。配合 `codex_turn_sandbox_extra_writable_root: artifact`、`expected_permission_writable_root_contains` 和 `expected_permission_allowed_writable_root_contains` 可以验证额外 writable root 被显式允许后的成功路径，并确认 run summary 只保留 `configured-writable-root` 形式的低敏引用。

`expected_agent_run_count` 用于断言本 dataset 对应 issue 产生的低敏 run summary 数量，默认值为 `1`；`expected_summary_dispatch_kind` 用于选择主 summary，`expected_summary_dispatch_kinds` 用于断言必须出现的 run 类型。

`expected_summary_workspace_files` 用于断言低敏 run summary 的 `workspace.artifact_inventory.files` 包含指定 workspace 相对路径。脚本会自动把成功路径的 `expected_workspace_file` 纳入该断言；该 inventory 只记录相对路径、文件数量和字节数，不读取或保存文件内容。

`expected_metrics_total_runs` 用于断言单个 dataset 允许产生的 run 数量，默认值为 `1`；只有专门验证重试窗口或自动派生 review run 的样例应显式提高该值。

`expected_external_effects` 用于断言低敏 run summary 中必须出现的外部副作用审计事件，格式为 `type:status`，例如 `tracker_state_update:succeeded`。

`expected_external_effect_details` 用于断言外部副作用审计事件的稳定错误码，格式为 `type:status:error_code`，例如 `linear_comment:failed:memory_comment_body`。它只检查低敏错误码，不要求也不允许依赖完整外部响应文本。

`linear_comment_reporting_enabled` 用于在单个 memory dataset 生成的临时 workflow 中开启 `reporting.linear_comments.enabled`。配合 `linear_comment_success_template`、`linear_comment_failure_template` 和 `linear_comment_issue_title_regex` 可以覆盖评论渲染与写回审计路径；未显式配置成功模板时，脚本会使用只包含 issue identifier 和 attempt outcome 的低敏默认模板。

## 失败 run 转回归草稿

当 Symphony 产生失败的低敏 `summary_type=symphony_agent_run` JSON 后，可以先生成 memory eval dataset 草稿：

```bash
scripts/harness/generate-agent-eval-drafts.sh artifacts/agent-runs
scripts/harness/generate-agent-eval-drafts.sh artifacts/agent-evals/<run>/agent-runs
```

默认输出到 `artifacts/agent-eval-drafts/`，该目录不提交。草稿只从低敏 summary 中提取 run id、失败分类、事件计数、token 合计、权限摘要和外部副作用稳定错误码，不读取 prompt、聊天记录、stdout 原文、工具输出或外部响应全文。

Harness metrics 会聚合 `artifacts/agent-eval-drafts/*.yml` 的低敏 backlog 指标，包括草稿总数、人工复核要求、失败分类和风险等级分布，帮助判断失败样例是否及时转为可复核回归线索。

草稿必须人工复核后才能移动到 `docs/agent-evals/datasets/`：维护者需要把 `issue_description` 改成最小稳定复现，确认 `codex_turn_outcome` / `codex_startup_outcome` 与真实失败一致，并删除 `manual_review_required`、`source_summary`、`source_run_id` 和生成器占位文案等草稿元信息。`scripts/harness/run-agent-evals.sh` 会拒绝仍带草稿元信息的正式 dataset；通过静态检查后，再运行对应 `scripts/e2e/run-symphony-agent-eval.sh <id>`。

当前可执行脚本只支持 `tracker: memory`，Linear / live eval 需要显式凭证和单独开关。
