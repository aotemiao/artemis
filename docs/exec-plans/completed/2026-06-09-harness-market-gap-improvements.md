# Harness Market Gap Improvements

Status: completed
Last Reviewed: 2026-06-09
Review Cadence: 90 days

## 背景

`2026-06-09-harness-engineering-market-gap-research.md` 指出 Artemis 的入口、规范和脚本已经较成熟，但真实运行闭环仍需要更强的可采集、可回放、可失败能力。0-7 天优先项中最适合当前工作树小步落地的是 API 文档自动发现、OpenSpec active 状态守门、低敏 agent run 摘要样例和质量信号更新。

当前工作树已有 Symphony 运行历史相关改动，本计划不触碰这些业务 / 编排实现文件，避免混入他人未提交工作。

## 关联需求与规范

- Feature Spec：无需新增。本次不改变业务行为、数据模型或对外业务能力。
- OpenSpec：
  - `openspec/specs/contract-doc-guardrails/spec.md`
  - `openspec/specs/harness-governance/spec.md`
- Runbook / API / 其它资产：
  - `scripts/harness/check-api-doc-sync.sh`
  - `scripts/harness/run-governance-checks.sh`
  - `docs/reports/agent-runs/README.md`
  - `QUALITY_SCORE.md`

## 目标

- 让 `artemis-system` REST Controller 覆盖范围由手写清单升级为自动发现，新增 Controller 缺少 API 文档时检查失败。
- 增加 OpenSpec active change 状态检查，避免已完成 change 长期停留在 active 区误导 agent。
- 归档已完成的 `remove-artemis-visual` OpenSpec change。
- 增加一个低敏 agent run 摘要样例，明确报告建议的复盘格式。
- 更新质量信号，把 agent eval / observability 下一步具体化为脚本和产物方向。

## 非目标

- 不实现真实 Symphony memory / Linear e2e eval。
- 不改 Symphony attempt 持久化、权限 preflight 或 run trace 运行时代码。
- 不补全 OpenAPI 字段级契约或生成物快照。
- 不处理当前工作树中既有的 Symphony 未提交改动。

## 范围

- 修改 harness 脚本、治理文档、OpenSpec 稳定规范和执行计划。
- 不修改 Java 业务代码、POM 或 Nacos / Docker 运行配置。

## 风险

- API 文档自动映射规则过宽可能误判 Controller 到文档的关系。
- OpenSpec active 状态检查过严可能影响正在草拟但尚未补齐结构的 change。
- 低敏 run 摘要样例不能被误读为真实自动 trace 已经完成。

## 风险分类

| 风险项 | 是否涉及 | 方案检查点 | Reviewer 关注点 |
|--------|----------|------------|-----------------|
| 领域建模 | 否 | 不改业务模型 | 无 |
| 权限 / 安全 | 是 | run 摘要样例不得包含 token、密钥、连接串或外部响应全文 | 是否扩大敏感信息提交风险 |
| 幂等 / 并发 / 锁 | 否 | 不改运行时代码 | 无 |
| 事务 / 数据一致性 | 否 | 不改数据写入 | 无 |
| SQL 性能 | 否 | 不改查询 | 无 |
| 日志 / 可观测性 | 是 | 明确当前只是低敏复盘样例，后续才接入自动 trace | 是否能指导后续真实 run summary |

## 任务拆解

| 编号 | 任务 | 输入 | 输出 | 验收标准 |
|------|------|------|------|----------|
| T-001 | API 文档同步自动发现 | 报告 P0-4、现有 Controller 与 API 文档 | `check-api-doc-sync.sh` 自动扫描 system Controller | tenant、tenant package、oper log 被纳入检查 |
| T-002 | OpenSpec 状态守门 | 报告 P0-5、active change 状态 | `check-openspec-change-state.sh` 并接入治理 | 已完成 active change 会失败并提示归档 |
| T-003 | OpenSpec change 归档 | `remove-artemis-visual` | archive 目录归档 | active 区不再包含已完成 change |
| T-004 | 低敏 run 摘要样例 | agent run 摘要规则 | `docs/reports/agent-runs/2026-06-09-harness-market-gap-improvements.md` | 样例包含资产、验证、风险、外部副作用 |
| T-005 | 规范与质量信号同步 | OpenSpec、`QUALITY_SCORE.md` | 稳定规则和下一步说明更新 | 新守门规则有规范来源 |

## 分步执行

1. 新增本执行计划。
2. 改造 API 文档同步脚本并单独验证。
3. 新增 OpenSpec change 状态脚本并单独验证。
4. 归档已完成 OpenSpec change。
5. 新增 run 摘要样例，更新质量信号和 OpenSpec。
6. 运行治理检查与增量验证。
7. 完成后将本计划迁移到 completed。

## 验收映射

| 验收编号 | 来源 | 验证入口 | 通过标准 |
|----------|------|----------|----------|
| AC-001 | 报告 P0-4 | `scripts/harness/check-api-doc-sync.sh` | 自动发现 system Controller，路由同步检查通过 |
| AC-002 | 报告 P0-5 | `scripts/harness/check-openspec-change-state.sh` | active OpenSpec change 均有 proposal、tasks 和未完成项 |
| AC-003 | 报告 0-7 天 | `scripts/harness/run-governance-checks.sh` | 新增治理守门接入统一入口并通过 |
| AC-004 | AGENTS 标准工作回路 | `scripts/harness/verify-changed.sh working-tree` | 增量验证完成或给出可解释失败 |

## 验证计划

- `scripts/harness/check-api-doc-sync.sh`
- `scripts/harness/check-openspec-change-state.sh`
- `scripts/harness/run-governance-checks.sh`
- `scripts/harness/verify-changed.sh working-tree`

## 验证分类

| 变更类型 | 最小验证入口 | 是否需要 |
|----------|--------------|----------|
| 文档 / 规范 | `scripts/harness/run-governance-checks.sh` | 是 |
| Java / POM | `scripts/harness/verify-changed.sh working-tree` | 否 |
| API / 契约 | `scripts/harness/check-api-doc-sync.sh` | 是 |
| 数据迁移 | 模块级 migration 测试或 infra 集成测试 | 否 |
| 服务行为 | `scripts/smoke/<service>.sh` 或 runbook 人工验收 | 否 |
| agent 编排 | `scripts/harness/check-agentic-harness-assets.sh`、`scripts/harness/run-agent-evals.sh` | 是 |

## 回滚策略

- 如 API 自动映射误判，可回退脚本改动并保留手写清单，后续改为显式 manifest。
- 如 OpenSpec 状态守门过严，可先从 `run-governance-checks.sh` 中移除接入点，保留独立脚本人工运行。
- 文档样例和质量信号可直接按文件回滚。

## 决策记录

- `2026-06-09`：本次先落 P0-4、P0-5 与低敏 run 摘要样例，不改 Symphony runtime，避免与当前未提交 Symphony 代码交叉。

## 遗留问题

- 真实 Symphony memory / Linear e2e eval、自动 run summary、permission preflight 和 harness metrics 仍需后续专项实现。

## 完成记录

- `2026-06-09`：完成 API 文档自动发现、OpenSpec active change 状态守门、`remove-artemis-visual` change 归档、低敏 run 摘要样例和质量信号更新。
- `2026-06-09`：验证通过 `scripts/harness/check-api-doc-sync.sh`、`scripts/harness/check-openspec-change-state.sh`、`scripts/harness/run-governance-checks.sh`、`scripts/harness/verify-changed.sh working-tree`。
