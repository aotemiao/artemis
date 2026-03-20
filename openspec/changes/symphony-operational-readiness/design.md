# Design: Symphony 可用、可运维基线

## Context

- `artemis-symphony` 以 Spring Boot 启动，`SymphonyBootstrap` 在启动时加载一次 `WORKFLOW.md` 并构造不可变 `ServiceConfig`；`Orchestrator` 在单线程调度器上执行 tick。
- HTTP 层已有 `GET /api/v1/state` 与 `POST /api/v1/refresh` 占位；orchestrator 未暴露「立即执行一轮」的显式 API。
- 日志使用默认 SLF4J，未统一 MDC。

## Goals / Non-Goals

**Goals:**

- 运行时可安全重载 WORKFLOW（成功则更新 poll 间隔、模板、tracker 配置等；失败不崩溃）。
- `POST /api/v1/refresh` 触发与 tick 等价的 reconcile +（在验证通过时）dispatch 路径，支持简单合并以防风暴。
- `GET /api/v1/issues/{identifier}` 返回 SPEC 建议形状的超集或子集，未知 issue 返回 404 + JSON 错误信封。
- 关键路径日志带 `issue_id`、`issue_identifier`、`session_id`（有则设置，无则省略或 `-` 策略在实现中统一）。
- 根 README 与 `repository-structure` 与 `pom.xml` 对齐。

**Non-Goals:**

- 完整 HTML 仪表盘、多租户控制面、持久化 orchestrator 状态。
- 修改 Nacos/网关路由将 Symphony 纳入主微服务网格（可后续单独变更）。
- 实现 SPEC 可选扩展 `linear_graphql` 工具、SSH worker。

## Decisions

1. **配置可重载载体**  
   - 使用 `AtomicReference<EffectiveWorkflow>`（或等价）持有当前 `WorkflowDefinition` + `ServiceConfig` + 可选版本号/时间戳；监听线程只更新引用，orchestrator 每 tick 或每次 dispatch 前读取最新引用（与 SPEC「defensive reload」一致）。  
   - **备选**：仅监听文件事件后在主线程重载 — 实现简单但需避免与 tick 死锁；采用引用替换 + orchestrator 读快照更简单。

2. **文件监听**  
   - 使用 `java.nio.file.WatchService` 监听 `WORKFLOW.md` 所在目录的 `ENTRY_MODIFY`（及必要时 `CREATE`），防抖（例如 300–500ms）后重新 `WorkflowLoader.load`。  
   - **备选**：Spring `@Scheduled` 轮询 `Files.getLastModifiedTime` — 实现更简单，略增 IO；若 WatchService 在目标 OS 上行为不稳可回退。

3. **Orchestrator 与 refresh**  
   - 在 `Orchestrator` 增加 `requestImmediateTick()`（或 `enqueuePoll()`）：设置 `volatile boolean` 或 `AtomicBoolean`，调度线程在下次 `schedule` 前若标志为 true 则立即执行 `onTick` 并清除标志；`POST /refresh` 仅置位并返回 202。  
   - **合并**：若短时间内多次 refresh，只保留一次 pending tick（coalesced=true 可在响应体体现，与 SPEC 示例一致）。

4. **Issue 详情 API**  
   - 从 orchestrator 内存态组装：`running`、`retry`、workspace path（由 `WorkspaceKeys` + root 推导）、recent_events 可为空列表或后续迭代填充。  
   - `identifier` 与内部 map key：按 SPEC 用 `issue_identifier` 匹配 running/retry 条目。

5. **日志**  
   - 使用 SLF4J MDC：`MDC.put("issue_id", ...)` 等在 worker 入口、`onCodexUpdate`、reconcile 终止路径设置，`finally` 中 `MDC.remove`。  
   - Logback pattern 可在 `artemis-symphony-start` 的 `logback-spring.xml`（若不存在则新增）中增加 `%X{issue_id}` 等占位。

6. **测试**  
   - 优先：`WorkflowLoader` + 重载协调器的单元测试；`refresh` Controller 使用 `@WebMvcTest` + mock bean；可选 Testcontainers 不引入。

## Risks / Trade-offs

- **[Risk]** WatchService 在部分环境对「同一文件保存」产生多次事件 → **缓解**：防抖 + 比较内容 hash 或 lastModified。  
- **[Risk]** 热重载与正在运行的 agent 会话配置不一致 → **缓解**：SPEC 允许不自动重启 in-flight；文档说明。  
- **[Risk]** MDC 在异步 worker 线程未传递 → **缓解**：在 `ExecutorService` 提交任务时复制 MDC 到子线程（若采用线程池需 `MdcTaskDecorator`）。

## Migration Plan

- 无数据迁移；部署后仅需保证 `WORKFLOW.md` 路径与监听目录可写可读。  
- 回滚：还原代码版本；配置仍兼容旧行为。

## Open Questions

- 是否在首版即为 `WatchService` 单独起守护线程，还是挂在 Spring 生命周期 `SmartLifecycle`？  
- `GET /api/v1/issues/{id}` 的 path 变量名 SPEC 示例为 identifier：是否同时支持 `issue_id`（Linear UUID）查询？首版可仅支持 `identifier`（如 `MT-649`）。
