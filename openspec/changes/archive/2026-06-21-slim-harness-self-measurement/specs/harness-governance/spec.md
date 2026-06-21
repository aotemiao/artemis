## MODIFIED Requirements

### Requirement: Agentic Harness 资产必须可验证

仓库 SHALL 为 agentic 开发优化资产提供专项守门，至少覆盖：

- agent eval fixture
- executable memory agent eval dataset
- agent 运行轨迹摘要规则
- agent run summary sensitive-content checker
- Symphony dynamic tool registry
- agent 权限策略 runbook
- threat model
- 风险分级验证 runbook
- 安全审查清单
- adversarial review prompt / skill
- 跨 agent 工具的薄指针文件

本要求不再覆盖 harness metrics report generator。以下场景随自度量子系统一并移除：生成 Harness 指标报告、CI 生成并上传 Harness 指标快照、检查 Harness 文档证据 freshness、从 GitHub Actions event 采集低敏 delivery signal、从失败 agent run 生成 eval dataset 草稿。`scripts/harness/check-agentic-harness-assets.sh` SHALL 简化为可移植 POSIX sh，只校验关键入口资产存在与模板必备小节，MUST NOT 钉死各文档逐字符串或逐 dataset 内容。

#### Scenario: 新增 agentic 开发资产

- **WHEN** 仓库新增或修改 agentic 开发资产
- **THEN** `scripts/harness/check-agentic-harness-assets.sh` SHALL 校验关键入口文件存在与模板必备风险 / 验证小节
- **AND** `scripts/harness/run-agent-evals.sh` SHALL 能验证 eval fixture 的基础结构和路径引用

#### Scenario: 执行 Symphony memory agent eval 不再耦合指标

- **WHEN** 操作者运行 `scripts/e2e/run-symphony-agent-eval.sh`
- **THEN** 脚本 SHALL 使用 memory tracker 和可控 fake Codex app-server 启动本地 Symphony 编排链路并验证 workspace 产物、运行历史、Codex 事件与低敏 agent run summary
- **AND** MUST NOT 依赖已删除的 harness metrics 生成器
