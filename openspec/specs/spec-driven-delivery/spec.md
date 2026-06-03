## ADDED Requirements

### Requirement: 业务需求必须先收敛为 Feature Spec

当任务来自 PRD、issue 或口头需求，且涉及业务规则、数据模型、API、内部契约或跨模块协作时，仓库 SHALL 使用 `docs/feature-specs/` 记录业务需求级 Spec。

#### Scenario: 需求验收标准不清晰

- **WHEN** 一个需求只有自然语言描述，且没有可测试验收标准
- **THEN** 实现前 SHALL 建立或更新 Feature Spec
- **AND** Feature Spec SHALL 包含用户故事、业务规则、验收标准和验证映射

### Requirement: Feature Spec 验收标准必须映射到验证入口

Feature Spec 中每条关键验收标准 SHALL 映射到至少一个验证入口，包括单元测试、集成测试、smoke、harness 脚本或明确的人工验收步骤。

#### Scenario: Feature Spec 缺少验证映射

- **WHEN** Feature Spec 包含 `AC-001` 验收标准
- **THEN** `## 验证映射` 中 SHALL 明确列出 `AC-001` 对应的验证入口和通过标准

#### Scenario: 验收标准编号未覆盖

- **WHEN** `## 验收标准` 中存在 `AC-001`、`AC-002`
- **THEN** `## 验证映射` SHALL 同时列出 `AC-001` 与 `AC-002`
- **AND** 每条映射 SHALL 包含非空验证入口与通过标准

### Requirement: Feature Spec 必须显式覆盖异常与工程风险

当 Feature Spec 涉及业务规则、数据模型、API、内部契约、权限或跨模块协作时，Feature Spec SHALL 显式记录异常场景与工程风险评估。风险评估至少覆盖：

- 权限 / 越权
- 幂等 / 重复提交
- 并发 / 锁
- 事务 / 一致性
- 异常码 / 错误响应
- SQL 性能
- 日志 / 可观测性
- 数据迁移 / 回滚

#### Scenario: 高风险业务需求缺少风险评估

- **WHEN** 一个 Feature Spec 涉及权限、写操作或跨模块行为
- **THEN** 该 Spec SHALL 在实现前说明相关风险是否涉及
- **AND** 对涉及的风险 SHALL 给出处理要求或验证方式

### Requirement: 执行计划必须引用需求与验证证据

当复杂任务基于 Feature Spec 推进时，执行计划 SHALL 记录关联 Feature Spec、任务拆解、每个任务的输入输出、验收映射和回滚策略。

#### Scenario: 多阶段业务能力交付

- **WHEN** 一个业务能力需要跨模块或分阶段实现
- **THEN** SHALL 在 `docs/exec-plans/active/` 建立执行计划
- **AND** 计划 SHALL 引用对应 Feature Spec 或说明不需要 Feature Spec 的原因

### Requirement: 复杂执行计划必须包含风险分类与验证分类

当执行计划用于复杂实现、跨模块改动或高风险改动时，计划 SHALL 显式记录风险分类和验证分类，帮助 agent 和 reviewer 判断是否需要安全审查、模块级验证、smoke、agent eval 或 full verify。

#### Scenario: 执行计划涉及权限或事务风险

- **WHEN** 执行计划标记权限、安全、幂等、并发、事务或 SQL 性能风险
- **THEN** 计划 SHALL 在验证分类中列出对应的测试、smoke、harness 脚本或人工验收
- **AND** 最终 handoff SHALL 引用这些验证证据

### Requirement: Agent 交付必须包含验收映射

Agent 完成需求交付时 SHALL 在 handoff 中说明关键验收标准如何被验证，并列出实际执行的验证命令。

#### Scenario: Agent 完成 Feature Spec 关联任务

- **WHEN** agent 完成一个关联 Feature Spec 的任务
- **THEN** 最终 handoff SHALL 包含验证命令、结果和验收映射
- **AND** 若存在未自动化验收，SHALL 标明人工验收范围与剩余风险

### Requirement: 高风险 Agent 交付必须包含安全审查结论

当 agent 交付涉及权限、幂等、锁、事务、异常处理、SQL 性能、日志或可观测性时，handoff SHALL 包含安全审查结论，并说明是否使用 adversarial review 资产。

#### Scenario: Agent 修改权限相关逻辑

- **WHEN** agent 修改认证、授权、网关、数据权限或内部接口访问逻辑
- **THEN** handoff SHALL 引用 `docs/patterns/security-review-checklist.md`
- **AND** SHOULD 使用 `artemis-symphony/skills/adversarial-review.md` 进行独立复核

### Requirement: Symphony 运行时必须显式承载 SDD 上下文

当 `WORKFLOW.md` 启用 `delivery.spec_driven.enabled` 时，Symphony SHALL 将 Spec-driven Delivery 的关键资产注入首轮 agent prompt，并在 `/api/v1/state` 暴露当前 delivery 配置快照。

#### Scenario: 首轮 agent prompt 缺少 SDD 上下文

- **WHEN** `delivery.spec_driven.enabled` 为 `true`
- **THEN** Symphony SHALL 在首轮 agent prompt 中包含 Feature Spec、验收映射、执行计划和 handoff 要求
- **AND** prompt SHALL 列出 `delivery.spec_driven.required_assets` 中的本地资产

#### Scenario: 运维侧无法判断 SDD 是否启用

- **WHEN** 请求 `GET /api/v1/state`
- **THEN** 响应 SHALL 包含 `delivery.spec_driven_enabled`
- **AND** 响应 SHALL 包含当前 `delivery.required_assets`
