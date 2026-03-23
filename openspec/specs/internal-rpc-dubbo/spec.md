## ADDED Requirements

### Requirement: 内部服务间使用 Dubbo RPC

微服务之间的内部调用 SHALL 通过 Apache Dubbo RPC 进行。调用方 SHALL 仅依赖被调用方业务领域的 `*-client` 模块中暴露的 Dubbo 接口（Java interface）及 DTO，通过 `@DubboReference`（或项目约定的等效封装）注入并调用，MUST NOT 通过 HTTP 调用内部 REST 接口或直接依赖被调用方的 app/domain/infra 模块。

#### Scenario: 认证服务调用系统服务用户校验

- **WHEN** artemis-auth 需要校验用户名与密码并获取用户 ID
- **THEN** SHALL 通过 artemis-system-client 暴露的 Dubbo 接口（如 UserValidateService）调用，MUST NOT 使用 RestTemplate 请求 artemis-system 的 REST 接口

#### Scenario: 调用方仅依赖 client 模块

- **WHEN** 某服务需要调用另一业务领域的能力
- **THEN** SHALL 仅依赖该领域的 `*-client` 模块及 Dubbo 相关依赖，MUST NOT 依赖该领域的 app、domain 或 infra 模块

### Requirement: Dubbo 与 Nacos 注册中心集成

Dubbo 服务发现 SHALL 使用 Nacos 作为注册中心。SHALL 通过 `dubbo-registry-nacos` 与现有 Nacos 集群集成；Dubbo 的 application.name SHALL 与 Spring 应用名（spring.application.name）一致；registry 的 server-addr、namespace 等 SHALL 与项目 Nacos 配置一致，可通过环境变量或外部配置覆盖。

#### Scenario: 微服务暴露 Dubbo 服务

- **WHEN** 某业务微服务启动且需对外提供 Dubbo 接口
- **THEN** SHALL 将标注了 `@DubboService` 的实现类注册到 Nacos，供其他服务通过 Dubbo 发现并调用

#### Scenario: 微服务消费 Dubbo 服务

- **WHEN** 某业务微服务需要调用另一服务的 Dubbo 接口
- **THEN** SHALL 通过 `@DubboReference` 注入该接口，Dubbo 从 Nacos 发现提供方并建立调用

### Requirement: Dubbo 版本与依赖管理

Dubbo 及相关扩展（如 dubbo-registry-nacos）的版本 SHALL 由 `artemis-dependencies` BOM 统一管理。需要参与内部 RPC 的业务微服务 start 模块 SHALL 显式引入 `dubbo-spring-boot-starter`（或等价 starter）及 `dubbo-registry-nacos`，以及被调用方的 `*-client` 模块；不参与内部 RPC 的服务（如 artemis-gateway）SHALL NOT 引入 Dubbo 依赖。

#### Scenario: BOM 管理 Dubbo 版本

- **WHEN** 任何模块需要引入 Dubbo
- **THEN** 版本 SHALL 从 artemis-dependencies BOM 继承，MUST NOT 单独指定版本

### Requirement: 各领域 client 暴露 Dubbo 接口

各业务领域 SHALL 在自身的 `*-client` 模块中定义供内部调用的 Dubbo 接口（Java interface）及跨调用的 DTO；实现 SHALL 位于被调用方服务的 app 层或 adapter 层，并标注 `@DubboService` 暴露。接口与 DTO 的包名、命名 SHALL 与现有 *-client 契约约定一致（见 per-domain-client-contracts）。

#### Scenario: 系统领域暴露用户校验 Dubbo 接口

- **WHEN** artemis-system 需为 artemis-auth 提供用户名校密校验能力
- **THEN** SHALL 在 artemis-system-client 中定义 Dubbo 接口（如 UserValidateService），在 artemis-system 的 app 或 adapter 中提供实现并标注 @DubboService
