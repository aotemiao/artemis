## ADDED Requirements

### Requirement: Symphony tracker 适配层支持 Linear 与 memory

`artemis-symphony` SHALL 通过统一的 tracker 适配边界访问工单系统，而不是让协调层直接绑定单一实现。当前仓库至少 SHALL 支持：

- `tracker.kind: linear`
- `tracker.kind: memory`

#### Scenario: memory tracker 启动

- **WHEN** workflow 配置 `tracker.kind: memory`
- **THEN** Symphony SHALL 使用内存 tracker 进行候选拉取、状态刷新和写回模拟，而不是要求 Linear 凭证

### Requirement: Todo 议题在执行前自动认领为 In Progress

当 Symphony 开始实际执行一个 `Todo` 议题时，系统 SHALL 尝试将该议题推进到 `In Progress`，以与参考实现的默认 workflow 语义保持一致。若状态写回失败，Symphony MUST 记录告警，但 MAY 继续当前 worker 尝试，避免把单次 Linear 写失败放大成全链路中断。

#### Scenario: Todo 议题开始执行

- **WHEN** 编排器准备执行一个 tracker 状态为 `Todo` 的 issue
- **THEN** Symphony SHALL 先尝试更新 issue 状态为 `In Progress`，再继续 worker 执行

#### Scenario: 状态写回失败

- **WHEN** `Todo -> In Progress` 状态更新失败
- **THEN** Symphony SHALL 记录 issue 标识与失败原因，并继续该次 worker 尝试

### Requirement: Linear 动态工具链路

当 workflow 使用 Linear tracker 时，Symphony SHALL 在 `thread/start` 中向 Codex app-server 宣告 `linear_graphql` 动态工具，并在 `item/tool/call` 请求到达时使用当前 tracker 鉴权代为执行 Linear GraphQL。

动态工具定义 SHALL 同步沉淀到 `artemis-symphony/tools/registry.json`。注册表 SHALL 至少记录工具名、provider、可用条件、输入 schema、输出 schema、权限等级、是否允许外部写操作、是否允许无人值守、审计事件和稳定失败码。

注册表中的动态工具条目 SHALL 保持可机器校验的稳定外形：`status` 只能使用已知状态，输入 / 输出 schema 必须是 object schema，输出 schema SHALL 至少包含 `success:boolean`、`output:string`、`contentItems:array`，审计字段 SHALL 覆盖成功 / 失败运行历史事件，稳定错误码 SHALL 非空、不重复并与运行时执行器保持同步。

具备外部写能力的动态工具 MUST 在注册表中显式声明 `external_write_allowed: true`，避免把 mutation 能力误标为只读诊断工具。

#### Scenario: Codex 请求调用 linear_graphql

- **WHEN** app-server 发送 `item/tool/call` 且工具名为 `linear_graphql`
- **THEN** Symphony SHALL 执行对应 Linear GraphQL 请求，并把结构化结果回传给 app-server

#### Scenario: Codex 动态工具调用失败

- **WHEN** app-server 发送 `item/tool/call` 且动态工具执行结果为 `success: false`
- **THEN** Symphony SHALL 回传结构化失败结果
- **AND** SHALL 将当前 turn 视为失败，记录 `tool_call_failed` 与 `turn_ended_with_error`

#### Scenario: 动态工具注册表覆盖 linear_graphql

- **WHEN** 仓库暴露 `linear_graphql` 动态工具
- **THEN** `artemis-symphony/tools/registry.json` SHALL 包含同名工具条目
- **AND** 该条目 SHALL 声明 `provider=linear`、`availability.tracker_kind=linear`、输入参数 `query` 与 `variables`
- **AND** 该条目 SHALL 声明稳定输出 schema、审计事件、失败行为和运行时可识别的稳定错误码
- **AND** 该条目 SHALL 声明 `external_write_allowed=true`

### Requirement: 默认 turn sandbox 收敛到当前工作区

当 workflow 未显式配置 `codex.turn_sandbox_policy` 时，Symphony SHALL 为每个 issue workspace 生成一个默认的 `workspaceWrite` turn sandbox policy，使默认写权限收敛在当前工作区，而不是完全缺省。

#### Scenario: workflow 未配置 turn sandbox

- **WHEN** workflow 未提供 `codex.turn_sandbox_policy`
- **THEN** Symphony SHALL 生成一个基于当前 workspace 的默认 `workspaceWrite` turn sandbox policy

### Requirement: tracker.assignee 路由与 worker 路由收敛

当 workflow 配置 `tracker.assignee` 时，Symphony SHALL 保留候选 issue 的 assignee 信息，并显式标记 issue 是否仍应路由到当前 worker。若运行中的 issue 不再满足 assignee 路由条件，Symphony SHALL 停止该 worker，而不是继续盲跑。

#### Scenario: issue 不再路由到当前 worker

- **WHEN** 运行中的 issue 在刷新后不再匹配 `tracker.assignee`
- **THEN** Symphony SHALL 将该 issue 视为“不再路由到当前 worker”，并终止当前运行，不做终态清理

### Requirement: SSH worker 语义与远程 workspace 链路

Symphony SHALL 支持参考实现中的 `worker.ssh_hosts` 与 `worker.max_concurrent_agents_per_host` 配置语义。配置了 SSH worker 时，系统 SHALL 能够：

- 在可用 host 中选择负载最低的 worker
- 在重试时优先复用上一次 worker host
- 通过 SSH 创建/删除远程 workspace
- 通过 SSH 执行远程 workspace hooks
- 通过 SSH 启动远程 Codex app-server

#### Scenario: 多个 SSH worker 可用

- **WHEN** workflow 配置多个 `worker.ssh_hosts`
- **THEN** Symphony SHALL 按当前运行中 agent 数量选择负载最低的 host，并尊重 `worker.max_concurrent_agents_per_host`

#### Scenario: issue 重试

- **WHEN** 某个 issue 进入重试队列，且上一次尝试记录了 worker host
- **THEN** Symphony SHALL 优先在该 host 仍有容量时复用原 host，再回退到其他可用 host

#### Scenario: issue 存在待执行调度计划

- **WHEN** 某个 issue 已存在待执行的 retry 或 continuation 调度计划
- **THEN** 普通 poll dispatch SHALL NOT 在该计划到期前再次派发同一 issue
- **AND** 该 issue 只能由对应定时调度路径释放后再次进入 worker 尝试
- **AND** worker exit 写入调度计划、普通 dispatch 最终下发检查、定时调度释放计划 SHALL 共享原子状态迁移，避免 `refresh` 或后台 tick 在状态空窗内重复下发同一 issue

### Requirement: 高风险 issue 支持独立 adversarial review attempt

当 workflow 开启 `delivery.adversarial_review.enabled` 时，Symphony SHALL 在匹配过滤条件的实现 run 正常完成后派生一个独立 adversarial review run，而不是只依赖实现 agent 自评。该 review run SHALL 使用只读或等价受限的 turn sandbox policy，并在低敏 run summary 中标记 `attempt.dispatch_kind=adversarial_review` 与父实现 run id。

#### Scenario: 实现 run 完成后派生 review run

- **WHEN** `delivery.adversarial_review.enabled=true` 且 issue title 或风险 label 命中过滤条件
- **THEN** 实现 run 正常完成后 SHALL 调度一次 `dispatch_kind=adversarial_review` 的独立 run
- **AND** review run 的 prompt SHALL 明确要求独立审查、只读或 diff-only 模式，并引用 adversarial review prompt / skill 与安全审查资产
- **AND** review run summary SHALL 记录 `attempt.parent_run_id` 指向实现 run
- **AND** review run 完成或失败后 SHALL NOT 再递归调度新的 adversarial review run

### Requirement: approval_policy 支持 string 与 map

为对齐参考实现中的 workflow 语义，Symphony SHALL 接受 `codex.approval_policy` 的 string 与 map 两种配置形式，并在 `thread/start` 与 `turn/start` 中按原始结构传给 Codex app-server。

#### Scenario: workflow 使用 map 形式 approval_policy

- **WHEN** `codex.approval_policy` 在 workflow 中被配置为对象/map
- **THEN** Symphony SHALL 保留该对象结构并原样下发给 app-server，而不是强制字符串化

### Requirement: Result 类型暴露 isSuccess 访问器

artemis-symphony 中用于封装成功/失败与 payload 的 Result 类型（如 `LinearTrackerClient.Result`、`WorkspaceManager.Result`）SHALL 提供 `boolean isSuccess()` 方法，与 record 组件 `success` 语义一致，以便调用方使用 `isSuccess()` 或 `success()` 均可通过编译。

#### Scenario: 调用方使用 isSuccess() 编译通过

- **WHEN** 代码中对 `Result` 实例调用 `isSuccess()`
- **THEN** 编译通过且返回值与组件 `success` 一致

### Requirement: 议题与会话相关日志上下文

凡与具体议题或 Codex 会话相关的日志（含 dispatch、worker 生命周期、reconcile 终止、hook 失败等），SHALL 在单条日志可检索字段中包含 `issue_id` 与 `issue_identifier`（若当时可得）。凡与 Codex 会话握手或 turn 流相关的日志，SHALL 在可得时包含 `session_id`。实现 MAY 通过 SLF4J MDC 或等价机制写入上述键，MUST NOT 在日志中输出 tracker API 密钥或未脱敏密钥。

#### Scenario: Worker 启动带议题上下文

- **WHEN** 编排器为某议题启动一次 worker 尝试
- **THEN** 该路径上输出的结构化/文本日志 SHALL 携带该议题的 `issue_id` 与 `issue_identifier`

#### Scenario: 会话事件带 session 标识

- **WHEN** 已从 app-server 获得 `thread_id` 与 `turn_id` 并组合为 `session_id`
- **THEN** 后续与该会话相关的日志 SHALL 携带 `session_id`

### Requirement: WORKFLOW.md 运行时重载

`artemis-symphony` SHALL 在进程运行期间监听已配置的 `WORKFLOW.md` 路径（或其所在目录）的文件变更，并在检测到变更后重新加载 YAML 前件与提示模板。重载成功后，后续 poll 间隔、并发上限、tracker 配置、hooks、codex 超时等 SHALL 从新配置生效。重载失败时 SHALL 保留上一次有效配置并输出操作者可见的错误日志，MUST NOT 因单次失败终止编排主循环。

#### Scenario: 有效 WORKFLOW 更新后配置生效

- **WHEN** 操作者保存对 `WORKFLOW.md` 的合法修改且解析与校验通过
- **THEN** 服务 SHALL 在合理延迟内将新配置应用于后续 tick、重试调度与 agent 启动（已在运行的会话不要求自动重启）

#### Scenario: 非法 WORKFLOW 更新不破坏当前运行

- **WHEN** 保存的 `WORKFLOW.md` 无法解析或导致 dispatch 前校验失败
- **THEN** 服务 SHALL 继续使用上一次有效配置并记录错误，编排循环 SHALL 保持存活

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
