## ADDED Requirements

### Requirement: Client modules colocated with domains
每个业务领域 SHALL 在自身模块目录下提供一个 `*-client` 模块，用于对外暴露该领域的能力接口和数据契约（Facade 接口、DTO/Result 等），而不是使用顶层统一聚合的 `api` 目录。

#### Scenario: New domain exposes client module
- **WHEN** 新增一个业务领域模块（例如 `artemis-order`）
- **THEN** 该领域 SHALL 在 `artemis-modules/artemis-order` 下新增一个 `artemis-order-client` 模块，对外暴露订单相关能力的接口和 DTO

### Requirement: Callers depend only on client modules
内部其他服务、BFF、网关及第三方集成方 SHALL 仅依赖各业务领域的 `*-client` 模块来调用该领域能力，不得直接依赖该领域的 app/domain/infra 等实现细节模块。

#### Scenario: Internal service consumes system domain
- **WHEN** 另一个内部服务需要调用系统领域的用户能力
- **THEN** 该服务 SHALL 仅将 `artemis-system-client` 作为依赖引入，而不是直接依赖 `artemis-system-app`、`artemis-system-domain` 或 `artemis-system-infra`
