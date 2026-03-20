## ADDED Requirements

### Requirement: 刷新接口触发真实调度周期

`POST /api/v1/refresh` SHALL 触发一次与定时 tick 等价的处理路径：至少包含对运行中议题的 reconciliation，并在 dispatch 校验通过时执行候选拉取与 dispatch 决策。实现 MAY 合并短时间内的多次请求并在响应中标明 `coalesced`。成功响应 SHALL 使用 `202 Accepted` 与 JSON 体，字段 SHALL 至少包含 `queued`、`requested_at` 及所执行操作列表（如 `poll`、`reconcile`）。

#### Scenario: 调用 refresh 后排程被执行

- **WHEN** 客户端发送 `POST /api/v1/refresh` 且服务正常
- **THEN** 编排器 SHALL 在可接受延迟内执行至少一次 reconciliation，并在配置有效时继续执行候选拉取与 dispatch 逻辑

### Requirement: 按议题标识查询运维详情

服务 SHALL 提供 `GET /api/v1/issues/{identifier}`（path 变量为 Linear 风格 `issue_identifier`，如 `MT-649`），返回当前内存中该议题的运维态摘要（含 workspace 路径、running/retry 信息、可选 recent_events）。若当前进程不掌握该议题，SHALL 返回 `404` 与 JSON 错误信封（如 `{"error":{"code":"issue_not_found","message":"..."}}`）。

#### Scenario: 未知议题返回 404

- **WHEN** 客户端请求未在 running、retry 或实现所追踪集合中出现的 `identifier`
- **THEN** 响应状态 SHALL 为 404 且 body 为 JSON 错误信封
