## ADDED Requirements

### Requirement: 业务微服务采用 client 契约模块与 COLA 五模块拆分

每个业务微服务（如 `artemis-system`）SHALL 至少包含一个用于内部跨服务调用的 `*-client` 契约模块，并将服务内部实现拆分为以下 5 个 Maven 子模块：

| 模块后缀 | 层 | 职责 |
|----------|-----|------|
| `-client` | 契约模块 | 内部 Dubbo 接口、跨服务 DTO/Result、对内发布的 Java 契约 |
| `-adapter` | Adapter 层 | REST Controller、协议适配、参数校验、DTO 组装 |
| `-app` | Application 层 | 用例编排、CmdExe / QryExe 执行器、事务边界 |
| `-domain` | Domain 层 | 聚合根、实体、值对象、领域服务、Gateway 接口、领域事件 |
| `-infra` | Infrastructure 层 | Gateway 实现、Spring Data JDBC Repository/聚合根持久化、DO、DO 与领域实体转换、外部 RPC/消息调用 |
| `-start` | 启动模块 | Spring Boot 主类、配置文件、模块组装 |

#### Scenario: 系统管理微服务模块结构

- **WHEN** 查看 `artemis-modules/artemis-system` 目录
- **THEN** SHALL 包含 `artemis-system-client`、`artemis-system-adapter`、`artemis-system-app`、`artemis-system-domain`、`artemis-system-infra`、`artemis-system-start` 六个子模块

### Requirement: 层间依赖方向强制约束

模块间依赖 SHALL 严格遵循以下方向，MUST NOT 出现逆向依赖：

```
adapter → app → domain ← infra
                  ↑
start (组装所有模块)
```

`client` 作为对外发布的内部契约模块独立于上述内部分层：

- `client` MUST NOT 依赖 `adapter`、`app`、`domain`、`infra`、`start`
- 其他微服务 SHALL 仅依赖 `client`，不得依赖内部实现层

具体规则：
- `adapter` SHALL 依赖 `app`，MUST NOT 直接依赖 `domain` 或 `infra`
- `adapter` MAY 依赖 `client`，用于实现 Dubbo 服务接口或复用跨服务 DTO
- `app` SHALL 依赖 `domain`，MUST NOT 依赖 `infra`（通过 domain 的 Gateway 接口解耦）
- `infra` SHALL 依赖 `domain`（实现 Gateway 接口），MUST NOT 依赖 `app` 或 `adapter`
- `domain` MUST NOT 依赖 `infra`、`app`、`adapter` 中的任何一个
- `start` SHALL 依赖所有需要被组装的模块（通常包含 `adapter`、`infra`、`client`），负责 Spring Boot 应用组装

#### Scenario: Domain 层纯净性验证

- **WHEN** 检查 `artemis-system-domain` 的 `pom.xml` 依赖
- **THEN** MUST NOT 包含 Spring Data JDBC、MyBatis、Spring Web、数据库驱动等技术框架依赖

#### Scenario: App 层通过 Gateway 接口访问基础设施

- **WHEN** 应用层需要持久化领域对象
- **THEN** SHALL 调用 domain 层定义的 Gateway 接口，由 infra 层提供实现，MUST NOT 直接引用 infra 层的 Mapper 或 Repository

### Requirement: Adapter 层职责约定

Adapter 层 SHALL 承担以下职责：
- 定义 REST Controller，处理 HTTP 请求/响应
- 定义请求/响应 DTO（Data Transfer Object）
- 通过 Assembler 将 DTO 转换为 domain Command/Query
- 参数校验（Bean Validation）
- 调用 app 层的应用服务或执行器

Adapter 层 MUST NOT：
- 包含业务逻辑
- 直接操作领域实体
- 直接调用 infra 层

#### Scenario: Controller 处理用户创建请求

- **WHEN** HTTP POST 请求到达 UserController
- **THEN** Controller SHALL 将 CreateUserRequest DTO 转换为 CreateUserCmd，调用 app 层的 CreateUserCmdExe，返回响应 DTO

### Requirement: Application 层职责约定

App 层 SHALL 承担以下职责：
- 定义应用服务（ApplicationService）编排用例流程
- 实现命令执行器（CmdExe）处理写操作
- 实现查询执行器（QryExe）处理读操作
- 管理事务边界（`@Transactional`）
- 调用 domain 层的领域服务和 Gateway 接口

App 层 MUST NOT：
- 包含核心业务规则（属于 domain 层）
- 直接依赖技术框架（如 MyBatis、Redis）

#### Scenario: 用户创建用例编排

- **WHEN** CreateUserCmdExe 执行
- **THEN** SHALL 调用 domain 层的领域服务进行业务规则校验，再通过 Gateway 接口持久化聚合根

### Requirement: Domain 层职责约定

Domain 层 SHALL 承担以下职责：
- 定义聚合根（Aggregate Root）和实体（Entity）
- 定义值对象（Value Object），优先使用 Java `record`
- 定义领域服务（DomainService）封装跨实体业务规则
- 定义 Gateway 接口（端口），声明对基础设施的需求
- 定义领域事件（DomainEvent）

Domain 层 MUST NOT：
- 依赖任何 Spring 框架注解（`@Service`、`@Component` 等除外，允许 COLA 的 `@DomainService`）
- 依赖持久化框架（MyBatis、JPA 等）
- 依赖 Web 框架
- 包含 DTO 定义

#### Scenario: Gateway 接口定义

- **WHEN** 领域层需要持久化用户聚合根
- **THEN** SHALL 在 domain 模块中定义 `UserGateway` 接口，声明 `save(User user)` 等方法

### Requirement: Infrastructure 层职责约定

Infra 层 SHALL 承担以下职责：
- 实现 domain 层定义的 Gateway 接口
- 定义数据对象 DO（Data Object），与数据库表/聚合根映射一致
- 实现 DO ↔ Domain Entity 的转换（Converter）
- 使用 Spring Data JDBC 的 CrudRepository 或 JdbcAggregateTemplate 持久化聚合根 DO
- 封装外部服务调用（RPC、HTTP、消息队列）

Infra 层 MUST NOT：
- 包含业务逻辑
- 被 adapter 或 app 层直接依赖（仅通过 domain Gateway 接口间接使用）

#### Scenario: UserGateway 实现

- **WHEN** infra 层实现 UserGateway
- **THEN** SHALL 创建 UserGatewayImpl，内部通过 Spring Data JDBC 的 UserRepository（CrudRepository<UserDO, ID>）或 JdbcAggregateTemplate 操作 UserDO，并在 save/load 时转换为 Domain 实体

### Requirement: Start 模块启动类与组件扫描

`-start` 模块的 Spring Boot 主类 SHALL 仅使用 `@SpringBootApplication`，MUST NOT 指定 `scanBasePackages`。业务包（主类所在包及子包）SHALL 依赖默认扫描；公共能力（artemis-framework 各 starter）SHALL 通过其 `@AutoConfiguration` 与 `META-INF/spring/.../AutoConfiguration.imports` 由依赖引入，MUST NOT 通过 scanBasePackages 扫描 `com.aotemiao.artemis.framework` 等包。

#### Scenario: 启动类不扫 framework 包

- **WHEN** 某业务微服务启动
- **THEN** framework 下的 Bean（如 WebMvcConfig、JdbcAutoConfiguration）SHALL 由 Spring Boot 自动配置加载，业务侧 SHALL 无需也不得通过 scanBasePackages 包含 framework 包

### Requirement: 包命名规范

各层的 Java 包结构 SHALL 遵循以下约定（以 `artemis-system` 为例，基础包为 `com.aotemiao.artemis.system`）：

| 层 | 包路径 |
|----|--------|
| client | `com.aotemiao.artemis.system.client.api` / `.client.dto` |
| adapter | `com.aotemiao.artemis.system.adapter.web` |
| app | `com.aotemiao.artemis.system.app.service` / `.app.command` / `.app.query` |
| domain | `com.aotemiao.artemis.system.domain.model` / `.domain.service` / `.domain.gateway` / `.domain.event` |
| infra | `com.aotemiao.artemis.system.infra.gateway` / `.infra.repository` / `.infra.mapper` / `.infra.dataobject` / `.infra.converter` |

#### Scenario: 按包路径定位代码

- **WHEN** 开发者需要查找用户领域的 Gateway 接口
- **THEN** SHALL 在 `com.aotemiao.artemis.system.domain.gateway` 包下找到 `UserGateway` 接口
