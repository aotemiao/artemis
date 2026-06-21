## Why

延续 Symphony 感知后的 harness 精简：(1) `openspec/specs/` 下 Symphony 运行时语义被拆成多个碎片 spec（result-type 10 行、structured-logging 15、workflow-reload 15、http-operations 19），与 `symphony-runtime-alignment` 同属运行时语义却分散维护；(2) `check-symphony-assets.sh`（310 行、依赖 python3）对 `tools/registry.json` 做的核心校验，本质是“JSON 文档契约 ↔ `LinearGraphqlDynamicToolExecutor` 错误码同步”，更适合作为该模块内的 JUnit 断言而非仓库级 shell。

## What Changes

- 将 4 个碎片 spec（`symphony-result-type`、`symphony-structured-logging`、`symphony-workflow-reload`、`symphony-http-operations`）合并进 `symphony-runtime-alignment`，并删除这 4 个能力目录（`openspec/specs/` 32 → 28）。
- 新增 `SymphonyToolRegistryTest`（artemis-symphony-orchestrator，JUnit + Jackson）：校验 `registry.json` 的 `registry_type`、唯一 `linear_graphql` 条目（provider/availability/output schema 稳定外形/`external_write_allowed=true`/非空唯一稳定错误码），并断言稳定错误码与运行时 `LinearGraphqlDynamicToolExecutor` 保持同步。
- 删除 `scripts/harness/check-symphony-assets.sh`；其资产存在校验并入精简的 `scripts/harness/check-agentic-harness-assets.sh`（新增 Symphony skills / prompts / tools 关键资产）。
- 从 `run-governance-checks.sh` 与 `.githooks/pre-commit` 移除 Symphony Assets 子检查；更新 spec、runbook、模板与文档中对 `check-symphony-assets.sh` 的引用为 JUnit / 精简检查。
- 保留 `scripts/harness/check-agent-run-summaries.sh`：它是对 `docs/reports/agent-runs/` 的 secret / 低敏扫描器（API key、JWT、密码 URI），具备独立安全价值。

## Capabilities

### Modified Capabilities

- `harness-governance`：Symphony 动态工具注册表的校验事实源从 `check-symphony-assets.sh` 收敛为 `SymphonyToolRegistryTest`（JUnit）+ `check-agentic-harness-assets.sh`（资产存在）。

## Impact

- 不改变 Symphony 运行时能力、`registry.json` 内容或内部契约；治理脚本再减少一个 python3 依赖项。
- 已用 `mvn test` 验证 `SymphonyToolRegistryTest` 通过；`check-agentic-harness-assets.sh` 实跑通过。
- `openspec/specs/` 能力数 32 → 28；harness 脚本数 21 → 19（累计两轮 28 → 19）。
