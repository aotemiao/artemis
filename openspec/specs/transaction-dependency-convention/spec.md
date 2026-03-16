## ADDED Requirements

### Requirement: 事务边界在 App 层

事务边界 SHALL 与用例边界一致，由 Application 层（App 模块）划定。即一个用例（一次 CmdExe 或 ApplicationService 调用）SHALL 在一个事务内完成；若用例内调用多个 Gateway 的写操作，这些操作 SHALL 处于同一事务中。MUST NOT 在 Infra 层的单个 Gateway 方法上作为唯一事务边界来设计跨多个 Gateway 的用例一致性。

#### Scenario: 用例级事务

- **WHEN** 一个命令执行器（CmdExe）内依次调用 Gateway A.save 与 Gateway B.log
- **THEN** 两处持久化 SHALL 在同一事务中提交或回滚，即事务 SHALL 在 App 层方法入口开启、方法出口提交或回滚

#### Scenario: 单 Gateway 场景

- **WHEN** 用例仅调用单一 Gateway 的写方法
- **THEN** 仍 SHALL 在 App 层（如 CmdExe 方法）上声明 `@Transactional`，以便未来扩展多步时无需调整事务边界

### Requirement: App 层显式依赖 spring-tx

需要声明事务边界的 App 模块（如 `artemis-system-app`）SHALL 在对应模块的 `pom.xml` 中显式声明对 `org.springframework:spring-tx` 的依赖。版本 SHALL 由项目 BOM（如 `spring-boot-dependencies`）统一管理，MUST NOT 在模块内写死版本号。App 层 MUST NOT 为获得 `@Transactional` 而依赖 `spring-boot-starter-jdbc` 或 `spring-boot-starter-data-jdbc`，以避免引入 DataSource 自动配置与 JDBC 相关依赖到 App 层。

#### Scenario: App 模块 pom 中声明 spring-tx

- **WHEN** 新建或维护一个包含 CmdExe/ApplicationService 的 App 模块且需使用 `@Transactional`
- **THEN** 该模块的 `pom.xml` SHALL 包含依赖 `groupId=org.springframework`, `artifactId=spring-tx`，且 SHALL 不指定 `version`（由 BOM 管理）

#### Scenario: App 层不引入 JDBC starter

- **WHEN** App 层仅需事务注解能力
- **THEN** MUST NOT 在该 App 模块中引入 `spring-boot-starter-jdbc` 或 `spring-boot-starter-data-jdbc`，以避免将数据源与 JDBC 能力错误地拉入 App 层

### Requirement: Infra 层通过 framework-jdbc 获得事务能力

Infrastructure 层实现（如 Gateway 实现类）若需使用 `@Transactional`（例如单个 Gateway 方法内多表写入），SHALL 通过该模块对 `artemis-framework-jdbc` 的依赖间接获得 `spring-tx`。即 Infra 模块 SHALL 依赖 `artemis-framework-jdbc`，由 `spring-boot-starter-data-jdbc` 传递引入 `spring-tx`；Infra 层 MUST NOT 在 `pom.xml` 中单独声明 `spring-tx`，除非该模块不依赖任何带 JDBC 的 framework 模块且确有需要（极少见）。

#### Scenario: Infra 模块事务依赖来源

- **WHEN** 检查依赖 `artemis-framework-jdbc` 的 Infra 模块（如 `artemis-system-infra`）
- **THEN** 该模块 SHALL 通过传递依赖获得 `spring-tx`，无需在模块内显式添加 `org.springframework:spring-tx`

#### Scenario: 事务边界优先在 App 层

- **WHEN** Infra 的 Gateway 实现类上存在 `@Transactional` 且同一用例会调用多个 Gateway
- **THEN** 设计上 SHALL 优先将 `@Transactional` 移至 App 层对应用例入口，使多 Gateway 调用处于同一事务；Infra 方法上的 `@Transactional` 仅作为单方法内多步写操作的可选补充，且 SHALL 使用 `propagation = REQUIRED` 以参与上层事务
