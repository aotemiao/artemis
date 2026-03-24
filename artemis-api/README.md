# Artemis API

Status: maintained
Last Reviewed: 2026-03-24
Review Cadence: 90 days

`artemis-api` 用于给内部调用方提供统一的契约入口，而不是承载任何业务实现。

当前职责：

- `artemis-api-bom`
  统一管理内部客户端与 API bridge 的版本
- `artemis-api-system`
  聚合 `artemis-system-client`，供调用方通过单一入口依赖系统域契约

设计约束：

- `artemis-api-*` 只能依赖 `*-client`
- 不能依赖 `adapter / app / domain / infra / start`
- 新增领域服务时，优先补对应 `artemis-api-<domain>`，再让调用方接入
