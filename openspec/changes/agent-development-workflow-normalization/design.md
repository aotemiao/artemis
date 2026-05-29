## Context

Artemis 目前已经把执行入口、验证脚本、runbook 与 Symphony 编排能力铺开，但“默认 agent 开发方式”的知识散落在多个文件中：

- `AGENTS.md` 说明先读哪些资料
- `README.md` 说明 Harness 与 OpenSpec 的分工，并作为文档目录职责的权威入口
- `openspec/specs/agent-task-assets/spec.md` 说明执行计划与规范变更的关系
- `artemis-symphony/WORKFLOW.md.example` 说明默认执行时需要读哪些文件

这些内容 individually 都对，但对于“提一个需求后应该怎么分流”仍不够直接。团队需要的是一个更短的默认判断模型，而不是再多一个只讲背景的总览文档。

## Goals / Non-Goals

**Goals:**

- 给提需求、接需求、agent 落地提供统一的判断顺序
- 把 `artemis-symphony`、Harness Engineering、OpenSpec、执行计划的职责边界压缩成单页模型
- 给 Symphony 提供一个可直接复用的需求受理 prompt
- 让规则变化既有稳定 spec，也有 change 轨迹

**Non-Goals:**

- 不在本次变更中改写现有 `README.md`、`AGENTS.md` 或 `WORKFLOW.md.example` 的入口结构
- 不引入新的自动化检查器来强制模板字段
- 不修改业务服务、测试基线或部署脚本

## Decisions

| 决策 | 说明 | 备选未采纳原因 |
|------|------|----------------|
| 新增独立能力 `agent-development-workflow` | 让“agent 开发分流”成为稳定约束，而不是散落在若干说明里 | 只改 `agent-task-assets`：会把“任务资产”与“需求分流”混在一起 |
| 新增单页工作流文档 | 面向人类快速判断，比直接读 spec 更容易上手 | 只写 spec：规范清楚，但不够适合作为提需求入口 |
| 新增 Symphony 需求受理 prompt | 让编排器能直接复用同一套模板 | 只写文档：agent 仍需每次人工拼装分流步骤 |
| 当前先新增独立资产 | 降低与仓库里其他在途总览文档改动的冲突风险 | 直接改所有总览文件：发现性更好，但更容易与当前工作树冲突 |

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 新工作流文档与现有说明重复 | 只保留“如何分流”的最小模型，把细节继续引用到既有文档 |
| 入口未立刻串到所有总览文档 | 在执行计划中记录为后续可选动作，等待当前在途大改收敛 |
| 需求模板仍可能被跳过 | 通过 Symphony prompt 与 OpenSpec 双重沉淀，先让默认做法一致，再考虑后续机检 |

## Migration Plan

- 本次只新增文档、规范和 prompt 资产，无数据迁移。
- 后续若需要提升发现性，可在总览文档中逐步加入到新工作流文档的引用。
- 若需要回滚，可删除新增文档、prompt 与对应 spec / change 目录，不影响业务运行。

## Open Questions

- 是否要在后续变更中给“需求模板必填字段”增加自动检查或 issue 模版约束。
- 是否要把新 prompt 直接接入 `artemis-symphony/WORKFLOW.md.example` 的默认指引。
