# Adversarial Review Skill

用于对 agent 或开发者交付进行独立复核，重点寻找安全、事务、权限、数据一致性、SQL 性能、日志和可观测性问题。

## 适用场景

- 任务涉及权限、认证、网关、内部接口或数据权限。
- 任务涉及写操作、批量操作、删除、状态流转、跨服务调用或缓存。
- 任务涉及 schema、SQL、分页、导出、审计日志或可观测性。
- 任务是复杂执行计划或 Feature Spec 关联交付。

## 必读资产

1. `docs/agent-workflow/AGENT_REVIEW_LOOP.md`
2. `docs/patterns/security-review-checklist.md`
3. `docs/patterns/agent-delivery-handoff.md`
4. `artemis-symphony/prompts/adversarial-review.md`
5. 本次任务关联的 Feature Spec、执行计划和 OpenSpec。

## 审查步骤

1. 先确认需求边界：目标、非目标、验收标准和验证映射是否一致。
2. 再看方案边界：领域模型、事务边界、跨服务 client 契约是否符合 OpenSpec。
3. 然后审高风险项：权限、幂等、锁、事务、异常码、SQL、日志、审计和可观测性。
4. 最后审证据：测试、smoke、harness、人工验收和 handoff 是否能证明验收标准。

## 输出要求

- 发现问题时，按严重程度排序，给出文件或资产位置。
- 找不到问题时，明确说明仍有哪些风险未被自动化验证覆盖。
- 不要只输出改动摘要；审查结论必须优先于总结。
