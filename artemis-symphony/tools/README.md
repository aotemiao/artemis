# Symphony Tool Registry

Status: maintained
Last Reviewed: 2026-06-10
Review Cadence: 90 days

本目录记录 Symphony 暴露给 Codex app-server 的动态工具。注册表用于让工具名、输入输出 schema、权限等级、外部副作用和失败语义有一个可审计事实源，减少只依赖 prompt 自然语言描述的漂移。

## 当前文件

- `registry.json`
  机器可读工具注册表。新增动态工具时，先更新该文件，再补实现、测试和治理检查。

## 维护规则

- 工具名必须与运行时代码中的动态工具名一致。
- `status` 只能使用 `active`、`planned` 或 `deprecated`。
- `availability.tracker_kind` 和 `availability.worker_scope` 必须明确说明工具在哪类 tracker 与 worker 范围内可用。
- 输入 schema 必须使用 object schema，并覆盖 app-server 会收到的参数结构。
- 输出 schema 必须使用 object schema，并至少声明 `success:boolean`、`output:string`、`contentItems:array`，让成功与失败结果保持稳定外形。
- 具备外部写能力的工具必须显式标注 `external_write_allowed: true`，不能伪装成只读工具。
- 审计字段必须声明 `tool_call_completed` 与 `tool_call_failed` 运行历史事件、外部副作用类型和低敏摘要策略。
- 失败语义必须声明 `retryable`、`turn_failure_on_error` 和非空且不重复的稳定错误码；运行时新增错误码时必须同步注册表。
- 注册表不得包含 token、外部响应全文、用户私密上下文或真实请求样例。

## 验证入口

```bash
mvn -pl artemis-symphony/artemis-symphony-orchestrator test -Dtest=SymphonyToolRegistryTest
```
