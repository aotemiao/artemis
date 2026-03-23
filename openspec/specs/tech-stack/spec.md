## ADDED Requirements

### Requirement: JDK 版本锁定

项目 SHALL 使用 JDK 21 作为编译和运行时版本。根 POM SHALL 配置 `maven.compiler.source` 和 `maven.compiler.target` 为 21。项目 MAY 使用 Java 21 已正式发布的语言特性（如 `record`、`sealed class`、pattern matching）；MUST NOT 依赖 preview features，除非仓库显式补齐 `--enable-preview` 等构建与运行配置。

#### Scenario: 编译环境验证

- **WHEN** 开发者执行 `mvn compile`
- **THEN** SHALL 使用 JDK 21 编译，支持 record、sealed class、pattern matching 等语法

### Requirement: Spring Boot 版本

项目 SHALL 使用 Spring Boot 3.x（最新稳定版）。BOM 模块 SHALL 通过 `spring-boot-dependencies` 管理 Spring 生态依赖版本。

#### Scenario: Spring Boot 版本一致性

- **WHEN** 任何模块引入 Spring Boot starter
- **THEN** 版本 SHALL 由 `artemis-dependencies` BOM 统一管理

### Requirement: Spring Cloud 版本

项目 SHALL 使用 Spring Cloud 2025.x（与 Spring Boot 3.x 对齐的版本线）。BOM 模块 SHALL 通过 `spring-cloud-dependencies` 管理 Spring Cloud 组件版本。

#### Scenario: Spring Cloud 组件版本对齐

- **WHEN** 引入 Spring Cloud Gateway 或 LoadBalancer
- **THEN** 版本 SHALL 由 Spring Cloud BOM 统一管理，MUST NOT 单独指定版本

### Requirement: 服务注册与配置中心

项目 SHALL 使用 Nacos 作为服务注册中心和配置中心。SHALL 通过 `spring-cloud-starter-alibaba-nacos-discovery` 和 `spring-cloud-starter-alibaba-nacos-config` 集成。需要从配置中心加载配置的业务微服务（如 artemis-system、artemis-gateway、artemis-auth）SHALL 统一从 Nacos 拉取各自配置。Nacos 的 server-addr、namespace 等连接信息 SHALL 通过环境变量或外部配置文件提供，MUST NOT 在仓库中写死具体地址（本地开发默认值可通过 profile 或文档说明）。

#### Scenario: 微服务启动注册

- **WHEN** 业务微服务启动
- **THEN** SHALL 自动注册到 Nacos 注册中心；若该服务已配置 Nacos config（config.import 或 bootstrap），SHALL 从 Nacos 配置中心加载配置

#### Scenario: Nacos 连接信息外置

- **WHEN** 部署或运行业务微服务
- **THEN** Nacos server-addr（及可选 namespace）SHALL 可通过环境变量或外部 application 配置覆盖，不在代码仓库中硬编码生产/测试地址

### Requirement: 认证授权框架

项目 SHALL 使用 Sa-Token 作为认证授权框架。SHALL 通过 `artemis-framework-security` starter 封装集成，支持 Token 认证、权限校验、角色校验。

#### Scenario: API 接口鉴权

- **WHEN** 请求到达需要认证的 API 接口
- **THEN** SHALL 通过 Sa-Token 校验 Token 有效性和权限

### Requirement: ORM 框架

项目 SHALL 使用 Spring Data JDBC 作为默认 ORM 实现。SHALL 通过 `artemis-framework-jdbc` starter 封装，提供：

- 基于 CrudRepository / JdbcAggregateTemplate 的聚合根持久化
- 审计字段自动填充（createTime, updateTime, createBy, updateBy）
- 逻辑删除支持（deleted 字段及查询过滤）
- 与 artemis-framework-core 中分页类型的转换（PageRequest、PageResult）

`artemis-framework-mybatis` MAY 作为可选能力保留，用于明确选择 MyBatis-Plus 的特殊场景；但业务模块当前默认持久化路径 SHALL 为 Spring Data JDBC。

#### Scenario: 数据访问层使用 Spring Data JDBC

- **WHEN** infra 层需要定义持久化实现
- **THEN** SHALL 使用 Spring Data JDBC 的 Repository 或 JdbcAggregateTemplate，聚合根 DO 与领域实体在 GatewayImpl 内转换

### Requirement: 默认数据库

项目 SHALL 以 PostgreSQL 作为默认数据库。BOM SHALL 管理 `org.postgresql:postgresql` 驱动版本。业务微服务 start 模块 SHALL 默认依赖 PostgreSQL 驱动；数据源连接信息 SHALL 通过配置（如 Nacos 或 application.yml）提供。

#### Scenario: 微服务使用 PostgreSQL

- **WHEN** 业务微服务启动并访问数据库
- **THEN** SHALL 使用 PostgreSQL 驱动连接；Spring Data JDBC 使用与 PostgreSQL 兼容的映射与 SQL 生成

### Requirement: 分页基类位置

分页请求与结果类型（如页码、每页条数、排序；结果列表、总数、总页数）SHALL 定义在 `artemis-framework-core` 中，与 Spring Data、MyBatis 等持久化框架解耦。adapter、app、infra 各层 SHALL 共用上述类型；infra 层 SHALL 负责将 core 分页类型与具体持久化框架的分页 API 相互转换。

#### Scenario: 分页类型不依赖持久化框架

- **WHEN** app 层或 adapter 层使用分页
- **THEN** SHALL 使用 core 中的 PageRequest、PageResult 等类型，MUST NOT 直接依赖 Spring Data 的 Pageable/Page 或 MyBatis 分页类型

### Requirement: 缓存与分布式锁

项目 SHALL 使用 Redisson 作为 Redis 客户端，提供缓存操作和分布式锁能力。SHALL 通过 `artemis-framework-redis` starter 封装。

#### Scenario: 分布式锁使用

- **WHEN** 需要对并发操作加锁
- **THEN** SHALL 通过 Redisson 提供的分布式锁 API 实现

### Requirement: API 网关

`artemis-gateway` SHALL 基于 Spring Cloud Gateway（WebFlux）实现，提供路由转发、鉴权过滤、限流等能力。

#### Scenario: 请求路由

- **WHEN** 外部请求到达网关
- **THEN** SHALL 根据路由规则转发到对应的业务微服务

### Requirement: 构建工具链

项目 SHALL 使用 Maven 作为构建工具，配置以下插件：
- `maven-compiler-plugin`：Java 21 编译
- `flatten-maven-plugin`：POM 版本展平
- `spring-boot-maven-plugin`：可执行 jar 打包（仅 start 模块）

#### Scenario: Start 模块打包

- **WHEN** 对 `artemis-system-start` 执行 `mvn package`
- **THEN** SHALL 生成包含所有依赖的可执行 fat jar

### Requirement: 跨服务调用方式

内部微服务之间的调用 SHALL 通过 Dubbo RPC 进行；各业务领域在 `*-client` 模块中暴露 Dubbo 接口，调用方使用 `@DubboReference`（或项目约定的等效封装）注入并调用，MUST NOT 通过 HTTP 调用内部 REST 接口。对外暴露（面向网关、开放 API）SHALL 仍通过各业务领域 adapter 层暴露的 REST API 进行；调用方使用 HTTP 客户端（RestTemplate、WebClient）或基于 OpenAPI 生成的客户端发起请求。Dubbo 与 Nacos 的集成方式及 BOM 管理见 internal-rpc-dubbo 能力规范。

#### Scenario: 跨服务调用契约

- **WHEN** 某服务需要调用系统服务的用户查询或校验能力（内部调用）
- **THEN** SHALL 通过 artemis-system-client 暴露的 Dubbo 接口调用，MUST NOT 依赖系统服务的 app/domain/infra 模块，MUST NOT 使用 RestTemplate 请求 system 的 REST 接口

#### Scenario: 对外调用仍为 REST

- **WHEN** 网关或外部系统需要调用业务服务的 API
- **THEN** SHALL 通过该业务服务暴露的 REST 接口（adapter 层）调用，契约为 OpenAPI/REST
