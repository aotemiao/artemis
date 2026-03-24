# Artemis Symphony

基于 [OpenAI Symphony SPEC](https://github.com/openai/symphony/blob/main/SPEC.md) 的编码代理编排服务：从 Linear 拉取议题、按议题创建隔离工作区、在工作区内启动 Codex app-server 执行任务。

## 模块结构

| 模块 | 职责 |
|------|------|
| `artemis-symphony-core` | 领域模型（Issue、WorkflowDefinition、Workspace、RetryEntry 等）与工具类 |
| `artemis-symphony-config` | WORKFLOW.md 加载、YAML 前件解析、ServiceConfig 与严格模板渲染（Pebble） |
| `artemis-symphony-tracker` | Linear GraphQL 客户端（候选议题、按状态拉取、按 ID 刷新状态） |
| `artemis-symphony-workspace` | 按议题创建工作区、执行 hooks（after_create/before_run/after_run/before_remove） |
| `artemis-symphony-agent` | Codex app-server 子进程客户端（stdio JSON-RPC、initialize/thread/start/turn/start） |
| `artemis-symphony-orchestrator` | 轮询、调度、协调、重试与 reconciliation；运行时配置快照（`SymphonyRuntimeHolder`） |
| `artemis-symphony-start` | Spring Boot 启动、HTTP API、`WatchService` 工作流热重载 |

## 仓库内工作流资产

- `WORKFLOW.md.example`
  默认 workflow 模版，要求 agent 先读仓库入口文档
- `skills/`
  针对常见工程任务的最小 skill 资产
- `prompts/`
  自评与 reviewer handoff 等 prompt 模版

## 运行

1. 在仓库根或工作目录放置 `WORKFLOW.md`（或复制 `WORKFLOW.md.example` 为 `WORKFLOW.md` 并填写 `tracker.project_slug`）。
2. 设置环境变量 `LINEAR_API_KEY`（或在 WORKFLOW.md 的 `tracker.api_key` 中写 `$LINEAR_API_KEY`）。
3. 从根项目编译并运行：

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
| GET | `/api/v1/issues/{identifier}` | 按人类可读编号（如 `MT-649`）查询 running/retry 与推导的 `workspace_path`；未知返回 `404` + JSON `error`/`message` |

### 日志（MDC）

控制台 pattern 包含：`issue_id`、`issue_identifier`、`session_id`（未设置时为空段）。异步 worker 会复制/恢复 MDC，避免污染线程池。

由 `artemis-symphony-start` 中的 `logback-spring.xml` 配置。

## 合规说明

- 实现覆盖 SPEC 核心路径：Workflow 加载与运行期重载、轮询与调度、Linear 客户端、工作区与 hooks、Codex 客户端、严格模板、重试与 reconciliation、结构化日志与运维 HTTP。
- 可选扩展未纳入本仓库：`linear_graphql` 工具、SSH worker 等。
