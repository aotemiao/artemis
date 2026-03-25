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

#### Scenario: Codex 请求调用 linear_graphql

- **WHEN** app-server 发送 `item/tool/call` 且工具名为 `linear_graphql`
- **THEN** Symphony SHALL 执行对应 Linear GraphQL 请求，并把结构化结果回传给 app-server

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

### Requirement: approval_policy 支持 string 与 map

为对齐参考实现中的 workflow 语义，Symphony SHALL 接受 `codex.approval_policy` 的 string 与 map 两种配置形式，并在 `thread/start` 与 `turn/start` 中按原始结构传给 Codex app-server。

#### Scenario: workflow 使用 map 形式 approval_policy

- **WHEN** `codex.approval_policy` 在 workflow 中被配置为对象/map
- **THEN** Symphony SHALL 保留该对象结构并原样下发给 app-server，而不是强制字符串化
