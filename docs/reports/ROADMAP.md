# Harness Engineering Roadmap

Status: maintained
Last Reviewed: 2026-06-10
Review Cadence: 90 days

## 目标

把 Artemis 从“有一些 AI 相关能力的微服务仓库”，推进到“对 agent 友好、可验证、可恢复、可持续演进”的工程系统。

## Phase 1: 建立稳定入口

目标：任何 agent 在 5 分钟内知道要读什么、跑什么、如何验证。

交付物：

- `AGENTS.md`
- `ARCHITECTURE.md`
- `QUALITY_SCORE.md`
- `docs/governance/`
- `docs/runbooks/`
- `docs/reports/`
- `docs/agent-workflow/`
- `docs/exec-plans/`
- `scripts/dev/`
- `scripts/harness/`
- `scripts/smoke/`

完成标准：

- 新成员或 agent 不依赖聊天上下文即可找到主要入口
- 复杂任务有计划目录可落地
- 至少存在一条增量验证命令和一条全量验证命令

状态：已完成本轮基础骨架

## Phase 2: 把验证回路变成默认路径

目标：把“记得自己跑检查”变成“仓库默认会要求检查”。

交付物：

- Maven lifecycle 中的质量门
- CI 中的 OpenSpec 同步检查
- 更系统的 ArchUnit / 集成测试
- 关键模块的 smoke 命令

完成标准：

- 关键工程约束能在 `mvn verify` 或 CI 中失败
- 主要服务改动都有对应最小验证回路

状态：已完成当前阶段补强，`mvn verify` 已接入覆盖率基线，契约 / API 文档同步检查、关键路径测试基线与更多 ArchUnit 约束均已落仓

## Phase 3: 提升可观测性与 agent 自主性

目标：让 agent 不只会改代码，也能观察自己是否真的修好了问题。

交付物：

- 日志与健康检查标准入口
- 启动时间、失败原因、关键端点可达性的脚本化断言
- Symphony workflow 中的默认验证步骤
- 常见故障 runbook

完成标准：

- agent 能独立复现、验证、回归检查至少一类真实问题
- 人工主要做判断和优先级，不再承担大部分机械验证

状态：已完成当前核心链路，`wait-http`、service smoke runbook、`check-service-readiness`、聚合 smoke 与 Symphony troubleshooting 已补齐

## Phase 4: 控制熵增并形成持续治理

目标：随着 agent 产出增多，仓库仍保持可读、可改、可守门。

交付物：

- 定期文档整理任务
- 质量评分更新机制
- 重复模式扫描
- 技术债与工程债执行计划

完成标准：

- 质量问题被连续、小步、低成本地清理
- 仓库中的知识比聊天系统更完整、更可信

状态：已完成当前阶段闭环，周期性治理工作流、重复模式扫描、质量问题归档标准与 agent review loop 已建立

## Phase 5: Agentic Engineering 评测与风险审查

目标：让 agent 不只“按规则写代码”，还要能被评测、被复盘、被安全审查，并按变更风险选择验证集。

交付物：

- agent workflow eval 与运行摘要资产
- 可执行 Symphony memory agent eval
- 权限策略、安全审查和风险分级验证 runbook
- Symphony permission preflight / audit
- Harness metrics report generator
- Symphony run environment snapshot
- Symphony dynamic tool registry
- adversarial review skill / prompt
- agentic harness 资产守门脚本

完成标准：

- agent workflow fixture 可被脚本检查。
- memory agent eval 可真实启动 Symphony、fake Codex app-server 并验证 workspace、运行历史、事件、指标摘要和 JSON summary。
- Harness metrics report generator 可从低敏 eval / run artifacts 输出 Markdown / JSON scorecard，并聚合 run environment 与 deploy drill 分布。
- Symphony dynamic tool registry 可记录 `linear_graphql` 的输入输出 schema、外部写能力、无人值守策略和稳定失败码，并由 Symphony 资产检查守住。
- 高风险改动有安全审查清单、权限策略和 adversarial review 入口。
- CI 与本地治理复用同一批 harness 脚本，避免重复清单漂移。

状态：已完成当前基础闭环，已补充低敏 agent run 摘要样例、自动 JSON run summary、permission preflight / audit 字段、run environment 快照、SQLite history metrics API、`/runs` 运行指标摘要和 deploy drill report 指标摘要；后续继续扩展更多 eval case、Linear / live eval、per-run 环境隔离和跨平台 dashboard。

> 注：原属本阶段的"自我度量"层——Harness metrics report generator、CI artifact metrics 快照、GitHub event delivery signal 采集入口、失败 run 到 eval dataset 草稿生成器，以及 `scripts/e2e/run-symphony-agent-eval.sh` 可执行 memory eval——已在 `slim agentic harness — drop self-measurement` 变更中退役。当前 Symphony 运行可观测性以 SQLite run history、`/runs` 页面、`/api/v1/history/*` 指标 API 和（gitignore 的）per-attempt JSON 审计摘要为准。上文"交付物"与"完成标准"保留作阶段历史记录。

## 封板后的演进原则

1. 继续让 `scripts/harness/full-verify.sh` 与 `scripts/harness/verify-changed.sh` 作为团队默认入口
2. 对业务需求先收敛到 `docs/feature-specs/`，把验收标准和验证映射写清楚
3. 继续把复杂任务落到 `docs/exec-plans/active/`，减少上下文散失
4. 对高风险 agentic 交付同步使用安全审查、权限策略、风险分级验证和 agent eval
5. 新增工程能力时，同步补文档、脚本、守门和 runbook，避免知识重新退回聊天上下文
