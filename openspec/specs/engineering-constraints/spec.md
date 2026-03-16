## ADDED Requirements

### Requirement: 微服务独立部署

每个业务微服务 SHALL 可独立编译、打包、部署和运行。微服务的 `-start` 模块 SHALL 包含独立的 Spring Boot 主类和配置文件。微服务间 MUST NOT 存在编译期循环依赖。

#### Scenario: 独立启动系统管理服务

- **WHEN** 仅启动 `artemis-system-start`
- **THEN** 系统管理微服务 SHALL 能独立运行，无需其他业务微服务同时启动（仅依赖 Nacos、Redis 等基础设施）

### Requirement: 公共能力 Starter 化

`artemis-framework` 下的每个公共能力模块 SHALL 以 Spring Boot Starter 模式提供：
- 包含 `@AutoConfiguration` 自动配置类
- 提供 `spring.factories` 或 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册
- 支持通过 `application.yml` 属性开关和配置
- 提供合理的默认值，实现零配置可用

根 POM SHALL 对 `application*`、`bootstrap*` 资源文件启用 Maven resource filtering，以便 Nacos 等占位符在打包时被 profile 变量替换。

#### Scenario: Redis starter 自动配置

- **WHEN** 业务模块添加 `artemis-framework-redis` 依赖
- **THEN** Redisson 客户端 SHALL 自动配置，默认连接 `localhost:6379`，可通过配置覆盖

### Requirement: API 契约化跨服务调用

微服务间的调用 SHALL 通过被调用方对外暴露的 REST API 进行。调用方 SHALL 使用 HTTP 客户端或基于 OpenAPI 生成的客户端调用目标服务的 REST 接口，MUST NOT 依赖被调用方的内部实现模块（app/domain/infra）。契约变更 SHALL 遵循语义化版本规则。

#### Scenario: 认证服务调用系统服务

- **WHEN** `artemis-auth` 需要查询用户信息
- **THEN** SHALL 通过系统服务对外 REST API（HTTP）或基于其 OpenAPI 生成的客户端进行调用，MUST NOT 直接依赖 `artemis-system-app` 或 `artemis-system-domain`

### Requirement: Nacos 配置模板目录

项目 SHALL 在仓库中保留 `config/nacos` 目录，作为 Nacos 配置中心的模板与说明目录（不参与打包）。SHALL 包含层级化 YAML 模板（application-common、datasource、各服务 artemis-xxx）及 README（Data ID 对应关系、上传顺序、Maven profile 说明）。MAY 提供通过 Nacos Open API 批量上传的脚本（如 PowerShell），支持指定 Nacos 地址与 namespace。

### Requirement: 容器化部署骨架

项目 SHALL 在根目录预留以下容器化相关文件/目录结构：

- `docker/`：Docker 相关配置
  - 各微服务的 Dockerfile 模板
  - `docker-compose.yml`：本地开发环境编排（含 Nacos、Redis 等）
- 每个 `-start` 模块 SHALL 预留 Dockerfile 或在 `docker/` 中有对应配置

#### Scenario: 本地一键启动依赖

- **WHEN** 开发者执行 `docker-compose up`
- **THEN** SHALL 启动 MySQL、Redis、Nacos 等基础设施，业务微服务可直接连接

### Requirement: 代码质量工具集成

项目 SHALL 在根 POM 中集成以下代码质量工具：

- **Checkstyle**：代码风格检查，配置文件位于项目根目录
- **SpotBugs**：静态 Bug 检测

质量检查 SHALL 在 `mvn verify` 阶段自动执行。违反规则 SHALL 导致构建失败。

#### Scenario: 代码风格违规

- **WHEN** 提交的代码违反 Checkstyle 规则
- **THEN** `mvn verify` SHALL 失败并报告具体违规位置

### Requirement: 多环境配置管理

每个微服务 SHALL 支持多环境配置，通过 Spring Profiles 切换：
- `dev`：本地开发
- `test`：测试环境
- `prod`：生产环境

默认激活的 profile SHALL 为 `dev`。环境特定配置 SHALL 通过 Nacos 配置中心管理。

#### Scenario: 切换到测试环境配置

- **WHEN** 启动微服务时指定 `--spring.profiles.active=test`
- **THEN** SHALL 加载测试环境的 Nacos 配置

### Requirement: 日志规范

项目 SHALL 使用 SLF4J + Logback 作为日志框架。日志 SHALL 遵循以下规范：
- 使用 `@Slf4j`（Lombok）注解注入 logger
- 生产环境默认日志级别 SHALL 为 `INFO`
- 敏感信息（密码、Token 等）MUST NOT 出现在日志中
- 操作审计日志 SHALL 通过 `artemis-framework-log` 以 AOP 方式记录

#### Scenario: 操作审计日志

- **WHEN** 用户执行创建、修改、删除操作
- **THEN** SHALL 自动记录操作人、操作时间、操作类型、操作内容到审计日志

### Requirement: 测试与 TDD 约定（artemis-system）

在 artemis-system 模块中，App 层（CmdExe、QryExe）SHALL 配备单元测试，以 LookupTypeGateway 等 Gateway 为边界进行 mock；Infra 层 Gateway 实现 SHALL 配备集成测试（真实或内嵌 DB）。新增或修改 CmdExe/QryExe 时 SHALL 同步编写或先写对应单元测试；新增或修改 Gateway 实现时 SHALL 提供集成测试覆盖关键路径。测试类命名 SHALL 与实现对应（如 *CmdExeTest、*QryExeTest、*GatewayImplIntegrationTest）。详细分层策略与任务分解见变更 `openspec/changes/lookup-tdd-tests` 的 design.md 与 tasks.md。

#### Scenario: App 层执行器有对应单元测试

- **WHEN** 代码库中存在 *CmdExe 或 *QryExe 类（如 CreateLookupTypeCmdExe）
- **THEN** 同一模块 src/test 下 SHALL 存在对应 *ExeTest 类，且至少包含一个调用 execute 并断言的测试方法
