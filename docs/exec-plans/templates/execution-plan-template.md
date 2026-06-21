# <计划标题>

Status: draft
Last Reviewed: YYYY-MM-DD
Review Cadence: 90 days

## 背景

说明为什么要做这件事，以及当前仓库存在什么问题。

## 关联需求与规范

- Feature Spec：`docs/feature-specs/active/<date-topic>.md` 或说明无需 Feature Spec 的原因
- OpenSpec：`openspec/specs/<name>/spec.md` 或无
- Runbook / API / 其它资产：

## 目标

- 目标 1
- 目标 2

## 非目标

- 本计划不解决什么

## 范围

- 会改动哪些模块
- 不会改动哪些模块

## 风险

- 风险 1
- 风险 2

## 风险分类

| 风险项 | 是否涉及 | 方案检查点 | Reviewer 关注点 |
|--------|----------|------------|-----------------|
| 领域建模 | 是 / 否 | <聚合、值对象、领域服务边界> | <模型是否表达业务规则> |
| 权限 / 安全 | 是 / 否 | <鉴权、数据权限、内部接口阻断> | <是否存在越权或敏感信息泄露> |
| 幂等 / 并发 / 锁 | 是 / 否 | <幂等键、唯一约束、锁策略> | <重复提交、并发写入、竞态条件> |
| 事务 / 数据一致性 | 是 / 否 | <事务边界、跨服务一致性、补偿> | <失败时是否回滚或可恢复> |
| SQL 性能 | 是 / 否 | <索引、分页、查询范围> | <N+1、全表扫描、排序分页风险> |
| 日志 / 可观测性 | 是 / 否 | <trace、审计、指标、告警> | <问题是否可定位、可审计> |

## 任务拆解

| 编号 | 任务 | 输入 | 输出 | 验收标准 |
|------|------|------|------|----------|
| T-001 | <任务> | <依赖文档、代码或数据> | <交付物> | <对应 AC 或检查> |

## 分步执行

1. 第一步
2. 第二步
3. 第三步

## 验收映射

| 验收编号 | 来源 | 验证入口 | 通过标准 |
|----------|------|----------|----------|
| AC-001 | Feature Spec / 本计划 | `<command-or-test>` | <期望输出或断言> |

## 验证计划

- 要跑哪些脚本
- 要看哪些测试或端点
- 通过标准是什么

## 验证分类

| 变更类型 | 最小验证入口 | 是否需要 |
|----------|--------------|----------|
| 文档 / 规范 | `scripts/harness/run-governance-checks.sh` | 是 / 否 |
| Java / POM | `scripts/harness/verify-changed.sh working-tree` | 是 / 否 |
| API / 契约 | `scripts/harness/check-api-doc-sync.sh`、`scripts/harness/check-client-contracts.sh` | 是 / 否 |
| 数据迁移 | 模块级 migration 测试或 infra 集成测试 | 是 / 否 |
| 服务行为 | `scripts/smoke/<service>.sh` 或 runbook 人工验收 | 是 / 否 |
| agent 编排 | `scripts/harness/check-agentic-harness-assets.sh`、`SymphonyToolRegistryTest` | 是 / 否 |

## 回滚策略

- 如果实现失败，如何撤回代码、配置或数据变更
- 如果部署失败，使用哪个 runbook 或脚本恢复

## 决策记录

- `YYYY-MM-DD`：记录关键决策与原因

## 遗留问题

- 如有未完成项，在这里说明
