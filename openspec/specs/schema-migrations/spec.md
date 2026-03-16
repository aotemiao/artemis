## ADDED Requirements

### Requirement: 应用启动时执行 Flyway 迁移

使用 Flyway 的微服务 start 模块 SHALL 在应用启动时、在数据源初始化之后、业务访问数据库之前，自动执行 Flyway 迁移。迁移脚本 SHALL 位于 classpath 下的 `db/migration` 目录，使用 Spring Boot 对 Flyway 的自动配置与默认数据源；SHALL 不要求开发者或运维在启动应用前手动执行 SQL 或 Flyway CLI。

#### Scenario: 首次启动应用且数据库为空

- **WHEN** 使用 Flyway 的 start 模块首次启动，且连接的数据库为空（无 flyway_schema_history 表）
- **THEN** Flyway SHALL 创建 `flyway_schema_history` 表，并按版本顺序执行 `db/migration` 下所有未应用的 `V*__*.sql` 脚本；应用 SHALL 能正常启动并访问由迁移创建的表

#### Scenario: 再次启动应用且迁移已应用

- **WHEN** 使用 Flyway 的 start 模块再次启动，且当前数据库的 flyway_schema_history 中已记录所有现有迁移脚本
- **THEN** Flyway SHALL 不重复执行已应用的脚本；应用 SHALL 正常启动且不因迁移而失败

#### Scenario: 新增迁移脚本后启动

- **WHEN** 在 `db/migration` 下新增符合命名约定的脚本（如 `V2__add_foo.sql`）并启动应用
- **THEN** Flyway SHALL 仅执行尚未应用的脚本（如 V2），按版本号顺序执行；已应用的脚本（如 V1）SHALL 不再执行

### Requirement: 迁移脚本位置与命名约定

迁移脚本 SHALL 放置在模块的 `src/main/resources/db/migration/` 目录下（即 classpath 下的 `db/migration`）。脚本文件名 SHALL 符合 Flyway 版本化迁移约定：`V<版本号>__<描述>.sql`，其中版本号为数字（可含多段，如 1、2、1_1），双下划线为分隔符，描述为可读标识。SHALL 不修改已已被 Flyway 应用过的脚本内容，以避免校验和变更导致迁移失败；所有 schema 变更 SHALL 通过新增迁移脚本完成。

#### Scenario: 新表结构通过新脚本引入

- **WHEN** 需要为某能力新增表或索引
- **THEN** 开发 SHALL 在 `db/migration` 下新增一个带新版本号的 SQL 文件（如 `V2__add_orders_table.sql`），并在其中编写 DDL；SHALL 不修改已有的 `V1__*.sql` 等已应用脚本

### Requirement: Flyway 依赖与配置范围

仅在实际包含 `db/migration` 脚本且需要在启动时自动迁移的 start 模块中 SHALL 引入 Flyway 依赖（如 `flyway-core`，及按需 `flyway-database-postgresql`）；版本 SHALL 由项目 BOM 或 Spring Boot 依赖管理统一管理。Flyway SHALL 使用与 Spring Data JDBC 相同的单数据源配置（如 `spring.datasource.*`），默认 SHALL 使用 `classpath:db/migration` 作为迁移位置，无需在未有多数据源或自定义路径需求时额外配置 locations。

#### Scenario: 使用默认数据源与默认路径

- **WHEN** 某 start 模块已配置 `spring.datasource.url`（及 username、password）且未配置 `spring.flyway.locations`
- **THEN** Flyway SHALL 使用该数据源并对 `classpath:db/migration` 执行 migrate；应用启动后 SHALL 能访问由迁移创建或更新的表
