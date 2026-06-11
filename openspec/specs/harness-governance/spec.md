## ADDED Requirements

### Requirement: 周期性治理任务

仓库 SHALL 提供一个可周期执行的治理入口，用于统一执行文档整理、重复模式扫描、质量问题检查与相关报告输出。该入口 SHALL 可在本地脚本和 CI 定时任务中复用，避免出现“规则写了但无人执行”的状态。

#### Scenario: CI 定时执行治理入口

- **WHEN** GitHub Actions 的定时任务触发
- **THEN** SHALL 调用仓库内治理脚本，完成文档与工程治理检查，并输出可定位失败原因的日志

#### Scenario: CI 与本地治理入口保持一致

- **WHEN** GitHub Actions 执行治理检查
- **THEN** SHALL 复用 `scripts/harness/run-governance-checks.sh`
- **AND** SHOULD NOT 在 workflow 中复制一份容易漂移的治理脚本清单

### Requirement: 重复模式扫描

仓库 SHALL 提供重复模式扫描脚本，至少覆盖以下高频熵增点：

- DTO / Request / Response 类
- 异常处理类
- `scripts/` 下的 shell 入口

扫描脚本 SHALL 使用稳定、可解释的归一化规则识别高度重复实现，并在发现重复模式时给出对应文件列表。

#### Scenario: 扫描重复 DTO

- **WHEN** 两个 DTO 或 Request/Response 文件在去除 package/import/空白差异后内容完全一致
- **THEN** 扫描脚本 SHALL 报告重复文件组并使检查失败

### Requirement: 质量问题归档与关闭标准

仓库 SHALL 为质量问题提供统一归档与关闭标准，至少定义：

- 问题如何建档
- 什么信号可视为关闭
- 何时从 active 状态迁移到 archive

该标准 SHALL 沉淀在仓库文档中，并被周期性治理入口引用。

#### Scenario: 质量问题达到关闭条件

- **WHEN** 某质量问题对应的脚本、测试、文档与验证入口均已落仓，并在标准验证中通过
- **THEN** 该问题 SHALL 可按文档约定迁移到 archive，并记录关闭日期与验证方式

### Requirement: OpenSpec active change 状态守门

仓库 SHALL 提供 OpenSpec active change 状态检查，并将其接入周期性治理入口。检查至少 SHALL 要求：

- active change 必须包含 `proposal.md`
- active change 必须包含 `tasks.md`
- `tasks.md` 必须包含 checkbox 任务
- 已全部完成的 active change 必须归档到 `openspec/changes/archive/YYYY-MM-DD-<name>/`
- archive 目录下不应存在未被版本控制跟踪的归档文件；若同一工作树中存在对应 active change 文件删除，视为正在进行的合法归档移动

#### Scenario: 已完成 OpenSpec change 留在 active 区

- **WHEN** `openspec/changes/<name>/tasks.md` 中所有 checkbox 任务均已勾选
- **THEN** OpenSpec change 状态检查 SHALL 失败
- **AND** SHALL 提示将该 change 归档到 `openspec/changes/archive/YYYY-MM-DD-<name>/`

#### Scenario: active OpenSpec change 缺少结构文件

- **WHEN** `openspec/changes/<name>/` 缺少 `proposal.md` 或 `tasks.md`
- **THEN** OpenSpec change 状态检查 SHALL 失败并指出缺失文件

### Requirement: Agentic Harness 资产必须可验证

仓库 SHALL 为 agentic 开发优化资产提供专项守门，至少覆盖：

- agent eval fixture
- executable memory agent eval dataset
- agent 运行轨迹摘要规则
- agent run summary sensitive-content checker
- harness metrics report generator
- Symphony dynamic tool registry
- agent 权限策略 runbook
- threat model
- 风险分级验证 runbook
- 安全审查清单
- adversarial review prompt / skill
- 跨 agent 工具的薄指针文件

#### Scenario: 新增 agentic 开发资产

- **WHEN** 仓库新增或修改 agentic 开发资产
- **THEN** `scripts/harness/check-agentic-harness-assets.sh` SHALL 能检查关键文件存在和核心索引可发现
- **AND** `scripts/harness/run-agent-evals.sh` SHALL 能验证 eval fixture 的基础结构和路径引用

#### Scenario: 验证独立 adversarial review run

- **WHEN** executable memory eval 开启 `delivery.adversarial_review.enabled`
- **THEN** `scripts/e2e/run-symphony-agent-eval.sh` SHALL 能断言同一 issue 产生 `implementation` 与 `adversarial_review` 两类低敏 run summary
- **AND** dataset SHALL 能通过 `expected_agent_run_count`、`expected_summary_dispatch_kind` 和 `expected_summary_dispatch_kinds` 明确声明期望 run 数量与 dispatch kind
- **AND** agent run summary schema-lite SHALL 要求 `attempt.dispatch_kind` 与 `attempt.parent_run_id`

#### Scenario: 检查 Symphony 动态工具注册表

- **WHEN** 操作者运行 `scripts/harness/check-symphony-assets.sh`
- **THEN** 脚本 SHALL 检查 `artemis-symphony/tools/registry.json` 存在且声明 `registry_type=symphony_tool_registry`
- **AND** SHALL 验证 `linear_graphql` 条目声明 provider、availability、input schema、permissions、audit 和 failure behavior
- **AND** SHALL 验证每个工具条目的 `status`、`availability`、`description`、输入 / 输出 schema、audit 字段和 failure behavior 的基础类型与必需字段
- **AND** SHALL 验证输出 schema 保持 `success:boolean`、`output:string`、`contentItems:array` 的稳定外形
- **AND** SHALL 验证稳定错误码非空、不重复，并与对应运行时执行器中的错误码保持同步
- **AND** SHALL 验证具备 Linear GraphQL mutation 能力的工具显式声明 `external_write_allowed=true`

#### Scenario: 生成 Harness 指标报告

- **WHEN** 操作者运行 `scripts/harness/generate-harness-metrics-report.sh`
- **THEN** 脚本 SHALL 从低敏 eval summary 和 agent run summary 聚合指标
- **AND** SHALL 递归发现 memory eval suite 下的 case summary 与 case agent run summary
- **AND** SHALL 将 suite 自身作为独立 `eval_suites` 指标统计，避免把 suite 误算为单个 eval case
- **AND** SHALL 按稳定失败类别聚合失败样例，避免只按完整错误文本统计
- **AND** SHALL 只把 `dispatch_kind=retry` 的失败重试计入 agent run 重试率，不把成功后的 continuation 调度计为重试
- **AND** SHALL 按外部副作用事件的 `type`、`status`、`type:status` 和稳定 `error_code` 聚合外部写操作，不聚合完整错误消息
- **AND** SHALL 从 agent run summary 的低敏 `environment` 字段聚合 Java major、OS name、OS arch 和 Spring profile 分布
- **AND** SHALL 从 agent run summary 的低敏 `permissions` 字段聚合权限姿态，包括远程 worker、网络访问、danger-full-access 许可、approval policy、thread sandbox、turn sandbox type 和可写 root 数量分布
- **AND** SHALL 支持从 `summary_type=harness_delivery_signal` 的低敏 JSON 聚合 PR 创建、合并、回滚、merge time 和 review finding 计数
- **AND** SHALL 支持从 deploy / rollback drill 报告中的 `summary_type=deploy_drill_report` JSON 摘要聚合演练数量、服务、状态、smoke 和失败阶段分布
- **AND** SHALL 支持从 `artifacts/agent-eval-drafts/*.yml` 聚合 eval dataset 草稿 backlog，包括草稿总数、人工复核要求、失败分类和风险等级分布
- **AND** SHALL 输出可读 Markdown 与机器可读 JSON
- **AND** `scripts/harness/check-harness-metrics-report.sh` SHALL 使用临时 fixture 验证生成器结构，不依赖本机真实 artifacts

#### Scenario: CI 生成并上传 Harness 指标快照

- **WHEN** GitHub Actions 执行 verify 或 governance workflow
- **THEN** workflow SHALL 先运行 `scripts/harness/collect-github-delivery-signal.sh`
- **AND** SHALL 再运行 `scripts/harness/generate-ci-harness-metrics.sh`
- **AND** SHALL 将 `artifacts/harness-metrics/` 作为 GitHub Actions artifact 上传
- **AND** 该脚本 SHALL 在没有本机 eval / run artifacts 时仍生成 `latest.json` 与 `latest.md`
- **AND** 该脚本 SHALL 支持从 `artifacts/harness-delivery-signals/` 读取低敏 delivery signal artifacts
- **AND** 该脚本 SHALL 支持从 `docs/reports/deploy-drills/` 读取低敏 deploy drill report 摘要
- **AND** `scripts/harness/check-harness-metrics-report.sh` SHALL 使用临时 fixture 验证 CI 包装脚本输出

#### Scenario: 检查部署与回滚演练报告摘要

- **WHEN** 操作者运行 `scripts/harness/check-deploy-drill-reports.sh`
- **THEN** 脚本 SHALL 校验每份 deploy / rollback drill 报告包含 `## 指标摘要`
- **AND** SHALL 校验该章节包含 `summary_type=deploy_drill_report` 的 JSON 摘要
- **AND** SHALL 校验摘要只使用低敏字段，包括 kind、service、services、status、smoke、rollback 和 failure_stage

#### Scenario: 检查 Harness 文档证据 freshness

- **WHEN** 操作者运行 `scripts/harness/check-doc-freshness.sh`
- **THEN** 脚本 SHALL 继续验证核心文档头部 `Status`、`Last Reviewed` 与 `Review Cadence`
- **AND** SHALL 对关键 Harness / Symphony 报告验证 evidence markers，包括可复跑验证入口、低敏 artifact 来源和敏感原文排除边界
- **AND** SHALL 防止只更新审阅日期但缺少运行证据或验证入口的文档被视为新鲜

#### Scenario: 从 GitHub Actions event 采集低敏 delivery signal

- **WHEN** 操作者运行 `scripts/harness/collect-github-delivery-signal.sh`
- **THEN** 脚本 SHALL 读取传入 event JSON 或 `GITHUB_EVENT_PATH`
- **AND** SHALL 输出 `summary_type=harness_delivery_signal` 的 JSON
- **AND** SHALL 只输出 PR 创建、合并、回滚、merge time 和 review finding 计数 / 分类
- **AND** SHALL 不输出 PR 正文、review 评论正文、提交消息、token 或外部 API 响应
- **AND** `scripts/harness/check-harness-metrics-report.sh` SHALL 使用临时 GitHub event fixture 验证采集输出

#### Scenario: 检查 agent run 摘要脱敏

- **WHEN** 操作者运行 `scripts/harness/check-agent-run-summaries.sh`
- **THEN** 脚本 SHALL 扫描 `docs/reports/agent-runs/` 中准备沉淀的摘要
- **AND** SHALL 支持传入一个或多个文件 / 目录递归扫描本地低敏 artifact summary
- **AND** SHALL 对 `summary_type=symphony_agent_run` 的 JSON 摘要执行轻量结构校验，至少覆盖 issue、attempt、workspace、codex usage、permissions、environment、retry 和 external effects
- **AND** SHALL 要求 `workspace.path`、`permissions.writable_roots`、`permissions.allowed_writable_roots` 与 `turn_sandbox_policy.writableRoots` 只保存低敏相对引用，不能保存本机绝对路径或 `..` 路径片段
- **AND** SHALL 在发现疑似 Bearer token、JWT、云访问密钥、明文密码字段或带密码连接串时失败
- **AND** SHALL 被 `scripts/harness/run-governance-checks.sh` 纳入周期性治理入口

#### Scenario: Symphony memory eval 生成 summary 后执行脱敏检查

- **WHEN** `scripts/e2e/run-symphony-agent-eval.sh` 完成单个 memory eval 或 memory eval suite
- **THEN** 脚本 SHALL 对本次 artifact 下的 agent run summary 执行 `scripts/harness/check-agent-run-summaries.sh`
- **AND** SHALL 在发现疑似敏感内容时使 eval 失败，阻止该 summary 进入后续指标聚合或人工沉淀流程

#### Scenario: 从失败 agent run 生成 eval dataset 草稿

- **WHEN** 操作者运行 `scripts/harness/generate-agent-eval-drafts.sh`
- **THEN** 脚本 SHALL 从低敏 `summary_type=symphony_agent_run` JSON 中筛选 failed run
- **AND** SHALL 只使用 run id、失败分类、事件计数、token 合计、权限摘要和外部副作用稳定错误码生成 memory eval dataset 草稿
- **AND** SHALL 默认将草稿写入不提交的 `artifacts/agent-eval-drafts/`
- **AND** SHALL 在草稿中标记人工复核要求，MUST NOT 自动把失败 run 直接写入正式 `docs/agent-evals/datasets/`

#### Scenario: 检查正式 eval dataset 不含草稿元信息

- **WHEN** 操作者运行 `scripts/harness/run-agent-evals.sh`
- **THEN** 脚本 SHALL 检查 `docs/agent-evals/datasets/*.yml` 不包含 `manual_review_required`、`source_summary`、`source_run_id` 等草稿专用字段
- **AND** SHALL 拒绝仍包含生成器占位文案的 dataset，避免未复核草稿直接进入正式回归集
- **AND** `scripts/harness/check-agentic-harness-assets.sh` SHALL 使用临时 fixture 验证该守门会拦截草稿专用元信息

#### Scenario: 检查正式 eval dataset 可稳定寻址

- **WHEN** 操作者运行 `scripts/harness/run-agent-evals.sh`
- **THEN** 脚本 SHALL 检查每个 `docs/agent-evals/datasets/*.yml` 的 `id` 与文件名去掉 `.yml` 后一致
- **AND** SHALL 检查 dataset `id` 在正式 dataset 目录内唯一
- **AND** `scripts/harness/check-agentic-harness-assets.sh` SHALL 使用临时 fixture 验证 id / 文件名不一致会被拒绝

#### Scenario: 执行 Symphony memory agent eval

- **WHEN** 操作者运行 `scripts/e2e/run-symphony-agent-eval.sh`
- **THEN** 脚本 SHALL 使用 memory tracker 和可控 fake Codex app-server 启动本地 Symphony 编排链路
- **AND** SHALL 验证 workspace 产物、运行历史、Codex 事件和低敏 agent run summary
- **AND** SHALL 支持通过 dataset 断言低敏 run summary 中的外部副作用审计事件
- **AND** SHALL 支持通过 dataset 断言 `networkAccess=true` 且配置 `permissions.network_access_reason` 的成功路径进入低敏权限摘要
- **AND** SHALL 支持通过 dataset 断言 `thread_sandbox=danger-full-access` 且配置 `permissions.allow_danger_full_access=true` 的成功路径进入低敏权限摘要
- **AND** SHALL 支持通过 dataset 断言额外 `writableRoots` 被 `permissions.allowed_writable_roots` 显式允许后的成功路径进入低敏权限摘要
- **AND** SHALL 在生成本次 `harness-metrics/latest.json` 后校验 `agent_runs.permission_posture` 与本次低敏 agent run summary 中的权限姿态一致
- **AND** SHALL 将低敏评测结果写入 `artifacts/agent-evals/`

#### Scenario: 执行 Symphony memory eval suite

- **WHEN** 操作者运行 `scripts/e2e/run-symphony-agent-eval.sh all`
- **THEN** 脚本 SHALL 自动发现 `docs/agent-evals/datasets/*.yml`
- **AND** SHALL 复用同一个 Symphony start jar 顺序运行 memory dataset
- **AND** SHALL 为每个 dataset 保留单次 eval summary 和日志
- **AND** SHALL 输出 suite 级 `summary.json` 与 `harness-metrics/latest.*`
- **AND** SHALL 在任一 dataset 未通过时以非零状态退出

#### Scenario: 执行模糊需求受理 memory eval 样例

- **WHEN** memory eval dataset 声明模糊需求需要先进入需求受理流程
- **THEN** `scripts/e2e/run-symphony-agent-eval.sh` SHALL 验证 Codex turn prompt 包含 `artemis-symphony/prompts/agent-requirement-intake.md`
- **AND** SHALL 验证 prompt 包含原始模糊需求语境
- **AND** SHALL 验证 workspace 中写入低敏需求受理模板产物
- **AND** SHALL 验证运行历史和低敏 run summary 记录 completed 状态

#### Scenario: 执行业务需求 Feature Spec memory eval 样例

- **WHEN** memory eval dataset 声明业务需求涉及 API、权限或跨模块协作
- **THEN** `scripts/e2e/run-symphony-agent-eval.sh` SHALL 验证 Codex turn prompt 包含 Spec-driven delivery guidance
- **AND** SHALL 验证 prompt 包含 `docs/feature-specs/README.md` 和 `artemis-symphony/prompts/spec-driven-delivery.md`
- **AND** SHALL 验证 prompt 包含原始业务需求语境
- **AND** SHALL 验证 workspace 中写入低敏 Feature Spec 模板产物
- **AND** SHALL 验证运行历史和低敏 run summary 记录 completed 状态

#### Scenario: 执行复杂任务执行计划 memory eval 样例

- **WHEN** memory eval dataset 声明任务需要跨模块或分阶段迁移
- **THEN** `scripts/e2e/run-symphony-agent-eval.sh` SHALL 验证 Codex turn prompt 包含 `docs/exec-plans/templates/execution-plan-template.md`
- **AND** SHALL 验证 prompt 包含复杂任务建立执行计划的规则
- **AND** SHALL 验证 prompt 包含原始迁移任务语境
- **AND** SHALL 验证 workspace 中写入低敏执行计划模板产物
- **AND** SHALL 验证运行历史和低敏 run summary 记录 completed 状态

#### Scenario: 执行稳定规则变更 OpenSpec memory eval 样例

- **WHEN** memory eval dataset 声明任务会改变质量门、契约治理或默认 agent workflow 规则
- **THEN** `scripts/e2e/run-symphony-agent-eval.sh` SHALL 验证 Codex turn prompt 包含稳定规则变更需要 `openspec/changes/` 的分流规则
- **AND** SHALL 验证 prompt 包含原始 workflow 规则变更语境
- **AND** SHALL 验证 workspace 中写入低敏 OpenSpec proposal 产物
- **AND** SHALL 验证运行历史和低敏 run summary 记录 completed 状态

#### Scenario: 执行高风险 adversarial review memory eval 样例

- **WHEN** memory eval dataset 声明高风险权限边界变更需要 adversarial review
- **THEN** `scripts/e2e/run-symphony-agent-eval.sh` SHALL 验证 Codex turn prompt 包含 Spec-driven delivery guidance
- **AND** SHALL 验证 prompt 包含安全审查清单、权限 runbook、adversarial review prompt / skill 和 Symphony tool registry
- **AND** SHALL 验证 prompt 包含原始权限边界变更语境
- **AND** SHALL 验证 workspace 中写入低敏 review guardrail 产物
- **AND** SHALL 验证运行历史和低敏 run summary 记录 completed 状态

#### Scenario: 执行 Linear 评论写回 memory eval 样例

- **WHEN** memory eval dataset 声明启用 Linear 评论写回报告
- **THEN** `scripts/e2e/run-symphony-agent-eval.sh` SHALL 在临时 workflow 中开启 `reporting.linear_comments.enabled`
- **AND** SHALL 通过 memory tracker comment 写回路径验证该能力不依赖真实 Linear 凭证
- **AND** SHALL 验证低敏 run summary 的 `external_effects.events` 包含 `linear_comment:succeeded`
- **AND** SHALL 验证运行历史和低敏 run summary 记录 completed 状态

#### Scenario: 执行 Linear 评论写回失败审计 memory eval 样例

- **WHEN** memory eval dataset 声明 Linear 评论写回应失败但 worker attempt 应完成
- **THEN** `scripts/e2e/run-symphony-agent-eval.sh` SHALL 验证 workspace 成功 marker 已被写入
- **AND** SHALL 验证低敏 run summary 的 `external_effects.events` 包含 `linear_comment:failed`
- **AND** SHALL 支持通过 dataset 断言该外部副作用事件的稳定错误码
- **AND** SHALL 验证运行历史和低敏 run summary 仍记录 completed 状态

#### Scenario: 执行权限预检失败 memory eval 样例集

- **WHEN** memory eval dataset 声明权限预检应失败
- **THEN** `scripts/e2e/run-symphony-agent-eval.sh` SHALL 验证 Codex app-server 未启动
- **AND** SHALL 验证低敏 run summary、运行历史和指标中记录 failed 状态与失败原因
- **AND** SHALL 验证历史事件中不存在 Codex turn 或 token 事件
- **AND** SHOULD 至少覆盖网络访问缺少理由和 writable root 越过 issue workspace 两类失败样例

#### Scenario: 执行 before_run hook 失败 memory eval 样例

- **WHEN** memory eval dataset 声明 before_run hook 应失败
- **THEN** `scripts/e2e/run-symphony-agent-eval.sh` SHALL 验证 Codex app-server 未启动
- **AND** SHALL 验证低敏 run summary、运行历史和指标中记录 failed 状态与 hook 失败原因
- **AND** SHALL 验证历史事件包含 `run_started` 和 `run_failed`
- **AND** SHALL 验证历史事件中不存在 session、turn 或 token usage 事件
- **AND** SHALL 验证 workspace 成功 marker 未被写入

#### Scenario: 执行 after_create hook 失败 memory eval 样例

- **WHEN** memory eval dataset 声明 after_create hook 应失败
- **THEN** `scripts/e2e/run-symphony-agent-eval.sh` SHALL 验证 issue workspace 已创建但 Codex app-server 未启动
- **AND** SHALL 验证低敏 run summary、运行历史和指标中记录 failed 状态与 hook 失败原因
- **AND** SHALL 验证历史事件包含 `run_started` 和 `run_failed`
- **AND** SHALL 验证历史事件中不存在 session、turn 或 token usage 事件
- **AND** SHALL 验证 workspace 成功 marker 未被写入

#### Scenario: 执行 Codex turn 失败 memory eval 样例

- **WHEN** memory eval dataset 声明 Codex turn 应失败
- **THEN** `scripts/e2e/run-symphony-agent-eval.sh` SHALL 验证 Codex app-server 已启动
- **AND** SHALL 验证低敏 run summary、运行历史和指标中记录 failed 状态与失败原因
- **AND** SHALL 验证历史事件包含 `session_started`、token usage 事件和 `turn_failed`
- **AND** SHALL 验证 workspace 成功 marker 未被写入

#### Scenario: 执行 Codex turn 取消 memory eval 样例

- **WHEN** memory eval dataset 声明 Codex turn 应取消
- **THEN** `scripts/e2e/run-symphony-agent-eval.sh` SHALL 验证 Codex app-server 已启动
- **AND** SHALL 验证低敏 run summary、运行历史和指标中记录 failed 状态与失败原因
- **AND** SHALL 验证历史事件包含 `session_started`、token usage 事件和 `turn_cancelled`
- **AND** SHALL 验证 workspace 成功 marker 未被写入

#### Scenario: 执行 Codex turn 超时 memory eval 样例

- **WHEN** memory eval dataset 声明 Codex turn 应超时
- **THEN** `scripts/e2e/run-symphony-agent-eval.sh` SHALL 验证 Codex app-server 已启动
- **AND** SHALL 验证低敏 run summary、运行历史和指标中记录 failed 状态与超时失败原因
- **AND** SHALL 验证历史事件包含 `session_started`、token usage 事件和 `turn_ended_with_error`
- **AND** SHALL 验证历史事件不包含 `turn_completed`、`turn_failed` 或 `turn_cancelled`
- **AND** SHALL 验证 workspace 成功 marker 未被写入

#### Scenario: 执行 Codex turn 响应超时 memory eval 样例

- **WHEN** memory eval dataset 声明 Codex turn/start 响应应超时
- **THEN** `scripts/e2e/run-symphony-agent-eval.sh` SHALL 验证 Codex app-server 已启动
- **AND** SHALL 验证低敏 run summary、运行历史和指标中记录 failed 状态与响应超时失败原因
- **AND** SHALL 验证历史事件包含 `session_started`、`turn_ended_with_error` 和 `run_failed`
- **AND** SHALL 验证历史事件不包含 token usage、`turn_completed`、`turn_failed` 或 `turn_cancelled`
- **AND** SHALL 验证 workspace 成功 marker 未被写入

#### Scenario: 执行 Codex 审批请求失败 memory eval 样例

- **WHEN** memory eval dataset 声明 Codex turn 需要审批且 approval policy 不允许自动批准
- **THEN** `scripts/e2e/run-symphony-agent-eval.sh` SHALL 验证 Codex app-server 已启动
- **AND** SHALL 验证低敏 run summary、运行历史和指标中记录 failed 状态与审批失败原因
- **AND** SHALL 验证历史事件包含 `session_started`、token usage 事件、`approval_required` 和 `turn_ended_with_error`
- **AND** SHALL 验证历史事件不包含 `approval_auto_approved`
- **AND** SHALL 验证 workspace 成功 marker 未被写入

#### Scenario: 执行 Codex 动态工具失败 memory eval 样例

- **WHEN** memory eval dataset 声明 Codex turn 的动态工具调用应失败
- **THEN** `scripts/e2e/run-symphony-agent-eval.sh` SHALL 验证 Codex app-server 已启动
- **AND** SHALL 验证低敏 run summary、运行历史和指标中记录 failed 状态与动态工具失败原因
- **AND** SHALL 验证历史事件包含 `session_started`、token usage 事件、`tool_call_failed` 和 `turn_ended_with_error`
- **AND** SHALL 验证 workspace 成功 marker 未被写入

#### Scenario: 执行 Codex 非交互用户输入阻断 memory eval 样例

- **WHEN** memory eval dataset 声明 Codex turn 需要无法自动回答的用户输入
- **THEN** `scripts/e2e/run-symphony-agent-eval.sh` SHALL 验证 Codex app-server 已启动
- **AND** SHALL 验证低敏 run summary、运行历史和指标中记录 failed 状态与用户输入阻断原因
- **AND** SHALL 验证历史事件包含 `session_started`、token usage 事件、`turn_input_required` 和 `turn_ended_with_error`
- **AND** SHALL 验证历史事件不包含 `tool_input_auto_answered`
- **AND** SHALL 验证 workspace 成功 marker 未被写入

#### Scenario: 执行 Codex malformed stdout memory eval 样例

- **WHEN** memory eval dataset 声明 Codex turn 会输出 malformed stdout 后正常完成
- **THEN** `scripts/e2e/run-symphony-agent-eval.sh` SHALL 验证 Codex app-server 已启动
- **AND** SHALL 验证低敏 run summary、运行历史和指标中记录 completed 状态
- **AND** SHALL 验证历史事件包含 `malformed`、token usage 事件和 `turn_completed`
- **AND** SHALL 验证 workspace 成功 marker 已被写入

#### Scenario: 执行 Codex 启动失败 memory eval 样例

- **WHEN** memory eval dataset 声明 Codex app-server 启动握手应失败
- **THEN** `scripts/e2e/run-symphony-agent-eval.sh` SHALL 验证 Codex app-server 进程已启动
- **AND** SHALL 验证低敏 run summary、运行历史和指标中记录 failed 状态与启动失败原因
- **AND** SHALL 验证历史事件包含 `startup_failed` 和 `run_failed`
- **AND** SHALL 验证历史事件中不存在 session、turn 或 token usage 事件

### Requirement: 跨 Agent 工具入口必须指向同一事实源

当仓库为 Copilot、Claude、Gemini 等工具提供专用指令文件时，这些文件 SHALL 作为薄指针引用 `AGENTS.md`，MUST NOT 复制完整规则，避免多套 agent 指令漂移。

#### Scenario: 新增外部 agent 指令文件

- **WHEN** 仓库新增 `.github/copilot-instructions.md`、`CLAUDE.md` 或 `GEMINI.md`
- **THEN** 文件内容 SHALL 指向 `AGENTS.md`
- **AND** 具体 workflow、验证和 handoff 规则 SHALL 仍以 `AGENTS.md` 为准
