# Agent Delivery Handoff

Status: maintained
Last Reviewed: 2026-06-01
Review Cadence: 90 days

本模式用于统一 agent 完成任务后的交付说明，确保 reviewer 能快速判断需求、实现和验证是否闭环。

## 必须交代

- 需求来源：关联 Feature Spec、issue 或执行计划。
- 改动范围：代码、测试、文档、脚本、OpenSpec、Symphony 资产。
- 验证证据：实际执行过的命令和结果。
- 验收映射：每条关键验收标准对应什么验证入口。
- 剩余风险：未自动化验证、外部依赖、需要人工确认的部分。

## 推荐格式

```md
## Summary

- 改动 1
- 改动 2

## Validation

- `scripts/harness/verify-changed.sh working-tree`
- `mvn test -pl <module> -am`

## Acceptance Mapping

| 验收编号 | 验证入口 | 结果 |
|----------|----------|------|
| AC-001 | `<command>` | 通过 |

## Risks

- 风险或无
```
