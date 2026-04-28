## Why

当前仓库已经同时具备 `artemis-symphony`、Harness Engineering、OpenSpec 与执行计划目录，但对于“一个需求到底应该改哪里”仍缺少单页化、可复用的默认答案。结果是团队在提需求时容易先围绕工具名分流，而不是先判断“是不是改规则”“是不是缺验证入口”“是不是要补编排资产”。

如果不把这套分流逻辑明确沉淀，后续 agent 仍可能在 `prompt`、`runbook`、`spec` 与“纯代码改动”之间来回摇摆，增加沟通与返工成本。

## What Changes

- 新增仓库级工作流文档 `docs/harness-engineering/AGENT_DEVELOPMENT_WORKFLOW.md`，明确 `artemis-symphony`、Harness Engineering、OpenSpec 与执行计划的默认分工。
- 新增 OpenSpec 能力 `agent-development-workflow`，把需求分流、模板与默认回写规则固化为稳定约束。
- 新增 Symphony prompt 资产 `artemis-symphony/prompts/agent-requirement-intake.md`，让“先结构化需求再进入实现”成为可复用动作。
- 为本次规则变化补 OpenSpec change 目录，保留 proposal / design / tasks 轨迹。

## Capabilities

### New Capabilities

- `agent-development-workflow`：定义 agent 开发的分工模型、需求受理模板与按变化类型分流的默认规则。

### Modified Capabilities

<!-- 无：本次新增独立能力，不直接修改既有 capability 的 requirement。 -->

## Impact

- `docs/harness-engineering/` 新增一份面向人和 agent 共用的工作流说明
- `openspec/specs/` 新增一份稳定规范，同时在 `openspec/changes/` 保留变更轨迹
- `artemis-symphony/prompts/` 新增一个可直接复用的需求受理 prompt
- 不影响业务代码、运行时配置、数据库或服务契约
