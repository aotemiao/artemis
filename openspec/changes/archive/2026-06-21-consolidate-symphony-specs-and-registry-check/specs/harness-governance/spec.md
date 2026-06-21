## MODIFIED Requirements

### Requirement: Agentic Harness 资产必须可验证

`scripts/harness/check-agentic-harness-assets.sh` SHALL 额外校验关键 Symphony agent 资产存在：`WORKFLOW.md.example`、`tools/README.md`、`tools/registry.json`、`skills/spec-driven-delivery.md`、`skills/adversarial-review.md`、`prompts/spec-driven-delivery.md`、`prompts/adversarial-review.md`。Symphony 动态工具注册表的结构契约与“文档↔运行时错误码同步”SHALL 由 `artemis-symphony` 下的 `SymphonyToolRegistryTest`（JUnit）校验，不再由独立 shell 脚本 `check-symphony-assets.sh` 承担。

#### Scenario: 检查 Symphony 动态工具注册表

- **WHEN** 修改 `artemis-symphony/tools/registry.json` 或运行时执行器 `LinearGraphqlDynamicToolExecutor`
- **THEN** `SymphonyToolRegistryTest` SHALL 校验 registry 声明 `registry_type=symphony_tool_registry` 与唯一 `linear_graphql` 条目（provider、availability、output schema 稳定外形、permissions `external_write_allowed=true`、非空且唯一的稳定错误码）
- **AND** SHALL 校验 registry 的稳定错误码与运行时执行器中的错误码保持同步
- **AND** `scripts/harness/check-agentic-harness-assets.sh` SHALL 校验 registry.json 与关键 skills / prompts 资产存在
