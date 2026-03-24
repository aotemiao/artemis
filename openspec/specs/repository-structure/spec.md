## ADDED Requirements

### Requirement: Maven 多模块顶层聚合结构

根 POM（`com.aotemiao:artemis`）SHALL 作为聚合 POM，packaging 类型为 `pom`，声明所有顶层模块。顶层模块 SHALL 包含以下固定成员：

- `artemis-dependencies`：BOM 模块
- `artemis-api`：内部契约聚合与客户端 BOM
- `artemis-framework`：公共能力聚合模块
- `artemis-gateway`：API 网关
- `artemis-auth`：认证授权服务
- `artemis-modules`：业务微服务聚合模块
- `artemis-visual`：运维基础设施聚合模块
- `artemis-symphony`：编码代理编排服务（Linear + WORKFLOW.md + Codex app-server，独立可运行；详见 `artemis-symphony/README.md`）

#### Scenario: 根 POM 模块声明完整

- **WHEN** 开发者查看根 `pom.xml` 的 `<modules>` 节
- **THEN** SHALL 包含上述固定顶层模块列表（含 `artemis-api` 与 `artemis-symphony`）

### Requirement: API 聚合模块用于收敛内部客户端契约

`artemis-api` SHALL 作为聚合 POM，承载以下两类子模块：

- `artemis-api-bom`：对内客户端 BOM，统一管理 `artemis-api-*` 与 `artemis-*-client` 版本
- `artemis-api-<domain>`：按领域聚合的 API bridge 模块，对外转发对应 `artemis-<domain>-client`

`artemis-api-<domain>` MUST NOT 依赖对应服务的 `adapter`、`app`、`domain`、`infra` 或 `start`。其职责是为调用方提供稳定入口，而不是复制服务实现。

#### Scenario: 系统领域提供统一 API bridge

- **WHEN** 查看 `artemis-api` 目录
- **THEN** SHALL 至少包含 `artemis-api-bom` 与 `artemis-api-system`
- **AND** `artemis-api-system` SHALL 仅依赖 `artemis-system-client`

### Requirement: BOM 模块统一依赖版本

`artemis-dependencies` 模块 SHALL 采用 `pom` packaging，在 `<dependencyManagement>` 中声明所有第三方依赖及 artemis 内部模块的版本。所有其他模块 SHALL 通过 import 该 BOM 来管理依赖版本，MUST NOT 在子模块中单独指定已被 BOM 管理的依赖版本号。

#### Scenario: 子模块引用 BOM 管理的依赖

- **WHEN** 业务模块需要使用 Spring Data JDBC
- **THEN** 仅需在 `<dependencies>` 中声明 `spring-boot-starter-data-jdbc` 及 `artemis-framework-jdbc` 等 BOM 已管理依赖，MUST NOT 指定 `<version>`

### Requirement: Framework 公共能力模块结构

`artemis-framework` SHALL 作为聚合 POM，包含以下公共能力 starter 子模块（按需扩展）：

- `artemis-framework-core`：核心工具、异常体系、通用 DTO、分页基类（PageRequest、PageResult）
- `artemis-framework-web`：Web 层公共配置、全局异常处理、响应封装
- `artemis-framework-security`：安全认证集成（Sa-Token）
- `artemis-framework-jdbc`：Spring Data JDBC 配置、聚合根基类、审计、逻辑删除、分页转换、PostgreSQL 约定
- `artemis-framework-mybatis`：（可选）MyBatis-Plus 配置、基类、审计字段；用于持久化可替换为 MyBatis 实现的场景
- `artemis-framework-redis`：Redisson 配置、缓存工具
- `artemis-framework-log`：日志与操作审计
- `artemis-framework-doc`：API 文档（Swagger/SpringDoc）

各子模块的 Java 包 SHALL 为 `com.aotemiao.artemis.framework.{能力}`（与模块名后缀一致，如 `com.aotemiao.artemis.framework.core`）。每个 starter SHALL 以 `@AutoConfiguration` 实现零配置接入。

#### Scenario: 业务模块引入公共安全能力

- **WHEN** 业务微服务需要认证能力
- **THEN** 仅需在 POM 中添加 `artemis-framework-security` 依赖，无需额外配置即可生效

#### Scenario: 业务模块引入持久化能力

- **WHEN** 业务微服务需要默认持久化能力
- **THEN** SHALL 在 POM 中添加 `artemis-framework-jdbc` 依赖及 PostgreSQL 驱动，无需额外配置即可使用 Spring Data JDBC 与分页转换

### Requirement: 跨服务调用区分内部契约与对外 REST API

内部微服务之间的调用 SHALL 通过各业务领域 colocated 的 `*-client` 模块暴露契约。`*-client` 模块 SHALL 承载供内部调用的 Dubbo 接口与 DTO/Result；仓库 MAY 通过 `artemis-api-<domain>` 作为聚合 bridge 暴露这些契约。调用方 SHALL 仅依赖该 `*-client` 模块或对应 `artemis-api-<domain>`，MUST NOT 直接依赖被调用方的 app/domain/infra 等实现模块。

面向网关、浏览器、第三方系统或开放 API 的对外能力 SHALL 通过 adapter 层暴露的 REST API 定义和发布。外部调用方 SHALL 通过 HTTP 客户端或基于 OpenAPI 生成的客户端访问上述 REST API，而不是直接依赖 Java `*-client` 模块。各领域对外 REST 契约 SHALL 通过 SpringDoc/OpenAPI 维护，契约变更 SHALL 遵循语义化版本规则。

#### Scenario: 内部微服务调用系统服务

- **WHEN** `artemis-auth` 等内部微服务需要调用系统服务（如校验用户凭证）
- **THEN** SHALL 通过 `artemis-system-client` 或 `artemis-api-system` 暴露的契约进行调用，MUST NOT 直接依赖 `artemis-system-app`、`artemis-system-domain` 或 `artemis-system-infra`，也 MUST NOT 通过 HTTP 调用 internal REST 接口

#### Scenario: 网关或外部系统调用系统服务

- **WHEN** 网关或第三方系统需要调用系统服务对外能力
- **THEN** SHALL 通过系统服务 adapter 层暴露的 REST API 进行 HTTP 调用，或依赖基于 OpenAPI 生成的客户端，而不是依赖 `artemis-system-client`

### Requirement: 业务微服务聚合结构

`artemis-modules` SHALL 作为聚合 POM，每个业务微服务作为其子模块。初始阶段 SHALL 包含：

- `artemis-system`：系统管理（用户、角色、菜单、部门、字典、租户等）

每个业务微服务 SHALL 按 COLA 分层拆分为内部子模块（详见 `ddd-cola-layering` spec）。

#### Scenario: 新增业务微服务

- **WHEN** 需要新增资源管理微服务
- **THEN** SHALL 在 `artemis-modules` 下创建 `artemis-resource`，内含 `artemis-resource-client`、adapter、app、domain、infra、start 等子模块

### Requirement: 版本管理使用 revision 机制

根 POM SHALL 定义 `<revision>` 属性作为全局版本号。所有子模块 SHALL 使用 `${revision}` 引用父版本。SHALL 配置 `flatten-maven-plugin` 在 install/deploy 阶段展平 POM。

#### Scenario: 全局版本升级

- **WHEN** 需要将框架从 1.0.0 升级到 1.1.0
- **THEN** 仅需修改根 POM 的 `<revision>` 属性值，所有子模块版本自动同步
