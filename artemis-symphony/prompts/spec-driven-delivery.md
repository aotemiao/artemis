# Spec Driven Delivery Prompt

适用场景：需求来自 PRD、issue 或口头描述，且需要先收敛业务规则、验收标准和验证映射。

## 输入

- 用户原始需求或 issue 描述
- 相关 `docs/feature-specs/`、`openspec/specs/`、`docs/runbooks/`
- 相关执行计划，如存在

## 输出

1. 判断是否需要 Feature Spec。
2. 若需要，创建或更新 `docs/feature-specs/active/YYYY-MM-DD-<topic>.md`。
3. 如果任务复杂，创建或更新 `docs/exec-plans/active/YYYY-MM-DD-<topic>.md`。
4. 在实现前明确每条验收标准的验证入口。
5. 最终 handoff 使用 `docs/patterns/agent-delivery-handoff.md`，说明验收映射和验证结果。

## 判定规则

- 涉及业务规则、数据模型、API、内部 RPC、权限或跨模块协作：需要 Feature Spec。
- 只修拼写、注释、轻量脚本或文档链接：通常不需要 Feature Spec。
- 改变长期工程规则：需要同步 OpenSpec。

## 最低检查

- Feature Spec 必须包含 `## 验收标准` 和 `## 验证映射`。
- 执行计划必须引用 Feature Spec，或说明为什么不需要。
- 验证入口必须是仓库内真实存在的测试、脚本、smoke 或明确人工验收。
