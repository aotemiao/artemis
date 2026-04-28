## MODIFIED Requirements

### Requirement: Maven 多模块顶层聚合结构

根 POM（`com.aotemiao:artemis`）SHALL 作为聚合 POM，packaging 类型为 `pom`，声明所有顶层模块。顶层模块 SHALL 包含以下固定成员：

- `artemis-dependencies`：BOM 模块
- `artemis-framework`：公共能力聚合模块
- `artemis-gateway`：API 网关
- `artemis-auth`：认证授权服务
- `artemis-modules`：业务微服务聚合模块
- `artemis-visual`：运维基础设施聚合模块
- `artemis-symphony`：编码代理编排服务（Linear + WORKFLOW.md + Codex app-server，独立可运行；详见 `artemis-symphony/README.md`）

#### Scenario: 根 POM 模块声明完整

- **WHEN** 开发者查看根 `pom.xml` 的 `<modules>` 节
- **THEN** SHALL 包含上述固定顶层模块列表（含 `artemis-symphony`）
- **AND** MUST NOT 包含 `artemis-api`

### Requirement: 内部客户端契约随领域服务放置

内部微服务之间的 Java 调用契约 SHALL 放在各业务领域自身目录下的 `*-client` 模块中。仓库 MUST NOT 再通过顶层 `artemis-api` 聚合 POM、`artemis-api-bom` 或 `artemis-api-<domain>` bridge 模块转发这些契约。

`*-client` 模块 MUST NOT 依赖对应服务的 `adapter`、`app`、`domain`、`infra` 或 `start`。其职责是承载 Dubbo 接口、跨服务 DTO/Result 与 `CLIENT_CONTRACT.md`，不承载服务实现。

#### Scenario: 系统领域提供 colocated client 契约

- **WHEN** 查看 `artemis-modules/artemis-system` 目录
- **THEN** SHALL 包含 `artemis-system-client`
- **AND** 内部调用方 SHALL 直接依赖 `artemis-system-client`

### Requirement: 跨服务调用区分内部契约与对外 REST API

内部微服务之间的调用 SHALL 通过各业务领域 colocated 的 `*-client` 模块暴露契约。`*-client` 模块 SHALL 承载供内部调用的 Dubbo 接口与 DTO/Result；调用方 SHALL 仅依赖该 `*-client` 模块，MUST NOT 直接依赖被调用方的 app/domain/infra 等实现模块。

面向网关、浏览器、第三方系统或开放 API 的对外能力 SHALL 通过 adapter 层暴露的 REST API 定义和发布。外部调用方 SHALL 通过 HTTP 客户端或基于 OpenAPI 生成的客户端访问上述 REST API，而不是直接依赖 Java `*-client` 模块。各领域对外 REST 契约 SHALL 通过 SpringDoc/OpenAPI 维护，契约变更 SHALL 遵循语义化版本规则。

#### Scenario: 内部微服务调用系统服务

- **WHEN** `artemis-auth` 等内部微服务需要调用系统服务（如校验用户凭证）
- **THEN** SHALL 通过 `artemis-system-client` 暴露的契约进行调用，MUST NOT 直接依赖 `artemis-system-app`、`artemis-system-domain` 或 `artemis-system-infra`，也 MUST NOT 通过 HTTP 调用 internal REST 接口
