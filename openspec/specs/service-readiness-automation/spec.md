## ADDED Requirements

### Requirement: 启动就绪自动检查

仓库 SHALL 提供面向本地服务启动的自动检查脚本，至少覆盖以下失败模式：

- 配置模板缺失
- HTTP 端点在超时时间内未就绪
- 启动失败后无法快速定位日志入口

该脚本 SHALL 支持按服务名执行，并输出清晰的失败原因与下一步排查入口。

#### Scenario: 配置缺失导致启动前失败

- **WHEN** 执行服务就绪检查时，服务依赖的 `config/nacos` 模板文件不存在
- **THEN** 脚本 SHALL 立即失败并指出缺失文件，而不是继续等待 HTTP 超时

#### Scenario: 慢启动触发超时

- **WHEN** 服务在限定重试次数内未返回期望 HTTP 状态
- **THEN** 脚本 SHALL 输出等待超时信息，并给出关联日志文件或日志脚本入口

### Requirement: 聚合 Smoke 回路

仓库 SHALL 提供一个可复用的聚合 smoke 入口，用于顺序执行各关键服务 smoke，并在单个服务失败时立即停止。该入口 SHALL 复用已有 `scripts/smoke/*.sh` 子脚本，而不是重复实现 HTTP 断言。

#### Scenario: 顺序执行关键服务 smoke

- **WHEN** 开发者或 agent 需要一次性验证 `system`、`auth`、`gateway`、`symphony`
- **THEN** SHALL 能通过一个仓库脚本按固定顺序运行全部 smoke，并获得统一的成功/失败输出
