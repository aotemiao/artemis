# Agent Run Reports

Status: maintained
Last Reviewed: 2026-06-02
Review Cadence: 90 days

本目录说明 agent 运行轨迹摘要的留存规则。完整模型提示词、工具输出和敏感日志不应默认提交到仓库；仓库只沉淀可审计、可复盘、低敏的摘要。

## 摘要用途

agent run 摘要用于回答：

- agent 处理了哪个需求或 issue。
- agent 使用了哪些事实源、Feature Spec、执行计划和 OpenSpec。
- agent 实际执行了哪些验证命令。
- 哪些风险由自动化验证覆盖，哪些需要人工 review。
- 本次运行是否产生外部副作用。

## 推荐文件位置

- 一次性本地调试产物：`artifacts/agent-runs/`，默认不提交。
- 需要沉淀的复盘摘要：`docs/reports/agent-runs/YYYY-MM-DD-<topic>.md`。

## 摘要模板

```md
# <Run Title>

Status: completed
Last Reviewed: YYYY-MM-DD
Review Cadence: 90 days

## 任务来源

- Issue / Feature Spec / 执行计划：

## 使用资产

- 事实源：
- Prompt / Skill：
- Runbook：

## 执行摘要

- 方案：
- 改动：
- 外部副作用：

## 验证证据

| 命令 | 结果 | 说明 |
|------|------|------|
| `<command>` | 通过 / 失败 | <关键断言> |

## 风险与审查

- 自动覆盖：
- 人工复核：
- 未覆盖风险：
```

## 禁止提交

- 完整 prompt、完整聊天记录或包含密钥的工具输出。
- token、密码、生产连接串、客户数据和个人敏感数据。
- 未脱敏的外部系统响应全文。
