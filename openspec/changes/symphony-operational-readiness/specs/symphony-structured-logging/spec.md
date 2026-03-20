## ADDED Requirements

### Requirement: 议题与会话相关日志上下文

凡与具体议题或 Codex 会话相关的日志（含 dispatch、worker 生命周期、reconcile 终止、hook 失败等），SHALL 在单条日志可检索字段中包含 `issue_id` 与 `issue_identifier`（若当时可得）。凡与 Codex 会话握手或 turn 流相关的日志，SHALL 在可得时包含 `session_id`。实现 MAY 通过 SLF4J MDC 或等价机制写入上述键，MUST NOT 在日志中输出 tracker API 密钥或未脱敏密钥。

#### Scenario: Worker 启动带议题上下文

- **WHEN** 编排器为某议题启动一次 worker 尝试
- **THEN** 该路径上输出的结构化/文本日志 SHALL 携带该议题的 `issue_id` 与 `issue_identifier`

#### Scenario: 会话事件带 session 标识

- **WHEN** 已从 app-server 获得 `thread_id` 与 `turn_id` 并组合为 `session_id`
- **THEN** 后续与该会话相关的日志 SHALL 携带 `session_id`
