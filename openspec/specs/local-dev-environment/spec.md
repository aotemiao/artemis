## ADDED Requirements

### Requirement: 本地基础设施与规范一致

本地开发时，项目 SHALL 使用与 openspec/specs/tech-stack 及 config/nacos 模板一致的默认数据库（PostgreSQL）。docker-compose 提供的数据库、缓存与配置中心 SHALL 与 Nacos 中 datasource.yml 及 application-common 等配置的默认连接信息一致，使“快速开始”路径无需额外修改 Nacos 或本地安装其他数据库即可运行各业务服务。

#### Scenario: 一键启动基础设施后与 Nacos 模板一致

- **WHEN** 开发者按 README 执行 `docker-compose up -d` 并按要求上传 config/nacos 中的配置到 Nacos
- **THEN** 各业务服务使用的数据源（如 datasource.yml 中的 system-master）SHALL 能连接 docker-compose 提供的数据库而无须改 Nacos 配置或本地安装其他 DB

#### Scenario: 文档中不出现默认 MySQL 的歧义

- **WHEN** 开发者阅读 README 与 config/nacos/README 中关于“快速开始”或“本地依赖”的说明
- **THEN** 文档 SHALL 明确默认数据库为 PostgreSQL，且与 docker-compose 及 Nacos 模板一致，MUST NOT 同时将 MySQL 描述为默认本地数据库

### Requirement: docker-compose 提供 PostgreSQL

项目根目录下的 docker-compose（如 `docker/docker-compose.yml`）SHALL 提供 PostgreSQL 服务用于本地开发，SHALL NOT 默认提供 MySQL 作为与 Nacos datasource 模板对应的数据库。PostgreSQL 的数据库名、用户名、密码等 SHALL 与 config/nacos/datasource.yml 中默认值一致（或文档中明确说明如何覆盖），以便上传 Nacos 配置后即可使用。

#### Scenario: 启动后存在可用的 PostgreSQL 实例

- **WHEN** 开发者执行 `docker-compose up -d`
- **THEN** SHALL 存在运行中的 PostgreSQL 容器，且使用与 Nacos datasource 模板一致的默认库名（如 artemis_system）与认证信息，业务服务连接该实例可正常启动并执行 Flyway 迁移

### Requirement: 基础设施服务具备健康检查

docker-compose 中的 PostgreSQL、Redis、Nacos 等基础设施服务 SHALL 配置 healthcheck，以便通过 `depends_on` 的 `condition: service_healthy` 控制启动顺序，避免依赖未就绪导致启动失败。

#### Scenario: Nacos 依赖数据库就绪后启动

- **WHEN** Nacos 配置为依赖数据库（若当前为 standalone 且无外置 DB，则仅需自身就绪）
- **THEN** Nacos 服务 SHALL 在自身健康检查通过后才视为就绪；若存在依赖的数据库，则 SHALL 在数据库健康后再启动

#### Scenario: 健康检查使用标准方式

- **WHEN** 为 PostgreSQL、Redis、Nacos 配置 healthcheck
- **THEN** SHALL 使用各组件官方或常规健康检查方式（如 postgres 使用 pg_isready，redis 使用 redis-cli ping），且超时与间隔合理，避免误判
