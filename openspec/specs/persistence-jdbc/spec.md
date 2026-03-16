## ADDED Requirements

### Requirement: Spring Data JDBC 默认持久化

项目 SHALL 使用 Spring Data JDBC 作为默认持久化实现。SHALL 通过 `artemis-framework-jdbc` starter 封装，提供：

- 自动配置（数据源、JdbcAggregateTemplate）；Repository 扫描 SHALL 通过配置项 `artemis.jdbc.repositories.base-packages` 启用，SHALL 使用 Ant 风格包名（如 `com.aotemiao.artemis.*.infra.repository`）以一份配置覆盖多业务模块；业务模块 MUST NOT 单独定义 `@EnableJdbcRepositories`
- 审计与逻辑删除作为可组合能力：提供 **Auditable**、**SoftDeletable** 接口；提供 **AuditFieldsBase**（仅审计字段：createTime, updateTime, createBy, updateBy）、**AuditAndSoftDeleteBase**（审计 + 逻辑删除字段 deleted）供 DO 按需继承；审计回调 SHALL 基于 **Auditable** 判断并填充，软删相关逻辑 SHALL 基于 **SoftDeletable** 识别
- 审计字段自动填充（BeforeConvert 等回调对实现 Auditable 的实体填充 createTime, updateTime, createBy, updateBy）
- 与 core 分页类型的转换（PageRequest ↔ Pageable，Page → PageResult）
- 默认数据库为 PostgreSQL 时的方言与驱动配置约定

#### Scenario: 数据访问层使用 Spring Data JDBC

- **WHEN** infra 层需要实现 Gateway 持久化聚合根
- **THEN** SHALL 通过 Spring Data JDBC 的 `CrudRepository<DO, ID>` 或 `JdbcAggregateTemplate` 实现，DO 与领域实体在 GatewayImpl 内转换

#### Scenario: 分页使用 core 中性类型

- **WHEN** Gateway 接口需要分页查询
- **THEN** SHALL 使用 artemis-framework-core 定义的 `PageRequest` 与 `PageResult<T>`；infra 实现 SHALL 将 PageRequest 转为 Spring Data 的 `Pageable`，并将 `Page<T>` 转为 `PageResult<T>`

#### Scenario: DO 需要审计与逻辑删除

- **WHEN** 聚合根 DO 需要审计字段与逻辑删除
- **THEN** SHALL 继承 `AuditAndSoftDeleteBase`，并映射 `@Table`；审计回调 SHALL 对其填充 createTime/updateTime/createBy/updateBy，查询时 SHALL 按约定过滤 deleted = 0（或由调用方/封装保证）

#### Scenario: DO 仅需审计

- **WHEN** 聚合根 DO 仅需审计字段、不需要逻辑删除
- **THEN** SHALL 继承 `AuditFieldsBase`（实现 Auditable）；审计回调 SHALL 对其填充审计字段

### Requirement: 聚合根持久化与表结构

以 Spring Data JDBC 持久化聚合根时，表结构 SHALL 满足三范式：聚合根对应根表，聚合内实体或集合值对象对应子表，外键指向聚合根；聚合外引用仅存 ID。SHALL 使用 `@Table`、`@Id`、`@Column` 等注解或约定进行映射。

#### Scenario: 订单聚合持久化

- **WHEN** 持久化 Order 聚合根（含 OrderLine 子实体）
- **THEN** SHALL 存在 order 表与 order_line 表，order_line 含 order_id 外键；Repository 以聚合根为单位 load/save，子表由 Spring Data JDBC 按聚合边界维护

#### Scenario: JDBC 仓库扫描由配置驱动

- **WHEN** 某业务微服务（如 artemis-system）使用 Spring Data JDBC Repository
- **THEN** SHALL 在配置（Nacos 或 application.yml）中设置 `artemis.jdbc.repositories.base-packages`（如 `com.aotemiao.artemis.*.infra.repository`），MUST NOT 在业务侧新增带 `@EnableJdbcRepositories` 的配置类
