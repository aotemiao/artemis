## Why

当前 `artemis-modules` 下的每个领域服务已经拥有 colocated `*-client` 契约模块，且 `per-domain-client-contracts` 已要求内部 JVM 调用方直接依赖这些 client。顶层 `artemis-api` bridge 只是再次转发 `*-client`，会增加一层模块边界，并让“契约到底归属领域还是归属聚合 API”变得模糊。

## What Changes

- 移除顶层 `artemis-api` 聚合模块、`artemis-api-bom` 与 `artemis-api-<domain>` bridge。
- 内部服务调用方统一依赖目标领域自身的 `*-client`。
- 根 BOM `artemis-dependencies` 继续统一管理 `*-client` 版本。
- 领域服务脚手架与服务目录守门改为生成和检查 colocated `*-client`。

## Capabilities

### Modified Capabilities

- `repository-structure`：顶层模块列表、内部契约位置、跨服务调用规则与 BOM 管理规则。

## Impact

- `artemis-auth` 依赖从 `artemis-api-system` 切换为 `artemis-system-client`。
- `scripts/dev/new-domain-service.sh` 不再生成 API bridge。
- `scripts/harness/check-service-catalog.sh` 不再检查 `artemis-api/<bridge>`，改为检查服务内 `*-client`。
- 不改变 Dubbo 接口签名、DTO 内容或 REST API。
