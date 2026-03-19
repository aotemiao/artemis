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
| `artemis-symphony-orchestrator` | 轮询、调度、协调、重试与 reconciliation |
| `artemis-symphony-start` | Spring Boot 启动、可选 HTTP API（GET /api/v1/state、POST /api/v1/refresh） |

## 运行

1. 在仓库根或工作目录放置 `WORKFLOW.md`（或复制 `WORKFLOW.md.example` 为 `WORKFLOW.md` 并填写 `tracker.project_slug`）。
2. 设置环境变量 `LINEAR_API_KEY`（或在 WORKFLOW.md 的 `tracker.api_key` 中写 `$LINEAR_API_KEY`）。
3. 从根项目编译并运行：

```bash
cd c:\Users\aotem\IdeaProjects\artemis
mvn compile -pl artemis-symphony-start -am
java -jar artemis-symphony/artemis-symphony-start/target/artemis-symphony-start-1.0-SNAPSHOT.jar
```

或指定 WORKFLOW 路径与 HTTP 端口：

```bash
java -jar artemis-symphony-start-1.0-SNAPSHOT.jar /path/to/WORKFLOW.md --server.port=8080
```

配置项可通过 `application.yml` 或 `symphony.workflow-path` 覆盖默认的 `./WORKFLOW.md`。

## 合规说明

- 实现覆盖 SPEC 第 18.1 节核心合规：Workflow 加载与动态重载、轮询与调度、Linear 客户端、工作区与 hooks、Codex 客户端、严格模板、重试与 reconciliation、结构化日志占位。
- 可选扩展：HTTP API（GET /api/v1/state、POST /api/v1/refresh）；WORKFLOW.md 热重载与 `server.port` 扩展未在本版完整实现，可后续按 SPEC 13.7 与 6.2 补全。
