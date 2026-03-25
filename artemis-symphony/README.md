# Artemis Symphony

基于 [OpenAI Symphony SPEC](https://github.com/openai/symphony/blob/main/SPEC.md) 的编码代理编排服务：从 tracker 拉取议题、按议题创建隔离工作区、在工作区内启动 Codex app-server 执行任务，并可选地把尝试结果评论回写到 Linear。

## 模块结构

| 模块 | 职责 |
|------|------|
| `artemis-symphony-core` | 领域模型（Issue、WorkflowDefinition、Workspace、RetryEntry 等）与工具类 |
| `artemis-symphony-config` | WORKFLOW.md 加载、YAML 前件解析、ServiceConfig 与严格模板渲染（Pebble） |
| `artemis-symphony-tracker` | tracker 适配层（Linear GraphQL / memory；候选议题、按状态拉取、按 ID 刷新、状态写回、assignee 路由、可选 issue 评论写回） |
| `artemis-symphony-workspace` | 按议题创建工作区、执行 hooks（after_create/before_run/after_run/before_remove），同时支持本地与 SSH worker 工作区 |
| `artemis-symphony-agent` | Codex app-server 子进程客户端（stdio JSON-RPC、initialize/thread/start/turn/start，本地或 SSH worker 均可） |
| `artemis-symphony-orchestrator` | 轮询、调度、协调、重试与 reconciliation；运行时配置快照（`SymphonyRuntimeHolder`），含 worker host 选择与复用 |
| `artemis-symphony-start` | Spring Boot 启动、HTTP API、`WatchService` 工作流热重载 |

## 仓库内工作流资产

- `WORKFLOW.md.example`
  默认 workflow 模版，要求 agent 先读仓库入口文档
- `skills/`
  针对常见工程任务的最小 skill 资产
- `prompts/`
  自评与 reviewer handoff 等 prompt 模版

当前重点资产：

- `skills/new-domain-service.md`
- `skills/expand-existing-service.md`
- `skills/contract-change.md`
- `skills/deploy-drill.md`
- `prompts/self-review-and-handoff.md`
- `prompts/contract-change-review.md`
- `prompts/deploy-drill-report.md`
- `prompts/phase-delivery-plan.md`

## 运行

推荐的本地入口：

1. 在仓库根或工作目录放置 `WORKFLOW.md`（或复制 `WORKFLOW.md.example` 为 `WORKFLOW.md` 并填写 `tracker.project_slug`）。
2. 设置环境变量 `LINEAR_API_KEY`（或在 WORKFLOW.md 的 `tracker.api_key` 中写 `$LINEAR_API_KEY`）。
3. 若需要按 assignee 路由，可再设置 `LINEAR_ASSIGNEE`，并在 workflow 中配置 `tracker.assignee: $LINEAR_ASSIGNEE`。
4. 从仓库根执行：

```bash
scripts/dev/run-symphony.sh
```

说明：

- 脚本默认会追加 `--server.port=9500`，便于本地直接访问状态页。
- 若使用非默认 workflow 路径，可先设置 `SYMPHONY_WORKFLOW_PATH=/path/to/WORKFLOW.md`，或直接传 `--symphony.workflow-path=...`。
- 若需要自定义端口，可直接传 `--server.port=8080`；也兼容 `-Dspring-boot.run.arguments=...` 旧写法。
- 若希望在 issue 评论中看到 Symphony 的尝试摘要，可在 workflow 中开启 `reporting.linear_comments.enabled: true`。

也可以从根项目编译并运行：

```bash
cd /path/to/artemis
mvn compile -pl artemis-symphony/artemis-symphony-start -am
java -jar artemis-symphony/artemis-symphony-start/target/artemis-symphony-start-1.0-SNAPSHOT.jar
```

或指定 WORKFLOW 路径与 HTTP 端口：

```bash
java -jar artemis-symphony-start-1.0-SNAPSHOT.jar --symphony.workflow-path=/path/to/WORKFLOW.md --server.port=8080
```

配置项可通过 `application.yml` 或 `symphony.workflow-path` 覆盖默认的 `./WORKFLOW.md`。

## 真实 Live E2E

仓库现在提供一条与官方 `openai/symphony` 参考实现同风格的真实 live e2e 演练入口。它会在显式开启时：

- 创建临时 Linear project 与 issue
- 真实运行 `codex app-server`
- 验证 workspace 副作用文件
- 验证 agent 通过 `linear_graphql` 写评论
- 验证 issue 进入完成态
- 最后把临时 project 标记为完成

推荐入口：

```bash
scripts/e2e/run-symphony-live-e2e.sh
```

前置条件：

- 已设置 `LINEAR_API_KEY`
- 本机可执行 `codex`
- 若未设置 `SYMPHONY_LIVE_SSH_WORKER_HOSTS`，则本机还需要：
  - `docker compose` 或 `docker-compose`
  - `ssh-keygen`
  - `~/.codex/auth.json`
  - docker fallback worker 会以 `privileged` 方式启动，以便在 macOS / OrbStack 这类环境下仍能保留默认 `workspace-write` / `workspaceWrite` sandbox 语义

可选环境变量：

- `SYMPHONY_LIVE_LINEAR_TEAM_KEY`：默认 `SYME2E`
- `SYMPHONY_LIVE_SSH_WORKER_HOSTS`：逗号分隔的真实 SSH worker 列表；未设置时会使用 `artemis-symphony/test-support/live-e2e-docker/` 中的 docker fallback worker
- `SYMPHONY_LIVE_E2E_KEEP_ARTIFACTS=1`：保留 live e2e 的本地调试副本与 docker SSH worker，便于在 `artemis-symphony-start/target/live-e2e-debug/` 下排查失败现场

实现位置：

- live e2e 测试：`artemis-symphony-start/src/test/java/com/aotemiao/artemis/symphony/live/`
- docker SSH worker 支撑：`artemis-symphony/test-support/live-e2e-docker/`

## 运维与可观测性

### WORKFLOW 热重载

- 默认监听 `WORKFLOW.md` 所在目录的文件变更（`symphony.workflow-watch.enabled=true`）。
- 防抖后重新加载；**成功**则替换内存快照并请求一轮立即 tick；**失败**打日志并保留上一份可用配置。
- 可调：`symphony.workflow-watch.debounce-ms`（默认 `400`）。

### HTTP API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 极简说明页，链向下方 JSON API |
| GET | `/api/v1/state` | 运行中议题、重试队列、Codex 累计等快照 |
| POST | `/api/v1/refresh` | `202 Accepted`，触发与 poll 等价的 reconcile/dispatch 路径；`coalesced=true` 表示与已在排队的立即 tick 合并 |
| GET | `/api/v1/issues/{identifier}` | 按人类可读编号（如 `MT-649`）查询 running/retry 与推导的 `workspace_path`；未知返回 `404` + JSON `error.code` / `error.message` |

### Linear 进度评论回写

- 默认关闭；在 `WORKFLOW.md` 中设置 `reporting.linear_comments.enabled: true` 后生效。
- 开启后，Symphony 会在每次 worker 尝试结束时向对应 Linear issue 写入一条评论摘要。
- 可选配置：
  - `reporting.linear_comments.success_template`
  - `reporting.linear_comments.failure_template`
  - `reporting.linear_comments.issue_title_regex`
- 模板上下文至少包含：
  - `issue`
  - `attempt`
  - `workspace`
  - `usage`
  - `retry`
- 评论渲染或 Linear API 写回失败只会记录告警，不会中断当前 worker 收尾或后续调度。

最小示例：

```yaml
reporting:
  linear_comments:
    enabled: true
    issue_title_regex: ^进度汇报$
```

建议验证方式：

1. 在当前 `tracker.project_slug` 指向的 Linear project 下创建一个 `Todo` 或 `In Progress` issue。
2. 运行 `scripts/dev/run-symphony.sh`，或在服务已运行时调用 `POST /api/v1/refresh`。
3. 观察该 issue 的评论区是否出现 Symphony 写入的尝试摘要。

### Todo 自动认领与 Linear 动态工具

- 当 Symphony 开始执行一个 `Todo` issue 时，会先尝试把它推进到 `In Progress`。
- 当 workflow 配置了 `tracker.assignee` 时，Symphony 会保留 assignee 信息，并只调度仍然路由到当前 worker 的 issue；若运行中的 issue 失去该路由，会在 reconcile 时停止。
- 当 tracker 为 `linear` 时，Symphony 会在 `thread/start` 中注入 `linear_graphql` 动态工具，供 Codex 在当前会话里直接执行 Linear GraphQL。
- 若状态认领失败，Symphony 会记录告警并继续本轮 worker；若动态工具执行失败，会把失败结果回传给 app-server，而不是直接卡死 turn。

### SSH Worker

- 当 workflow 配置 `worker.ssh_hosts` 后，Symphony 会通过本机 `ssh` 命令把 issue 分发到远端 worker。
- 若同时配置 `worker.max_concurrent_agents_per_host`，编排器会按当前每台 host 的运行中 agent 数量做限流，并优先选择负载最低的 host。
- issue 进入重试时会优先复用上一次的 worker host；若该 host 已无容量，再回退到其他可用 host。
- 远端模式下，workspace 创建、hook 执行、Codex app-server 启动与 workspace 清理都会通过 SSH 进行。
- 若真实 SSH worker 使用默认 `workspace-write` / `workspaceWrite` sandbox，请确保远端环境允许 Codex 所需的 user namespace / bwrap 能力；否则应在 workflow 中显式调整 `codex.thread_sandbox` 或 `codex.turn_sandbox_policy`。
- 可选环境变量：
  - `SYMPHONY_SSH_CONFIG`：额外传给 `ssh -F`
  - `SYMPHONY_SSH_EXECUTABLE`：覆盖默认 `ssh` 可执行文件

### Codex 会话默认值

- workflow 未显式配置时，`codex.approval_policy` 默认会对齐参考实现的 map 形式 `reject` 策略；若需要完全无人值守，也可以显式写成 `never`。
- 当前默认 `codex.thread_sandbox=workspace-write`；旧配置中的 `thread_sandbox: none` 仍会兼容映射为 `danger-full-access`。
- 当未显式配置 `codex.turn_sandbox_policy` 时，Symphony 会自动生成一个指向当前 issue workspace 的默认 `workspaceWrite` policy。
- 若需要显式配置，请使用当前 app-server 支持的取值：
  - `codex.approval_policy`: `untrusted` / `on-failure` / `on-request` / `never`，或参考实现使用的对象/map 结构
  - `codex.thread_sandbox`: `read-only` / `workspace-write` / `danger-full-access`
- 为兼容旧 workflow，`approval_policy: auto` 会自动映射为 `never`，`thread_sandbox: none` 会自动映射为 `danger-full-access`。

### 默认 workspace 根

- workflow 未显式配置 `workspace.root` 时，Symphony 会默认使用系统临时目录下的 `symphony_workspaces`，与参考实现保持一致。
- 若配置了 `worker.ssh_hosts`，远端 worker 仍会使用 workflow 中写明的 `workspace.root` 字面值，通过 SSH 在远端解析 `~`、相对路径与实际绝对路径。

### 日志（MDC）

控制台 pattern 包含：`issue_id`、`issue_identifier`、`session_id`（未设置时为空段）。异步 worker 会复制/恢复 MDC，避免污染线程池。

由 `artemis-symphony-start` 中的 `logback-spring.xml` 配置。

## 合规说明

- 实现覆盖 SPEC 核心路径：Workflow 加载与运行期重载、轮询与调度、Linear 客户端、工作区与 hooks、Codex 客户端、严格模板、重试与 reconciliation、结构化日志与运维 HTTP。
- 与参考实现的关键运行时语义已对齐：memory tracker、Linear 状态写回、assignee 路由、SSH worker、dynamic tool、approval request 处理、turn sandbox 默认值。
