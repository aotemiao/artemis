## 1. 可重载配置与 Orchestrator 挂钩

- [x] 1.1 引入 `AtomicReference`（或等价）持有当前 `ServiceConfig`/`WorkflowDefinition` 快照，供 Orchestrator、WorkspaceManager、Tracker、AgentRunner 在运行期读取
- [x] 1.2 重构 `SymphonyBootstrap`：将「单次 Bean 构造」改为可注入「配置持有者」，首次加载与后续重载共用 `WorkflowLoader` + `DispatchPreflight` 语义
- [x] 1.3 在 `Orchestrator` 增加 `requestImmediateTick()`（或等价）及 coalesce 标志，供 HTTP refresh 调用

## 2. WORKFLOW.md 监听与重载

- [x] 2.1 实现 `WatchService`（或设计文档备选方案）监听工作流文件，防抖后触发重载
- [x] 2.2 重载成功则替换快照；失败则打错误日志并保留 last-known-good
- [x] 2.3 为合法/非法重载各增加至少一个单元测试（或集成测试）

## 3. HTTP 运维接口

- [x] 3.1 实现 `POST /api/v1/refresh` 置位立即 tick，响应 202 与 `queued`/`coalesced`/`requested_at`/`operations`
- [x] 3.2 实现 `GET /api/v1/issues/{identifier}`，404 使用 JSON 错误信封
- [x] 3.3 （可选）`GET /` 返回极简 HTML 或说明文档链接，指向 `/api/v1/state`

## 4. 结构化日志

- [x] 4.1 在 worker dispatch、Codex 回调、reconcile 终止路径设置/清理 MDC（`issue_id`、`issue_identifier`、`session_id`）
- [x] 4.2 在 `artemis-symphony-start` 配置 logback pattern 包含 MDC 键（若尚无专用 logback 文件则新增）

## 5. 文档与主 spec 归档准备

- [x] 5.1 更新根 `README.md` 模块树，加入 `artemis-symphony` 与一句话说明
- [x] 5.2 更新 `artemis-symphony/README.md` 的运维段落（热重载、refresh API、日志字段）
- [x] 5.3 变更归档前将 `specs/repository-structure` delta 合并入 `openspec/specs/repository-structure/spec.md`（随 `/opsx-archive` 或手动同步）

## 6. 验证

- [x] 6.1 `mvn compile -pl artemis-symphony/artemis-symphony-start -am` 通过
- [x] 6.2 新增/既有测试 `mvn test -pl artemis-symphony-start -am`（或限定模块）通过
