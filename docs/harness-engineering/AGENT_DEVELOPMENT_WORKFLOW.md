# Agent 开发工作流

Status: maintained
Last Reviewed: 2026-04-28
Review Cadence: 90 days

本文件用于把“提需求 -> 判定落点 -> 实施 -> 验证 -> 回写”的默认 agent 开发方式固定下来，减少在 `artemis-symphony`、Harness Engineering 与 OpenSpec 之间反复猜测。

## 一句话分工

- `artemis-symphony`
  负责“任务如何被拉起、分配、执行、回写”，包括 workflow、prompt、skill、workspace 与 issue 状态流转。
- Harness Engineering
  负责“任务如何在仓库里落地”，包括入口文档、脚本、runbook、验证回路与交付编排。
- OpenSpec
  负责“哪些规则是仓库的稳定事实”，包括模块边界、契约要求、质量门、默认 workflow 规则与长期约束。
- `docs/exec-plans/`
  负责“复杂任务这次准备怎么做完”，记录分步实施、风险、验证与阶段性决策。

## 先判断变化类型，不先选工具

提需求或接需求时，先判断任务属于哪一类，再决定应该落到哪一层：

| 变化类型 | 典型信号 | 默认落点 |
|----------|----------|----------|
| 既有规则内的功能 / 缺陷修复 | 规则没变，只是在现有边界内实现或修复 | 代码、测试、文档；复杂任务再补执行计划 |
| 稳定规则变化 | 模块边界、契约、质量门、默认 workflow 规则要改 | OpenSpec 必改；复杂任务再补执行计划 |
| 工程入口或验证能力缺失 | agent 不知道怎么启动、怎么验证、怎么排障 | Harness 文档、脚本、runbook、守门脚本 |
| 编排资产缺失 | 需要让 Symphony 更好地接单、分类、回写或复盘 | Symphony workflow、prompt、skill 等资产 |

默认顺序是：

1. 先问“是不是改规则”
2. 再问“是不是缺入口或验证回路”
3. 最后才问“需不需要改 Symphony 编排资产”

## 默认分流规则

### 1. 只是在既有规则内施工

当需求只是实现功能、修复 bug、补测试或补文档，且不改变稳定约束时：

- 小任务直接改代码、测试、文档并验证
- 跨模块或多步骤任务，增加 `docs/exec-plans/active/`
- 通常**不需要**改 OpenSpec
- 通常**不需要**改 Symphony，除非暴露出已有 prompt / skill 明显不够用

### 2. 规则本身要变

当需求修改以下内容之一时，OpenSpec 必须同步更新：

- 模块边界
- 内外部契约
- 质量门或守门脚本的默认要求
- agent 默认 workflow 规则

如果这类需求还需要分阶段推进，则同时使用：

- OpenSpec：说明规则怎么变
- `docs/exec-plans/`：说明这次怎么落地

### 3. agent 不会落地，但规则没变

当问题是“agent 不知道怎么做、怎么跑、怎么验、怎么排障”，优先补 Harness 资产，而不是先改 OpenSpec：

- runbook
- 启动 / 验证脚本
- smoke / readiness / governance 入口
- 文档中的标准工作流说明

只有当这类补强会改变仓库的**默认长期规则**时，才同步更新 OpenSpec。

### 4. 需要让 Symphony 更会接需求

当问题是“任务自动分流不清楚”或“issue 受理方式不稳定”，优先补 Symphony 资产：

- prompt
- skill
- `WORKFLOW.md` 约定
- reviewer / self-review 模版

如果这会改变仓库默认的 agent 工作流规则，仍要同步回写 OpenSpec。

## 最小需求模板

以后提需求，优先按下面这份模板写，而不是只给一句“帮我改一下”：

```md
标题：

背景 / 问题：

目标：

范围：

非目标：

验收标准：

影响模块：

规则是否变化：
- 是 / 否
- 如果是，改的是哪条稳定规则

需要补的仓库资产：
- 代码 / 测试 / 文档 / 脚本 / runbook / OpenSpec / Symphony prompt / Symphony skill

验证方式：
```

其中最关键的是两项：

- `验收标准`
- `规则是否变化`

如果这两项说不清，agent 很容易把任务误判成“纯代码改动”或“纯文档改动”。

## Agent 默认执行步骤

1. 读 `AGENTS.md`、`ARCHITECTURE.md`、`QUALITY_SCORE.md` 与相关 OpenSpec
2. 用最小需求模板把任务补成结构化描述
3. 判断是否需要执行计划
4. 判断是否需要 OpenSpec 更新
5. 判断是否需要 Harness 资产补强
6. 判断是否需要 Symphony prompt / skill / workflow 资产补强
7. 成组交付代码、测试、文档、脚本与规范
8. 优先执行 `scripts/harness/verify-changed.sh`
9. 交付前按 `docs/harness-engineering/AGENT_REVIEW_LOOP.md` 做自评

## 常见例子

### 例 1：给 `artemis-system` 新增菜单权限接口

默认先落代码、测试、文档。

- 若只是现有规则内扩展：通常不改 Symphony
- 若改了内部 / 外部契约：同步更新 OpenSpec
- 若任务跨多个模块或分阶段：补执行计划

### 例 2：要求所有 agent 需求都必须写验收标准

这不是单纯“改 prompt”，而是默认 workflow 规则变化。

- OpenSpec：定义新默认规则
- Harness 文档：解释人和 agent 如何使用
- Symphony prompt：让编排器可直接复用该模板

### 例 3：新增部署演练任务

如果目标是让仓库长期具备统一部署演练入口：

- Harness：补 runbook、脚本、验证入口
- OpenSpec：如果部署演练成为默认交付规则，则同步写入规范
- Symphony：只有在需要自动化派发或总结该任务时才补 prompt / skill

## 默认原则

- 先判断“是否改规则”，再判断“改哪个工具层”
- `artemis-symphony` 是编排器，不是规范库
- Harness 是交付底座，不是领域规则库
- OpenSpec 是稳定规则，不记录一次性施工步骤
- `docs/exec-plans/` 记录这次如何推进，不替代长期规范
- 代码、测试、脚本、文档、规范尽量成组交付，不把关键知识留在聊天里
