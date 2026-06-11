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
| `artemis-symphony-persistence` | 本地 SQLite 运行历史，记录 run、Codex 事件、workspace、token 与失败原因 |
| `artemis-symphony-orchestrator` | 轮询、调度、协调、重试与 reconciliation；运行时配置快照（`SymphonyRuntimeHolder`），含 worker host 选择与复用 |
| `artemis-symphony-start` | Spring Boot 启动、HTTP API、`WatchService` 工作流热重载 |

## 仓库内工作流资产

- `WORKFLOW.md.example`
  默认 workflow 模版，要求 agent 先读仓库入口文档
- `skills/`
  针对常见工程任务的最小 skill 资产
- `prompts/`
  自评与 reviewer handoff 等 prompt 模版
- `tools/`
  动态工具 schema registry，记录工具名、输入输出 schema、权限等级和外部副作用边界
- `prompts/agent-requirement-intake.md`
  需求受理与分流模板
- `skills/spec-driven-delivery.md`
  业务需求 Spec、验收映射与执行计划联动的交付 skill
- `prompts/spec-driven-delivery.md`
  Spec 驱动交付 prompt 模板
- `skills/adversarial-review.md`
  独立 reviewer 复核模板，重点检查权限、幂等、事务、SQL、日志和可观测性
- `prompts/adversarial-review.md`
  adversarial review 输出格式和审查顺序

当前重点资产：

- `skills/new-domain-service.md`
- `skills/expand-existing-service.md`
- `skills/contract-change.md`
- `skills/deploy-drill.md`
- `skills/spec-driven-delivery.md`
- `prompts/self-review-and-handoff.md`
- `prompts/contract-change-review.md`
- `prompts/deploy-drill-report.md`
- `prompts/phase-delivery-plan.md`
- `prompts/agent-requirement-intake.md`
- `prompts/spec-driven-delivery.md`
- `skills/adversarial-review.md`
- `prompts/adversarial-review.md`
- `tools/registry.json`

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
- 运行历史默认写入当前运行目录的 `./symphony_runs.sqlite`；可用 `--symphony.history.sqlite-path=/path/to/symphony_runs.sqlite` 覆盖。
- 每次 agent attempt 收尾时默认输出低敏 JSON 摘要到 `artifacts/agent-runs/`；可用 workflow 中的 `reporting.agent_runs.enabled` 和 `reporting.agent_runs.directory` 调整。
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

### Spec 驱动交付配置

`WORKFLOW.md.example` 默认开启：

```yaml
delivery:
  spec_driven:
    enabled: true
```

开启后，Symphony 会在首轮 agent prompt 末尾自动追加 Spec-driven Delivery 上下文，要求 agent 判断是否需要 Feature Spec、补齐验收映射、在复杂任务中使用执行计划，并在最终 handoff 中说明验证证据。`delivery.spec_driven.required_assets` 是这段上下文引用的本地资产清单，默认覆盖 `docs/feature-specs/`、`docs/patterns/`、执行计划模板、Symphony prompt / skill 和对应 harness 检查脚本。

该能力不是新的规范事实来源：长期规则仍沉淀在 `openspec/specs/`，业务需求沉淀在 `docs/feature-specs/`，Symphony 只负责把这些资产注入执行现场并在状态接口暴露当前开关。

### WORKFLOW 热重载

- 默认监听 `WORKFLOW.md` 所在目录的文件变更（`symphony.workflow-watch.enabled=true`）。
- 防抖后重新加载；**成功**则替换内存快照并请求一轮立即 tick；**失败**打日志并保留上一份可用配置。
- 可调：`symphony.workflow-watch.debounce-ms`（默认 `400`）。

### HTTP API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 极简说明页，链向下方 JSON API 和运行历史页 |
| GET | `/runs` | 本地运行历史可视化页面，展示近期指标、状态分布、成功率、平均耗时、最近 run、worker、token 与事件 |
| GET | `/api/v1/state` | 运行中议题、重试队列、Codex 累计、delivery 配置等快照 |
| POST | `/api/v1/refresh` | `202 Accepted`，触发与 poll 等价的 reconcile/dispatch 路径；`coalesced=true` 表示与已在排队的立即 tick 合并 |
| GET | `/api/v1/issues/{identifier}` | 按人类可读编号（如 `MT-649`）查询 running/retry 与推导的 `workspace_path`；未知返回 `404` + JSON `error.code` / `error.message` |
| GET | `/api/v1/history/runs?limit=50` | 最近运行历史，按 `updated_at` 倒序返回，`limit` 上限为 500 |
| GET | `/api/v1/history/runs/{runId}/events?limit=200` | 单个 run 的事件流，按事件时间升序返回，`limit` 上限为 500 |
| GET | `/api/v1/history/metrics?limit=100` | 最近运行指标摘要，返回状态分布、重试数、token 汇总、平均耗时和时间窗口，`limit` 上限为 500 |

### SQLite 运行历史

- 默认 SQLite 文件为 `./symphony_runs.sqlite`，相关 WAL / SHM 文件已加入 `.gitignore`。
- 写入是 best-effort：数据库初始化或写入失败只会打告警，不会单独中断当前 worker attempt。
- 记录内容包括 issue id / identifier、tracker state、attempt、worker host、workspace path、Codex thread/session、token 汇总、失败原因以及 started / updated / finished 时间。
- 每个 run 的事件流会包含一个幂等的 `run_started` 事件；即使 Todo 自动认领导致 run 记录被刷新，也不会重复写开始事件。
- 当 reconcile 因终态、失去路由、非 active、tracker 不可见或 stall timeout 终止运行中 attempt 时，历史记录会以 `terminated` 收尾并写入具体原因，避免 `/runs` 长期显示假 running。
- Symphony 启动时会把上一次进程退出遗留的 `running` 记录标记为 `interrupted`，并追加 `run_interrupted` 事件，便于区分真实运行中任务和重启恢复记录。
- 运行指标摘要从最近 N 条本机历史记录实时汇总，覆盖状态分布、成功率、重试数、token 和平均耗时，不引入额外外部存储。
- 事件 payload 会截断到固定长度，适合本地排障，不应作为长期审计或敏感数据仓库。

### Agent Run 摘要

- 默认开启 `reporting.agent_runs.enabled: true`，每次 worker attempt 结束时写入一个 JSON 文件到 `artifacts/agent-runs/`。
- 摘要包含 run id、issue id / identifier / title、attempt、状态、失败原因、worker host、`workspace/<key>` 低敏引用、Codex session、token、重试计划、低敏运行环境、外部写回标记和本次 Codex approval / sandbox 权限快照。
- 权限快照会记录 `approval_policy`、effective `thread_sandbox`、解析后的 `turn_sandbox_policy`、`network_access`、`network_access_reason`、低敏引用后的 `writable_roots` / `allowed_writable_roots`、`danger_full_access_allowed` 与是否为远端 worker，便于复盘每次 run 的文件系统和网络边界。
- 环境快照会记录 Java runtime、Maven 版本占位、OS、CPU 架构、可用处理器数量和 Spring profile，不记录用户名、home 目录、PATH、完整环境变量或本地仓库路径。
- 摘要不写入完整 prompt、聊天记录、工具输出全文、密钥或外部系统响应全文；该目录默认被 `.gitignore` 忽略，需要沉淀的人工复盘应整理到 `docs/reports/agent-runs/`。
- 写入失败只会记录告警，不会改变 worker 收尾、重试或 tracker 回写路径。

### 权限 Preflight

- 每次 worker attempt 在创建 workspace 后、启动 Codex 前执行权限预检。
- 默认只允许 `turn_sandbox_policy.writableRoots` 指向当前 issue workspace；额外可写目录必须配置 `permissions.allowed_writable_roots`。
- 当 `turn_sandbox_policy.networkAccess: true` 时，必须配置 `permissions.network_access_reason`，该原因会进入 run summary。
- `codex.thread_sandbox: danger-full-access`、旧配置 `thread_sandbox: none` 或 `turn_sandbox_policy.type: dangerFullAccess` 必须显式配置 `permissions.allow_danger_full_access: true`。
- 权限预检失败会按本次 attempt failure 收尾：不启动 Codex app-server，会执行 `after_run`，并进入既有运行历史、低敏摘要和重试路径。

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
- 动态工具事实源为 `artemis-symphony/tools/registry.json`；该注册表声明工具输入 / 输出 schema、权限等级、是否允许外部写操作、是否允许无人值守和稳定失败码。
- `linear_graphql` 既支持 query 也支持 mutation，因此注册表必须显式标注 `external_write_allowed: true`，避免把外部写能力误读为只读诊断工具。
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
