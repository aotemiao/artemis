# Harness Engineering Roadmap

Status: maintained
Last Reviewed: 2026-03-23
Review Cadence: 90 days

## 目标

把 Artemis 从“有一些 AI 相关能力的微服务仓库”，推进到“对 agent 友好、可验证、可恢复、可持续演进”的工程系统。

## Phase 1: 建立稳定入口

目标：任何 agent 在 5 分钟内知道要读什么、跑什么、如何验证。

交付物：

- `AGENTS.md`
- `ARCHITECTURE.md`
- `QUALITY_SCORE.md`
- `docs/harness-engineering/`
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

状态：已进入封板阶段，`mvn verify`、docs 守门、OpenSpec diff 检查、服务 smoke 与镜像构建入口都已接入；后续重点转向持续治理而不是补基础骨架

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

状态：已完成当前核心链路，`wait-http`、service smoke runbook、`auth / gateway / symphony` smoke 与 Symphony troubleshooting 已补齐

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

状态：已完成当前封板范围，文档 freshness 守门、周期性 CI 巡检与质量入口已建立；后续按脚手架扩展需求继续增强

## 封板后的演进原则

1. 继续让 `scripts/harness/full-verify.sh` 与 `scripts/harness/verify-changed.sh` 作为团队默认入口
2. 继续把复杂任务落到 `docs/exec-plans/active/`，减少上下文散失
3. 新增工程能力时，同步补文档、脚本、守门和 runbook，避免知识重新退回聊天上下文
