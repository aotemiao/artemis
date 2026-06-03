# Agent Evals

Status: maintained
Last Reviewed: 2026-06-02
Review Cadence: 90 days

本目录存放 agentic 开发流程的评测资产，用于验证 agent 是否按仓库规则工作，而不只验证代码是否能编译。

## 评测目标

当前评测先覆盖流程合规性：

- 模糊需求是否先进入需求受理模板。
- 业务需求是否要求 Feature Spec 和验收映射。
- 复杂任务是否要求执行计划。
- 稳定规则变化是否要求 OpenSpec。
- 高风险改动是否触发安全审查和权限策略。
- 最终 handoff 是否包含验证证据、验收映射和剩余风险。

## 目录约定

- `fixtures/`
  评测用例。每个文件描述一个 issue 风格任务、期望 agent 使用的资产和必须触发的验证入口。
- `README.md`
  评测目的、目录结构和运行方式。

## 运行方式

```bash
scripts/harness/run-agent-evals.sh
```

当前脚本执行结构化静态评测：它不会调用真实模型，而是检查 fixture 是否完整、期望资产是否存在、期望验证入口是否真实可执行。后续可以在 Symphony memory tracker 上扩展真实 agent e2e eval。

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
