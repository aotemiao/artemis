## Why

把 Symphony 模块纳入视野后可以看清：`docs/agent-evals/` 的 26 个 memory eval 数据集 + `scripts/e2e/run-symphony-agent-eval.sh`（1,129 行）+ `scripts/harness/run-agent-evals.sh`（317 行）用“假 Codex + 起 JAR + 真 HTTP + python3”的全栈方式，重复测试了 `artemis-symphony` 已有 **22 个 JUnit 测试（约 4,184 行）+ `SymphonyLiveE2EIT` 真实端到端**已覆盖的运行时行为。数据集与 JUnit 方法逐一对应（权限预检↔`PermissionPreflightTest`，Codex turn 各类结果↔`CodexAppServerClientTest`，hook↔`WorkspaceManagerTest`，run history↔`OrchestratorRunHistoryTest`/`SqliteRunHistoryRepositoryTest`，summary↔`AgentRunSummaryWriterTest`，Linear 评论↔`OrchestratorLinearCommentTest`，spec-driven prompt 注入↔`AgentRunnerTest`/`ServiceConfigTest`）。这个 shell 评测层更脆、更慢、强依赖 python3，是“运行时 harness 与静态脚手架重复”的典型。

## What Changes

- 删除 shell 评测层：`scripts/e2e/run-symphony-agent-eval.sh`、`scripts/harness/run-agent-evals.sh`、`docs/agent-evals/`（26 个 dataset + 2 个 fixture + README）。
- 在删除前逐一核对 JUnit 覆盖；对原本只在全栈层断言、JUnit 未直接覆盖的 hook 失败路径，补两个 `AgentRunnerTest` 单元测试：`runAttempt_whenBeforeRunHookFails_stopsBeforeCodex`、`runAttempt_whenWorkspaceCreationFails_stopsBeforeBeforeRunAndCodex`（Codex turn 超时 / malformed 由 `FailureCategoryClassifierTest` 的稳定分类 + `CodexAppServerClientTest` 的 turn 处理 + live-e2e 覆盖）。
- 从 `openspec/specs/harness-governance/spec.md` 移除全部 memory eval 执行/校验场景与“executable memory agent eval dataset / agent eval fixture”资产项。
- 更新 `AGENTS.md`、`README.md`、`docs/README.md`、`QUALITY_SCORE.md`、`docs/reports/agent-runs/README.md`、`docs/runbooks/RISK_BASED_VERIFICATION_RUNBOOK.md`、`docs/exec-plans/templates/execution-plan-template.md` 中指向已删评测层的引用，改为指向 `artemis-symphony` 的 JUnit 与 `scripts/e2e/run-symphony-live-e2e.sh`。

## Capabilities

### Modified Capabilities

- `harness-governance`：Symphony 运行时行为评测的事实源从 shell 评测层收敛为 `artemis-symphony` 的 JUnit 测试 + 真实 live-e2e；harness 资产校验不再覆盖 memory eval dataset / fixture 与其执行脚本。

## Impact

- 不改变 Symphony 运行时能力、内部 Dubbo 契约或对外 REST API；`scripts/e2e/run-symphony-live-e2e.sh` 真实端到端保留。
- 删除约 1,450 行 shell + 28 个 YAML；治理脚本对 python3 的依赖进一步减少。
- 测试覆盖不降：删除前已用 `mvn test` 验证新增 hook 失败测试通过，并逐一核对数据集场景已被 JUnit 覆盖。
- 历史 `docs/reports/ROADMAP.md`、`docs/governance/CHECKLIST.md` 中对已删评测层的描述作为带日期记录保留。
