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
- 已全部完成的 active change 必须在 delta 合入 `openspec/specs/` 后从 `openspec/changes/` 删除
- 仓库不持久保留 `openspec/changes/archive/`；变更过程历史由 Git commit 记录

#### Scenario: 已完成 OpenSpec change 留在 active 区

- **WHEN** `openspec/changes/<name>/tasks.md` 中所有 checkbox 任务均已勾选
- **THEN** OpenSpec change 状态检查 SHALL 失败
- **AND** SHALL 提示在规则合入 `openspec/specs/` 后删除该 change 目录

#### Scenario: active OpenSpec change 缺少结构文件

- **WHEN** `openspec/changes/<name>/` 缺少 `proposal.md` 或 `tasks.md`
- **THEN** OpenSpec change 状态检查 SHALL 失败并指出缺失文件

### Requirement: Agentic Harness 资产必须可验证

仓库 SHALL 为 agentic 开发优化资产提供专项守门，至少覆盖：

- agent 运行轨迹摘要规则
- agent run summary sensitive-content checker
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

#### Scenario: 检查 Symphony 动态工具注册表

- **WHEN** 修改 `artemis-symphony/tools/registry.json` 或运行时执行器 `LinearGraphqlDynamicToolExecutor`
- **THEN** `artemis-symphony` 下的 `SymphonyToolRegistryTest`（JUnit）SHALL 校验 registry 声明 `registry_type=symphony_tool_registry` 与唯一 `linear_graphql` 条目（provider、availability、output schema 稳定外形 `success:boolean`/`output:string`/`contentItems:array`、permissions `external_write_allowed=true`、非空且唯一的稳定错误码）
- **AND** SHALL 校验 registry 的稳定错误码与运行时执行器 `LinearGraphqlDynamicToolExecutor` 中的错误码保持同步
- **AND** `scripts/harness/check-agentic-harness-assets.sh` SHALL 校验 registry.json 与关键 skills / prompts 资产存在

#### Scenario: 检查部署与回滚演练报告摘要

- **WHEN** 操作者运行 `scripts/harness/check-deploy-drill-reports.sh`
- **THEN** 脚本 SHALL 校验每份 deploy / rollback drill 报告包含 `## 指标摘要`
- **AND** SHALL 校验该章节包含 `summary_type=deploy_drill_report` 的 JSON 摘要
- **AND** SHALL 校验摘要只使用低敏字段，包括 kind、service、services、status、smoke、rollback 和 failure_stage

#### Scenario: 检查 agent run 摘要脱敏

- **WHEN** 操作者运行 `scripts/harness/check-agent-run-summaries.sh`
- **THEN** 脚本 SHALL 扫描 `docs/reports/agent-runs/` 中准备沉淀的摘要
- **AND** SHALL 支持传入一个或多个文件 / 目录递归扫描本地低敏 artifact summary
- **AND** SHALL 同时扫描 `.md` 与 `.json` 摘要文件中的敏感内容
- **AND** JSON 摘要的结构契约 SHALL 由写入实现 `AgentRunSummaryWriter` 及其单测 `AgentRunSummaryWriterTest` 作为唯一事实源保证，MUST NOT 在治理脚本中用第二套 schema 重复校验
- **AND** SHALL 在发现疑似 Bearer token、JWT、云访问密钥、明文密码字段或带密码连接串时失败
- **AND** SHALL 被 `scripts/harness/run-governance-checks.sh` 纳入周期性治理入口

### Requirement: 跨 Agent 工具入口必须指向同一事实源

当仓库为 Copilot、Claude、Gemini 等工具提供专用指令文件时，这些文件 SHALL 作为薄指针引用 `AGENTS.md`，MUST NOT 复制完整规则，避免多套 agent 指令漂移。

#### Scenario: 新增外部 agent 指令文件

- **WHEN** 仓库新增 `.github/copilot-instructions.md`、`CLAUDE.md` 或 `GEMINI.md`
- **THEN** 文件内容 SHALL 指向 `AGENTS.md`
- **AND** 具体 workflow、验证和 handoff 规则 SHALL 仍以 `AGENTS.md` 为准
