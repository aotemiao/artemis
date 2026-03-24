## ADDED Requirements

### Requirement: 常见任务 Runbook

仓库 SHALL 为高频工程任务提供专用 runbook，至少覆盖：

- 新增领域服务
- 新增 Dubbo client
- 补 ArchUnit 约束

每份 runbook SHALL 同时说明入口文档、推荐修改范围、验证方式与常见风险，避免 agent 仅依赖聊天记忆工作。

#### Scenario: 新增 Dubbo client 时查找标准入口

- **WHEN** 开发者或 agent 需要为某微服务新增内部 Dubbo client
- **THEN** SHALL 能在仓库中找到对应 runbook，获得模块拆分、契约放置位置、验证脚本与回写要求

### Requirement: 细粒度 Prompt / Skill 资产

仓库 SHALL 提供可复用的 prompt 或 skill 资产目录，用于沉淀常见任务的执行模版。至少 SHALL 覆盖与 runbook 对应的工程任务，以及一个面向自评 / reviewer 的模版。

#### Scenario: agent 需要执行 ArchUnit 补强

- **WHEN** agent 处理“补 ArchUnit 约束”类任务
- **THEN** SHALL 能读取对应 prompt / skill 资产，获取建议步骤、守门脚本和输出要求

### Requirement: Agent 自评与 Reviewer 回路

仓库 SHALL 在默认 agent 工作流中明确加入自评与 reviewer handoff 步骤。自评阶段至少 SHALL 包含：

- 本次修改触发了哪些风险
- 已执行哪些验证
- 剩余风险或未覆盖项

reviewer handoff 阶段 SHALL 引导复核者按相同事实来源核对文档、脚本与测试。

#### Scenario: agent 完成复杂任务

- **WHEN** agent 完成一个跨模块或多步骤任务
- **THEN** 交付说明 SHALL 包含自评结论与 reviewer 复核建议，而不是只给出改动清单
