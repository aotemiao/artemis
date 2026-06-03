## ADDED Requirements

### Requirement: Agent 开发分工必须可被显式判定

仓库 SHALL 为 agent 开发提供一套清晰、可重复的分工模型，至少包含以下层次：

- `artemis-symphony`：负责 issue 拉取、workspace、agent 调度、状态回写，以及 prompt / skill / workflow 等编排资产
- Harness Engineering：负责仓库内的入口文档、脚本、runbook、验证回路与交付编排
- OpenSpec：负责稳定规则、模块边界、契约约束、质量门与默认 workflow 规则
- `docs/exec-plans/`：负责复杂任务的实施计划、风险、步骤与阶段性验证

该分工 SHALL 先用于需求分流，再用于实施回写，避免开发者或 agent 一开始就围绕工具名猜测落点。

#### Scenario: 新需求涉及默认 agent 工作流

- **WHEN** 一个需求要求“以后所有 agent 任务都必须包含验收标准”
- **THEN** 该任务 SHALL 被识别为默认 workflow 规则变化，而不是仅仅视为 prompt 文案调整
- **AND** 仓库 SHALL 同步更新相关 OpenSpec artifact

### Requirement: 仓库必须提供统一需求受理模板

仓库 SHALL 提供一份人和 agent 共用的最小需求模板，至少包含以下字段：

- 背景或问题
- 目标
- 范围
- 非目标
- 验收标准
- 影响模块
- 规则是否变化
- 需要补的仓库资产
- 验证方式

该模板 SHALL 优先用于非平凡任务的受理与分流，减少“只给一句话就开始改代码”的默认行为。

#### Scenario: 原始需求只给出一句模糊描述

- **WHEN** 一个 issue 或聊天消息仅写“帮我规范一下 agent 开发方式”
- **THEN** agent SHALL 先用统一模板补齐验收标准、规则变化与验证方式
- **AND** 在关键信息补齐前，MUST NOT 直接把任务归类为纯代码修改

### Requirement: Agent 默认按变化类型分流交付

仓库 SHALL 要求 agent 先判断任务属于哪类变化，再决定落到哪一层：

- 既有规则内的功能 / 缺陷修复：SHALL 优先交付代码、测试、文档；复杂任务 MAY 同步补执行计划
- 稳定规则变化：SHALL 同步更新 OpenSpec；若任务还需要分阶段推进，SHALL 同时补执行计划
- 工程入口或验证能力缺失：SHALL 优先补 Harness 文档、脚本、runbook 与守门入口
- 编排资产缺失：SHALL 优先补 Symphony prompt、skill 或 workflow 资产；若影响默认 workflow 规则，SHALL 同步更新 OpenSpec

#### Scenario: 任务只暴露出验证入口缺失

- **WHEN** agent 能判断业务规则没变，但缺少统一 smoke 或 verify 入口
- **THEN** 该任务 SHALL 优先落到 Harness Engineering 资产，而不是先创建新的业务规范

#### Scenario: 任务同时改规则又补分阶段落地

- **WHEN** 一个任务既修改稳定规则，又需要分阶段推进实现
- **THEN** 该任务 SHALL 同时更新 OpenSpec artifact 与 `docs/exec-plans/`
- **AND** OpenSpec SHALL 描述规则如何变化，执行计划 SHALL 描述实现如何推进
