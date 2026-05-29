# Service Smoke Runbook

Status: maintained
Last Reviewed: 2026-03-25
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
- 若需要自定义 Symphony 端口，可直接追加 `--server.port=...`；旧写法 `-Dspring-boot.run.arguments=...` 仍兼容。
- 启动前若不确定 workflow 是否已准备好，可先执行 `scripts/dev/check-service-config.sh symphony`。

## 启动断言

- 等待任意 HTTP 端点：`scripts/dev/wait-http.sh <url> [expected_csv] [attempts] [sleep_seconds] [method]`
- 检查服务配置模板：`scripts/dev/check-service-config.sh <system|auth|gateway|symphony>`
- 检查服务就绪：`scripts/dev/check-service-readiness.sh <system|auth|gateway|symphony> [attempts] [sleep_seconds]`
- 一般场景优先用默认重试次数；对慢启动服务可以临时把 `attempts` 调大

## 推荐 smoke

- 系统服务：`scripts/smoke/system-lookup.sh`
- 认证服务：`scripts/smoke/auth-refresh.sh`
- 网关路由：`scripts/smoke/gateway-auth-refresh.sh`
- 网关最小 RBAC：`scripts/smoke/gateway-system-admin.sh`
- Symphony 状态页：`scripts/smoke/symphony-state.sh`
- 聚合 smoke：`scripts/smoke/all-services.sh`

## 常见组合

只验证系统链路：

```bash
scripts/smoke/system-lookup.sh
```

验证认证与网关：

```bash
scripts/smoke/auth-refresh.sh
scripts/smoke/gateway-auth-refresh.sh
scripts/smoke/gateway-system-admin.sh
```

验证 Symphony HTTP 可观测性：

```bash
scripts/smoke/symphony-state.sh
```

一次性串行验证全部关键服务：

```bash
scripts/smoke/all-services.sh
```
