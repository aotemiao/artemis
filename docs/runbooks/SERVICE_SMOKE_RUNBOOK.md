# Service Smoke Runbook

Status: maintained
Last Reviewed: 2026-03-25
Review Cadence: 90 days

本 runbook 用于把本地服务的启动顺序、等待方式和 smoke 命令固定下来，供开发者与 agent 共用。

## 推荐顺序

### 一键启动

本地核心链路可直接执行：

```bash
scripts/dev/start-all.sh
```

该入口会通过 `docker/docker-compose.yml` 启动 PostgreSQL、Redis、Nacos、Nacos 配置加载器、`system auth gateway`，并在需要时构建本地镜像；容器启动后会复用 `check-service-readiness.sh` 做主机侧就绪检查。停止 Compose 服务：

```bash
scripts/dev/stop-all.sh
```

### 手动启动

1. 启动基础设施：`scripts/dev/up.sh`
2. 上传 Nacos 配置模板：`scripts/dev/upload-nacos-configs.sh`
3. 启动系统服务：`scripts/dev/run-system.sh`
4. 启动认证服务：`scripts/dev/run-auth.sh`
5. 启动网关：`scripts/dev/run-gateway.sh`
6. 启动 Symphony：`scripts/dev/run-symphony.sh`

说明：

- `scripts/dev/run-symphony.sh` 默认会通过 `spring-boot.run.arguments` 打开 `9500` 端口，便于本地观测。
- 若需要自定义 Symphony 端口，可直接追加 `--server.port=...`；旧写法 `-Dspring-boot.run.arguments=...` 仍兼容。
- 启动前若不确定 workflow 是否已准备好，可先执行 `scripts/dev/check-service-config.sh symphony`。
- Compose 内的 Nacos 配置加载器默认上传并读取 public namespace，避免首次本地启动还需要先创建 `dev` namespace。
- `scripts/dev/start-all.sh --full` 会通过 Compose `full` profile 额外启动 resource/workflow；新增领域服务应先补齐 service catalog、Nacos 模板、Dockerfile 和 smoke。

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
