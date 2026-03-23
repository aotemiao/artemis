# Service Smoke Runbook

Status: maintained
Last Reviewed: 2026-03-23
Review Cadence: 90 days

本 runbook 用于把本地服务的启动顺序、等待方式和 smoke 命令固定下来，供开发者与 agent 共用。

## 推荐顺序

1. 启动基础设施：`scripts/dev/up.sh`
2. 启动系统服务：`scripts/dev/run-system.sh`
3. 启动认证服务：`scripts/dev/run-auth.sh`
4. 启动网关：`scripts/dev/run-gateway.sh`
5. 启动 Symphony：`scripts/dev/run-symphony.sh`

说明：

- `scripts/dev/run-symphony.sh` 默认会通过 `spring-boot.run.arguments` 打开 `9500` 端口，便于本地观测。
- 若需要自定义 Symphony 端口，可传入自己的 `-Dspring-boot.run.arguments=...` 覆盖默认值。

## 启动断言

- 等待任意 HTTP 端点：`scripts/dev/wait-http.sh <url> [expected_csv] [attempts] [sleep_seconds] [method]`
- 一般场景优先用默认重试次数；对慢启动服务可以临时把 `attempts` 调大

## 推荐 smoke

- 系统服务：`scripts/smoke/system-lookup.sh`
- 认证服务：`scripts/smoke/auth-refresh.sh`
- 网关路由：`scripts/smoke/gateway-auth-refresh.sh`
- Symphony 状态页：`scripts/smoke/symphony-state.sh`

## 常见组合

只验证系统链路：

```bash
scripts/smoke/system-lookup.sh
```

验证认证与网关：

```bash
scripts/smoke/auth-refresh.sh
scripts/smoke/gateway-auth-refresh.sh
```

验证 Symphony HTTP 可观测性：

```bash
scripts/smoke/symphony-state.sh
```
