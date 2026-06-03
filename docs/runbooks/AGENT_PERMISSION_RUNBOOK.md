# Agent Permission Runbook

Status: maintained
Last Reviewed: 2026-06-02
Review Cadence: 90 days

本 runbook 用于选择 agent 执行任务时的权限、sandbox 和人工审批策略。目标是让 agent 能完成工作，同时避免把高风险写权限、网络权限或外部系统副作用变成默认行为。

## 默认策略

- 默认使用受限工作区写权限，只允许写当前 issue workspace 或当前仓库。
- 默认不允许 agent 静默执行会删除数据、重置 Git 历史、推送远端、发送邮件、调用生产环境、修改云资源的命令。
- 默认把网络访问、依赖下载、外部 API 写操作和真实部署视为需要明确授权的动作。
- 默认把 secrets、token、密钥、生产数据和客户数据视为不可写入 prompt、日志、issue 评论和 trace 摘要的内容。

## 权限矩阵

| 任务类型 | 推荐 sandbox | 推荐 approval | 说明 |
|----------|--------------|----------------|------|
| 纯文档、模板、脚本结构检查 | `workspace-write` | `on-request` 或等价 reject 策略 | 可直接修改仓库文件，但不需要外部副作用 |
| 代码实现和本地测试 | `workspace-write` | `on-request` | 允许写源码和运行本地验证，依赖下载失败时再申请网络 |
| 依赖升级、镜像构建、外部下载 | `workspace-write` | `on-request` | 需要说明下载来源、版本和回滚方式 |
| 部署、回滚、真实 e2e、外部 issue 写回 | `workspace-write` 或隔离 worker | `on-request` | 必须引用 runbook，并在 handoff 中列出副作用 |
| 生产数据、密钥、权限策略变更 | 最小权限环境 | 人工逐步审批 | agent 只能准备方案和检查清单，不能静默执行 |
| 大规模删除、Git 历史重写、强制推送 | 不允许默认执行 | 人工显式授权 | 需要单独确认目标、范围和恢复手段 |

## Symphony 配置建议

`artemis-symphony/WORKFLOW.md.example` 中的 `codex.approval_policy` 和 `codex.thread_sandbox` 应按任务风险调整：

- 日常仓库任务保留 `workspace-write`，由 turn sandbox 收敛到当前 workspace。
- 无人值守任务不要默认使用 `danger-full-access`。
- 如果必须使用 `approval_policy: never`，任务必须限定在隔离 workspace，且不得包含外部副作用。
- SSH worker 必须保证 workspace 隔离、日志可追踪、清理策略明确。

## 审批前检查

在批准 agent 提权或外部副作用前，先确认：

1. 任务是否已有 Feature Spec、执行计划或 runbook。
2. 命令会读写哪些路径、调用哪些外部系统。
3. 是否会产生不可逆副作用。
4. 是否有回滚策略或失败现场保留方式。
5. 最终 handoff 是否会记录执行命令、结果和剩余风险。

## 禁止沉淀的内容

- 真实密钥、token、账号密码。
- 生产数据库连接串。
- 客户数据、个人敏感数据和未脱敏日志。
- 未经确认的外部系统写操作结果全文。
