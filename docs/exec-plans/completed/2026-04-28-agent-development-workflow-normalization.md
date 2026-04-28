# Agent 开发方式规范化

## 背景

当前仓库已经同时具备 `artemis-symphony`、Harness Engineering 与 OpenSpec，但“提需求时先判断什么、改动应该落到哪里”还不够直白。结果是同一个任务可能被误判为“改 prompt”“改 runbook”或“改 spec”，增加了沟通成本。

## 目标

- 固化 `artemis-symphony`、Harness Engineering、OpenSpec 与执行计划的默认分工
- 给提需求、接需求、agent 分流提供统一模板
- 给 Symphony 补一个可直接复用的需求受理 prompt 资产

## 非目标

- 不重构现有 Symphony 编排器实现
- 不调整现有业务服务代码或领域模型
- 不一次性改写所有总览文档的入口结构

## 范围

- 新增仓库级 agent 开发工作流文档
- 新增对应 OpenSpec 规范
- 新增 Symphony 需求受理 prompt
- 不修改业务代码、数据库脚本或服务启动脚本

## 风险

- 新文档如果不够聚焦，可能与现有 README / runbook 重复
- 当前仓库存在较多进行中的工作树改动，需要避免与它们产生无关冲突

## 分步任务

1. 梳理现有分工与痛点
2. 设计统一的需求分流规则
3. 新增文档、规范与 prompt 资产
4. 执行最小可解释验证并归档

## 验证

- `scripts/harness/verify-changed.sh staged`
- 验证通过标准：新增文档、OpenSpec 与 prompt 资产能够通过仓库治理检查

## 决策记录

- `2026-04-28`：采用“先判断是否改规则，再判断改哪个层”的统一入口，避免一开始就围绕工具名做分流。
- `2026-04-28`：优先新增独立资产，而不是修改已存在大量在途改动的总览文件，以降低合并冲突风险。

## 遗留问题

- 后续可在总览文档与 `WORKFLOW.md.example` 中增加到新工作流文档的显式链接，待当前在途改动收敛后再做更安全。
