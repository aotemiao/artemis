# Patterns And Reusable Assets

Status: maintained
Last Reviewed: 2026-06-02
Review Cadence: 90 days

本目录存放可复用的工程模式、模板和示例，目标是让 agent 不必每次从零推导常见做法。

## 内容边界

这里放“可以复用的砖块”，不放长期约束本身。

- 长期规则放 `../openspec/specs/`。
- 具体操作步骤放 `../runbooks/`。
- 一次复杂任务过程放 `../exec-plans/`。
- 业务需求 Spec 放 `../feature-specs/`。

## 当前资产

- `spec-to-validation-mapping.md`
  说明如何把 Feature Spec 的验收标准映射到测试、smoke、harness 脚本或人工验收。
- `agent-delivery-handoff.md`
  说明 agent 完成任务时应如何交付证据、验证结果和剩余风险。
- `security-review-checklist.md`
  说明高风险改动如何审查权限、幂等、并发、事务、异常、SQL、日志和可观测性。

新增资产时，优先选择能被多个任务复用的模式；只适用于单次任务的内容应放执行计划。
