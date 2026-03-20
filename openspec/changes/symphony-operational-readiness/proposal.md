# Proposal: 把 Symphony 往「可用、可运维」推

## Why

`artemis-symphony` 已实现核心编排骨架，但缺少运维侧关键能力：WORKFLOW 变更需重启才能生效、HTTP 控制面不完整（如 refresh 未真正触发调度）、日志未稳定携带 issue/session 上下文，且根文档与 `repository-structure` 未反映该顶层模块。上述缺口会阻碍排障、灰度调参与团队协作，需要在一次变更中补齐「可运维」基线。

## What Changes

- **WORKFLOW.md 热重载**：监听工作流文件变更，成功则重载配置与模板；失败保留 last-known-good 并打错误日志（对齐 Symphony SPEC 6.2）。
- **HTTP 运维接口**：`POST /api/v1/refresh` 真正触发一次 poll + reconcile（可合并重复请求）；补充 `GET /api/v1/issues/{identifier}`（404 语义与 SPEC 13.7.2 一致）；可选极简 HTML 或指向 JSON 的 `/` 占位说明（不强制完整仪表盘）。
- **结构化日志**：在 issue 相关与 session 相关路径使用 MDC 或等价机制，稳定输出 `issue_id`、`issue_identifier`、`session_id`（SPEC 13.1）；避免记录密钥。
- **文档与仓库规范**：根 `README.md` 模块树增加 `artemis-symphony`；`openspec/specs/repository-structure` 将 `artemis-symphony` 纳入顶层固定模块列表及对应 scenario。
- **轻量测试**：至少覆盖 workflow 重载 happy path、refresh 端点行为（Mock 或集成测试二选一，在 tasks 中明确）。

## Capabilities

### New Capabilities

- `symphony-workflow-reload`：工作流文件监听、重载语义、失败降级与 last-known-good。
- `symphony-http-operations`：刷新触发、按 identifier 查询 issue 运维详情、错误 JSON 信封、与 orchestrator 的集成点。
- `symphony-structured-logging`：issue/session 上下文字段约定与关键路径打点。

### Modified Capabilities

- `repository-structure`：顶层模块列表与 scenario 增补 `artemis-symphony`，与根 `pom.xml` 一致。

## Impact

- **代码**：`artemis-symphony-start`（Spring 配置、Controller、可能 `WatchService` 或调度线程协作）、`artemis-symphony-orchestrator`（暴露「立即 tick」或队列）、`artemis-symphony-config`（可重载的 `ServiceConfig` 快照）。
- **文档**：根 `README.md`、`artemis-symphony/README.md` 可补充运维说明。
- **依赖**：优先使用 JDK/`java.nio.file` 监听，避免不必要的新第三方依赖。
