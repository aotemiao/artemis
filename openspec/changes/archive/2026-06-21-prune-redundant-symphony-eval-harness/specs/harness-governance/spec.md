## MODIFIED Requirements

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

本要求不再覆盖 executable memory agent eval dataset 与 agent eval fixture。Symphony 运行时行为（权限预检、Codex turn 各类结果、workspace hook 失败、run history、低敏 agent run summary 脱敏、Linear 评论写回、spec-driven prompt 注入、独立 adversarial review 调度）SHALL 由 `artemis-symphony` 下的 JUnit 测试与真实端到端 `scripts/e2e/run-symphony-live-e2e.sh` 覆盖，MUST NOT 依赖已删除的 shell 评测层（`scripts/e2e/run-symphony-agent-eval.sh`、`scripts/harness/run-agent-evals.sh`、`docs/agent-evals/`）。

#### Scenario: 新增 agentic 开发资产

- **WHEN** 仓库新增或修改 agentic 开发资产
- **THEN** `scripts/harness/check-agentic-harness-assets.sh` SHALL 校验关键入口文件存在与模板必备风险 / 验证小节

#### Scenario: Symphony 运行时行为由 JUnit 与真实 e2e 覆盖

- **WHEN** 修改 Symphony 编排、权限预检、Codex 客户端、workspace hook、run history 或 agent run summary
- **THEN** 对应行为 SHALL 由 `artemis-symphony` 下的 JUnit 测试覆盖（如 `PermissionPreflightTest`、`CodexAppServerClientTest`、`WorkspaceManagerTest`、`OrchestratorRunHistoryTest`、`AgentRunSummaryWriterTest`、`AgentRunnerTest`）
- **AND** 真实端到端路径 SHALL 由 `scripts/e2e/run-symphony-live-e2e.sh` 验证
