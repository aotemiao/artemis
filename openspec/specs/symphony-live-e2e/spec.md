## ADDED Requirements

### Requirement: Symphony 提供受显式开关保护的真实 Live E2E 演练

`artemis-symphony` SHALL 提供一条真实的 live e2e 演练链路，用于在显式开启时验证 Symphony 对外部系统的真实集成，而不是只依赖 mock 或模块测试。该演练 MUST 默认关闭，并且仅在用户显式提供开关与凭证时运行。

#### Scenario: 未显式开启 live e2e

- **WHEN** 用户未设置 live e2e 开关
- **THEN** 默认测试回路 SHALL 跳过真实 Linear / Codex / SSH 外部依赖，不得误创建外部资源

#### Scenario: 显式开启 live e2e

- **WHEN** 用户显式开启 live e2e，并提供必要的 Linear 凭证
- **THEN** Symphony SHALL 运行真实端到端演练，而不是使用 mock app-server 或 mock tracker

### Requirement: Live E2E 覆盖本地 worker 与 SSH worker 两种场景

真实 live e2e SHALL 覆盖：

- 本地 worker
- SSH worker

若用户未提供真实 SSH worker 地址，系统 SHOULD 提供 docker fallback worker，以便在本机复现 SSH transport。

#### Scenario: 本地 worker live e2e

- **WHEN** 运行本地 worker live e2e
- **THEN** Symphony SHALL 在本地工作区运行真实 `codex app-server`，并完成同一套验收链路

#### Scenario: SSH worker live e2e

- **WHEN** 运行 SSH worker live e2e
- **THEN** Symphony SHALL 通过真实 SSH transport 在远端 worker 上创建 workspace、启动 `codex app-server` 并读取结果

#### Scenario: 未提供真实 SSH hosts

- **WHEN** 用户未配置真实 SSH worker 地址
- **THEN** live e2e SHOULD 使用 docker 启动临时 SSH worker，作为默认 SSH 场景支撑

#### Scenario: docker fallback worker 需要保留默认 sandbox 语义

- **WHEN** live e2e 使用 docker fallback worker，且宿主环境默认容器权限不足以支持 Codex 的 `workspaceWrite` sandbox
- **THEN** docker worker 准备过程 SHALL 提供足够权限，使默认 sandbox 语义仍然可用，而不是悄悄把 live e2e 降级成更宽松的执行模型

### Requirement: Live E2E 验收标准对齐官方参考实现

真实 live e2e MUST 至少验证以下结果：

- 创建临时 Linear project 与 issue
- 真实运行 `codex app-server`
- 在 workspace 内产生约定副作用文件
- agent 通过 `linear_graphql` 写入约定评论
- issue 被推进到完成态
- 结束后将临时 project 标记为完成，并清理临时本地资产

#### Scenario: live e2e 成功完成

- **WHEN** live e2e 正常跑完
- **THEN** 验证结果 SHALL 同时包含 workspace 副作用、Linear 评论存在、issue 进入完成态
