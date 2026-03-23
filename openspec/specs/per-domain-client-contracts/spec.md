## ADDED Requirements

### Requirement: Client modules colocated with domains
每个业务领域 SHALL 在自身模块目录下提供一个 `*-client` 模块，用于对内暴露给其他内部 JVM 微服务的能力接口和数据契约（Dubbo 接口、DTO/Result 等），而不是使用顶层统一聚合的 `api` 目录。

#### Scenario: New domain exposes client module
- **WHEN** 新增一个业务领域模块（例如 `artemis-order`）
- **THEN** 该领域 SHALL 在 `artemis-modules/artemis-order` 下新增一个 `artemis-order-client` 模块，对外暴露订单相关能力的接口和 DTO

### Requirement: 内部服务调用方仅依赖 client 模块
其他内部 JVM 微服务 SHALL 仅依赖各业务领域的 `*-client` 模块来调用该领域能力，不得直接依赖该领域的 app/domain/infra 等实现细节模块。

#### Scenario: Internal service consumes system domain
- **WHEN** 另一个内部服务需要调用系统领域的用户能力
- **THEN** 该服务 SHALL 仅将 `artemis-system-client` 作为依赖引入，而不是直接依赖 `artemis-system-app`、`artemis-system-domain` 或 `artemis-system-infra`

### Requirement: 对外调用方使用 REST/OpenAPI 契约
网关、浏览器、第三方系统及其他非内部 Java 调用方 SHALL 通过业务领域 adapter 层暴露的 REST/OpenAPI 契约访问该领域能力，MUST NOT 直接依赖 `*-client` 模块作为跨边界集成方式。

#### Scenario: Gateway consumes system domain
- **WHEN** `artemis-gateway` 或外部系统需要访问系统领域对外能力
- **THEN** SHALL 通过系统服务公开的 REST API 或 OpenAPI 客户端调用，而不是引入 `artemis-system-client`
