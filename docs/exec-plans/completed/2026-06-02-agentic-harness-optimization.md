# Agentic Harness Optimization

Status: completed
Last Reviewed: 2026-06-02
Review Cadence: 90 days

## 背景

本计划用于落实一次 Harness Engineering 优化审查的结论。当前仓库已经具备 Feature Spec、执行计划、OpenSpec、脚本化验证、Symphony 编排和 reviewer handoff，但仍存在治理入口重复、历史 active 资产噪声、Feature Spec 风险字段不足、缺少 agent eval / trace / permission / adversarial review 入口等问题。

## 关联需求与规范

- 需求来源：`根据现在流行的agentic开发流程...生成一篇详尽的优化报告` 后续执行要求。
- OpenSpec：`openspec/specs/agent-development-workflow/spec.md`、`openspec/specs/spec-driven-delivery/spec.md`、`openspec/specs/harness-governance/spec.md`
- Runbook / API / 其它资产：`docs/agent-workflow/AGENT_DEVELOPMENT_WORKFLOW.md`、`docs/agent-workflow/AGENT_REVIEW_LOOP.md`、`artemis-symphony/WORKFLOW.md.example`

## 目标

- 降低重复治理入口和历史 active 资产噪声。
- 强化 Feature Spec 与执行计划对异常、权限、事务、并发、幂等、SQL、日志和可观测性的显式要求。
- 补齐 agent eval、运行轨迹摘要、权限策略、adversarial review 和 risk-based verification 入口。
- 将新增能力接入仓库索引和 harness 守门，避免只停留在文档说明。

## 非目标

- 不在本计划中实现真实外部 SaaS 评测平台。
- 不要求历史所有 Feature Spec 立即补齐新增字段。
- 不改变业务服务运行逻辑或数据库结构。

## 范围

- `.github/workflows/`
- `scripts/harness/`
- `docs/`
- `artemis-symphony/`
- `openspec/specs/`
- `README.md`、`AGENTS.md`、`QUALITY_SCORE.md`

## 风险

- 新增守门如果过宽，会让 agent 在轻量任务上承担过多文档成本。
- agent eval 先以静态 fixture 和结构检查起步，只能验证流程资产，不代表真实模型质量。
- trace 摘要需要避免泄露 token、密钥、客户数据和完整提示词。

## 任务拆解

| 编号 | 任务 | 输入 | 输出 | 验收标准 |
|------|------|------|------|----------|
| T-001 | CI 与 governance 去重 | 当前 GitHub Actions 与 harness 脚本 | workflow 复用统一入口 | CI 不重复执行同一批治理脚本 |
| T-002 | 归档历史 active 资产 | active exec-plan 与 OpenSpec change | completed/archive 目录 | active 只保留真实未完成计划 |
| T-003 | 增强需求与计划模板 | Feature Spec / exec-plan 模板 | 风险、异常和验证分类字段 | 新模板覆盖异常、幂等、并发、事务、安全和可观测性 |
| T-004 | 补安全与权限 runbook | 优化报告与现有 review loop | security checklist、permission runbook | reviewer 能按清单审权限、锁、事务、SQL、日志 |
| T-005 | 补 agent eval 与 trace 入口 | Symphony 与 Harness 资产 | eval fixtures、eval 脚本、agent-runs 目录说明 | governance 可检查 eval / trace 资产存在并结构有效 |
| T-006 | 补 adversarial review skill | self-review prompt | adversarial review skill / prompt | Symphony 资产索引和 workflow 能发现该能力 |
| T-007 | 回写索引、质量评分与 OpenSpec | README / docs / AGENTS / specs | 更新后的事实源 | 文档索引与守门脚本通过 |

## 分步执行

1. 归档已完成的历史 active 执行计划与 OpenSpec change。
2. 收敛 GitHub Actions 到统一 harness 入口。
3. 新增风险分类、权限策略、安全审查、agent eval、trace 摘要和 adversarial review 资产。
4. 更新 `scripts/harness/run-governance-checks.sh` 与专项检查脚本。
5. 回写 README、docs 索引、AGENTS、QUALITY_SCORE 和相关 OpenSpec。
6. 执行 `scripts/harness/verify-changed.sh working-tree`。

## 验收映射

| 验收编号 | 来源 | 验证入口 | 通过标准 |
|----------|------|----------|----------|
| AC-001 | T-001 | `scripts/harness/check-agentic-harness-assets.sh` | CI workflow 复用统一治理入口且不存在已知重复模式 |
| AC-002 | T-002 | `find docs/exec-plans/active openspec/changes -maxdepth 2 -type f` | active 区仅保留未完成专项，已完成 change 进入 `openspec/changes/archive/YYYY-MM-DD-<name>/` |
| AC-003 | T-003 | `scripts/harness/check-agentic-harness-assets.sh` | Feature Spec / exec-plan 模板包含风险和验证分类字段 |
| AC-004 | T-004 | `scripts/harness/check-agentic-harness-assets.sh` | 安全审查和 agent 权限 runbook 可发现 |
| AC-005 | T-005 | `scripts/harness/check-agentic-harness-assets.sh` | agent eval / trace 资产存在且结构有效 |
| AC-006 | T-006 | `scripts/harness/check-symphony-assets.sh` | adversarial review skill / prompt 纳入 Symphony 守门 |
| AC-007 | T-007 | `scripts/harness/verify-changed.sh working-tree` | 变更范围内治理检查通过 |

## 验证计划

- `bash -n $(find scripts -type f -name '*.sh' | sort)`
- `scripts/harness/check-agentic-harness-assets.sh`
- `scripts/harness/run-agent-evals.sh`
- `scripts/harness/verify-changed.sh working-tree`

## 回滚策略

- 若 CI 去重导致可读性下降，恢复 workflow 的单项步骤，但保留 `run-governance-checks.sh` 作为本地事实源。
- 若新增 eval / trace 守门误报，先将专项脚本降级为结构检查，再逐步增强判定。
- 文档新增资产均可按文件级 revert 回退，不影响业务服务运行。

## 决策记录

- `2026-06-02`：本轮优先减少重复守门和历史 active 噪声，再补 agent eval / trace / permission 这些更高阶能力。
- `2026-06-02`：agent eval 先使用静态 fixture 验证流程资产，避免把真实模型输出稳定性绑定到基础治理 CI。
- `2026-06-02`：全量验证暴露 Mockito inline mock maker 在当前 JDK 21 发行版上无法 self-attach，已将 Surefire/Failsafe 的 Mockito javaagent 固化为根 POM 约束。

## 遗留问题

- 真实 agent e2e eval 仍需在后续结合 Symphony memory tracker 或 Linear sandbox 继续深化。
- trace 摘要目前先作为格式与留存规则，后续可扩展为 Symphony 自动生成。
