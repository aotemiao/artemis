# Spec Driven Harness Convergence

## 背景

Artemis 已具备仓库级 Harness Engineering 骨架，包括事实源、runbook、执行计划、验证脚本和 Symphony 编排资产。当前主要缺口是业务需求级 Spec、Spec 到验证的映射、agent 交付 handoff 与真实演练资产之间还没有固定链路。

## 目标

- 新增 Feature Spec 层，承载业务需求、验收标准和验证映射。
- 强化执行计划模板，使其可作为 agent 可执行计划。
- 新增守门脚本，检查 Feature Spec 与可复用资产的基础完整性。
- 更新 Symphony 资产，让默认交付流程按 Spec 驱动推进。

## 非目标

- 不引入外部 SDD 平台。
- 不要求所有历史需求立即补 Feature Spec。
- 不自动生成业务测试代码。

## 影响范围

- `docs/feature-specs/`
- `docs/patterns/`
- `docs/exec-plans/templates/`
- `scripts/harness/`
- `artemis-symphony/prompts/`
- `artemis-symphony/skills/`
- `openspec/specs/spec-driven-delivery/`
