## ADDED Requirements

### Requirement: Nacos 配置层级与拉取方式

Nacos 配置 SHALL 按层级组织（参考 RuoYi-Cloud-Plus script/config/nacos）：公共配置（application-common.yml）→ 数据源（datasource.yml）→ 各服务专属（artemis-xxx.yml）。各业务微服务 SHALL 通过 `spring.config.import` 按固定顺序拉取，同一项目内 SHALL 只采用 config.import 方式，不得混用 bootstrap 与 config.import。

#### Scenario: 服务从 Nacos 拉取配置

- **WHEN** 某业务微服务已声明 Nacos config 依赖并配置了 config.import
- **THEN** 该服务启动时 SHALL 从 Nacos 拉取对应 Data ID 的配置并合并到 Spring Environment

#### Scenario: 拉取顺序一致

- **WHEN** 需数据库的服务（如 artemis-system）
- **THEN** config.import SHALL 为 application-common.yml → datasource.yml → 本服务 yml；不需数据库的服务（gateway、auth）SHALL 为 application-common.yml → 本服务 yml

### Requirement: 每服务仅一个 application.yml 且 Nacos 连接信息由 Maven 注入

各需 Nacos 的业务微服务 SHALL 仅保留一个 `application.yml`（无 bootstrap.yml、无 application-local.yml）。其中 Nacos 连接信息（server-addr、username、password、discovery/config group、namespace）SHALL 使用 Maven 占位符（如 `@nacos.server@`、`@profiles.active@`）；根 POM 的 Maven profile（dev/test/prod）SHALL 定义上述变量，并对 `application*`、`bootstrap*` 启用 resource filtering，打包时替换。

#### Scenario: 本地默认环境

- **WHEN** 使用默认 profile（dev）打包并运行
- **THEN** 占位符 SHALL 被替换为 dev 对应值（如 nacos.server=127.0.0.1:8848），无需额外配置文件

#### Scenario: 生产构建覆盖

- **WHEN** 生产构建需使用不同 Nacos 地址
- **THEN** SHALL 通过 `-Pprod` 或构建时 `-Dnacos.server=...` 覆盖，无需修改仓库内配置内容

### Requirement: Nacos 配置模板目录与上传方式

项目 SHALL 在仓库中提供 `config/nacos` 目录，包含各 Data ID 对应的 YAML 模板及 README（层级说明、上传顺序、Data ID 与文件对应关系）。MAY 提供通过 Nacos Open API 批量上传配置的脚本（如 `upload-configs.ps1`），支持通过参数指定 Nacos 地址与 namespace。

#### Scenario: 按模板上传到 Nacos

- **WHEN** 开发者或运维需向 Nacos 写入配置
- **THEN** 可从 `config/nacos` 找到与 Data ID 一致的模板及说明，按 README 或脚本完成上传

### Requirement: 多环境与 namespace

多环境（dev/test/prod）MAY 通过 Nacos namespace 隔离；若使用，SHALL 在 Nacos 控制台创建对应 namespace，部署时通过 Maven profile 或运行参数使 `spring.profiles.active` 与 namespace 对应（如 profile dev 对应 namespace id=dev）。连接信息 SHALL 通过占位符或环境变量注入，MUST NOT 在仓库中写死生产/测试地址。

### Requirement: 配置动态刷新

需要随 Nacos 配置变更而生效的 Bean SHALL 使用 `@RefreshScope`（或项目约定的等效机制）。详见 [coding-standards](../coding-standards/spec.md) 中「Nacos 配置动态刷新」约定。

### Requirement: Nacos 配置拉取方式统一

所有需要从 Nacos 配置中心加载配置的业务微服务（如 artemis-system、artemis-gateway、artemis-auth）SHALL 采用统一的接入方式：使用 `spring.config.import` 指定 optional nacos Data ID，或统一使用 bootstrap 配置；同一项目内 SHALL 只采用其中一种方式，不得混用。

#### Scenario: 服务从 Nacos 拉取配置

- **WHEN** 某业务微服务已声明 Nacos config 依赖并配置了 config.import 或 bootstrap 中的 Nacos config
- **THEN** 该服务启动时 SHALL 从 Nacos 拉取对应 Data ID 的配置并合并到 Spring Environment

#### Scenario: 拉取方式一致性

- **WHEN** 存在多个需 Nacos 配置的微服务
- **THEN** 各服务 SHALL 均使用 config.import 或均使用 bootstrap，不得部分用 config.import、部分用 bootstrap 且无文档说明

### Requirement: Nacos 连接信息外置

Nacos 的 server-addr、namespace（若使用）等连接信息 SHALL 通过环境变量或外部配置文件（如 application-{profile}.yml）提供；MUST NOT 在代码仓库的默认配置中写死具体 IP/域名（本地开发可用 localhost 的默认值在文档中说明，或通过 profile 限定）。

#### Scenario: 生产环境注入 Nacos 地址

- **WHEN** 在生产或测试环境部署微服务
- **THEN** Nacos server-addr SHALL 可通过 `SPRING_CLOUD_NACOS_SERVER_ADDR` 或等价配置项覆盖，无需修改打包产物

### Requirement: 多环境配置隔离

多环境（dev/test/prod）SHALL 通过以下至少一种方式隔离 Nacos 配置：按环境的 Data ID（如 `{服务名}-{profile}.yml`）、或 Nacos namespace；若使用 namespace，SHALL 在部署或运行配置中指定，不在仓库默认配置中写死。

#### Scenario: 按环境 Data ID 拉取

- **WHEN** 服务启用 `spring.profiles.active=prod` 且配置了按 profile 的 Nacos Data ID
- **THEN** 该服务 SHALL 从 Nacos 拉取对应 prod 的配置（如 artemis-system-prod.yml），而非 dev 配置

### Requirement: 公共配置可选支持

项目 MAY 支持通过 Nacos shared-configs 或 extension-configs 引入公共配置（如 `application-common.yml`、`application-{profile}.yml`），供多服务复用 Redis、公共中间件等；若采用，SHALL 在文档或配置模板中说明 Data ID 与引用方式。

#### Scenario: 公共配置引用

- **WHEN** 项目启用了 Nacos 公共配置并配置了 shared-configs
- **THEN** 各引用该配置的服务 SHALL 能正确加载公共配置内容，且优先级与 Spring 属性覆盖规则一致

### Requirement: 配置动态刷新约定

需要随 Nacos 配置变更而生效的 Bean（如使用 `@Value` 注入且希望不重启即更新）SHALL 使用 `@RefreshScope` 或 Nacos 官方推荐的等效机制；SHALL 在编码规范或 tech 说明中简要约定，便于团队统一使用。

#### Scenario: 刷新作用域 Bean

- **WHEN** 某 Bean 需要依赖 Nacos 中可动态变更的配置项
- **THEN** 该 Bean SHALL 标注 `@RefreshScope`（或项目约定的等效方式），以便 Nacos 配置更新后生效

### Requirement: Nacos 配置模板与文档

项目 SHALL 在仓库中提供 Nacos 配置模板目录（如 `config/nacos`），包含各服务对应的 Data ID 模板文件及说明（README），明确 Data ID、Group、多环境用法及上传方式；模板内容 SHALL 与各服务实际拉取的 Data ID 命名一致。

#### Scenario: 模板与 Data ID 一致

- **WHEN** 开发者或运维需向 Nacos 上传配置
- **THEN** 可从该目录找到与 Data ID 对应的模板文件及使用说明，按文档操作即可完成上传
