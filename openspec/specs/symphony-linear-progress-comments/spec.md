## ADDED Requirements

### Requirement: Symphony 可选 Linear 进度评论回写

仓库 SHALL 允许 `artemis-symphony` 在 `WORKFLOW.md` 显式开启后，向当前 Linear issue 回写基于 worker 尝试结果的进度评论。该能力 MUST 为可选扩展；未开启时，Symphony MUST NOT 发起 Linear 评论 mutation。

#### Scenario: 未开启评论回写

- **WHEN** workflow 未配置或显式关闭 Linear 评论回写
- **THEN** Symphony SHALL 继续按既有方式调度与重试，且不会向 Linear 发送评论写回请求

#### Scenario: 开启评论回写并完成一次尝试

- **WHEN** workflow 开启 Linear 评论回写，且某个 issue 的一次 worker 尝试结束
- **THEN** Symphony SHALL 基于 issue、attempt、工作区路径、最近 Codex 事件与 token 统计渲染评论内容，并向该 issue 写入一条评论

### Requirement: 评论模板与默认值

评论回写 SHALL 支持通过 workflow 配置自定义成功 / 失败评论模板；并 MAY 通过 issue 标题过滤仅对特定议题回写。若开启回写但未提供模板，Symphony SHALL 使用仓库内置的默认模板。

#### Scenario: workflow 提供自定义评论模板

- **WHEN** workflow 中提供成功或失败评论模板
- **THEN** Symphony SHALL 使用严格模板渲染该模板，并将 issue / attempt / usage / retry 等上下文暴露给模板

#### Scenario: workflow 未提供评论模板

- **WHEN** workflow 开启评论回写但未提供评论模板
- **THEN** Symphony SHALL 使用默认模板生成可读的进度评论

#### Scenario: workflow 配置标题过滤

- **WHEN** workflow 为评论回写配置 issue 标题过滤规则
- **THEN** Symphony SHALL 仅对标题匹配该规则的 issue 写入评论，未匹配的 issue MUST NOT 回写评论

### Requirement: 评论写回失败降级

Linear 评论模板渲染失败、mutation 失败或外部请求失败时，Symphony MUST 记录结构化告警，但 MUST NOT 因该失败中止 worker 收尾、重试排队或后续调度。

#### Scenario: 评论 mutation 失败

- **WHEN** Symphony 在回写评论时遇到 Linear API 错误或网络失败
- **THEN** Symphony SHALL 记录包含 issue 标识与失败原因的告警日志，并继续完成当前 worker 的退出流程
