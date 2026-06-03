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

### Requirement: Agentic Harness 资产必须可验证

仓库 SHALL 为 agentic 开发优化资产提供专项守门，至少覆盖：

- agent eval fixture
- agent 运行轨迹摘要规则
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

### Requirement: 跨 Agent 工具入口必须指向同一事实源

当仓库为 Copilot、Claude、Gemini 等工具提供专用指令文件时，这些文件 SHALL 作为薄指针引用 `AGENTS.md`，MUST NOT 复制完整规则，避免多套 agent 指令漂移。

#### Scenario: 新增外部 agent 指令文件

- **WHEN** 仓库新增 `.github/copilot-instructions.md`、`CLAUDE.md` 或 `GEMINI.md`
- **THEN** 文件内容 SHALL 指向 `AGENTS.md`
- **AND** 具体 workflow、验证和 handoff 规则 SHALL 仍以 `AGENTS.md` 为准
