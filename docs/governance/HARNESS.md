# Harness Engineering

Status: maintained
Last Reviewed: 2026-06-21
Review Cadence: 90 days

本文是 Artemis Harness Engineering 的唯一概念源。其他入口文档只保留薄引用，避免在 README、CHECKLIST 和路线图之间重复解释同一套工程结构。

## 定位

Artemis 将 Harness Engineering 作为仓库级工程结构落地，而不是单独工具。它的目标是让开发者和 agent 共享同一套事实来源、执行入口、验证回路和交付编排。

Harness Engineering 覆盖五类资产：

- 事实源：`AGENTS.md`、`README.md`、`ARCHITECTURE.md`、`QUALITY_SCORE.md`、`openspec/specs/`
- 需求与计划：`docs/feature-specs/`、`docs/exec-plans/`
- 执行入口：`scripts/dev/`、`scripts/smoke/`、`docs/runbooks/`
- 治理守门：`scripts/harness/`、CI 工作流、OpenSpec 同步检查
- 编排资产：`artemis-symphony/`、workflow、skills、prompts、动态工具注册表

## 工作方式

Artemis 的 agentic 开发默认分工是：

- 人负责需求、边界、关键判断和最终审阅。
- agent 负责提出方案、分模块实施、补齐验证和回写交付证据。
- 仓库负责把稳定约束沉淀为可检索、可执行、可审计的资产。

标准闭环是：

1. 先读仓库入口、架构文档、质量状态和相关 OpenSpec。
2. 对业务需求补 Feature Spec；对复杂工程任务补执行计划。
3. 成组修改代码、测试、脚本、文档和规范。
4. 优先通过仓库脚本验证，而不是只依赖聊天记录或人工记忆。
5. 交付时说明改动、验证和剩余风险。

## 当前有效能力

当前仍作为 active 能力维护的是：

- 统一入口文档、架构地图、质量看板、docs 索引和 governance checklist。
- DDD/COLA 分层、内部 client 契约、API 文档同步、关键路径测试和 OpenSpec 规则。
- 本地开发、启动、readiness、smoke、打包、镜像、部署和回滚脚本。
- 增量验证、全量验证、治理检查、重复模式扫描、质量问题归档和文档链接 / 内联路径检查。
- Symphony 编排、权限 preflight、低敏 JSON run summary、SQLite run history、`/runs` 页面和 history metrics API。
- agent run 摘要规则、安全审查清单、权限 runbook、风险分级验证 runbook 和 adversarial review 入口。

## 已下线能力

以下 self-measurement 层已下线，不再作为当前事实源、执行入口或治理守门维护：

- agent workflow eval 目录和 fixture 数据集。
- agent workflow eval 脚本。
- 可执行 Symphony memory agent eval 脚本。
- Harness metrics report generator。
- CI artifact metrics 快照。
- GitHub event delivery signal 采集入口。
- 失败 run 到 eval dataset 草稿生成器。

历史执行计划中的相关路径保留为历史记录，不代表当前仓库仍提供这些入口。当前 Symphony 运行可观测性以 SQLite run history、`/runs` 页面、history metrics API 和低敏 per-attempt JSON 审计摘要为准。
