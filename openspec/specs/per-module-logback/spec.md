# 能力：per-module-logback

## ADDED Requirements

### Requirement: 可运行模块使用独立 logback 配置

每个可运行模块（artemis-system-start、artemis-auth、artemis-gateway）SHALL 在自身 classpath 下提供 logback 配置文件（logback-plus.xml），并通过 `logging.config` 指定为该文件，使该进程唯一使用此配置，不依赖 Spring Boot 默认发现或 Nacos 自带的 logback 配置。

#### Scenario: 启动时加载指定配置

- **WHEN** 启动任一可运行模块（如 artemis-system-start）
- **THEN** 该进程使用其 `src/main/resources/logback-plus.xml` 作为 logback 配置（通过 logging.config: classpath:logback-plus.xml）
- **AND** 启动日志中不出现 Logback 的 earlier_fa_collision 或 CONFIG_LOG_FILE/NAMING_LOG_FILE/REMOTE_LOG_FILE 重复定义错误

#### Scenario: 控制台与文件输出一致

- **WHEN** 应用运行并产生日志
- **THEN** 控制台与文件输出使用与 Spring Boot defaults 一致的格式（通过 include defaults.xml 与 console-appender）
- **AND** 文件日志按配置写入指定 LOG_PATH 下以应用名命名的文件（如 ${LOG_PATH}/${spring.application.name}.log）

### Requirement: 与 Nacos 共存无 logback 冲突

与 Nacos（discovery/config）共存时，SHALL 通过采用 Spring Cloud Alibaba 2025.x 与 nacos-client 2.5.x 的版本组合，在不排除 nacos-logback-adapter-12 或 logback-adapter 的前提下，避免 Nacos 重复加载自有 nacos-logback 配置，从而不产生 CONFIG_LOG_FILE、NAMING_LOG_FILE、REMOTE_LOG_FILE 的重复 appender 冲突（earlier_fa_collision）及「Attempted to append to non started appender」类 WARN。

#### Scenario: 升级后不排除 adapter 启动正常

- **WHEN** 项目使用 Spring Cloud Alibaba 2025.x 与 nacos-client 2.5.x，且 artemis-system-start、artemis-auth、artemis-gateway 对 nacos-discovery 与 nacos-config **未**排除 nacos-logback-adapter-12 或 logback-adapter，并已配置 logback-plus.xml 与 logging.config（含 application-common.yml）
- **THEN** 应用能正常启动并连接 Nacos
- **AND** 控制台无 earlier_fa_collision 相关 ERROR，无 NAMING_LOG_FILE/CONFIG_LOG_FILE/REMOTE_LOG_FILE 的「Attempted to append to non started appender」WARN

### Requirement: 文件日志滚动与保留策略

logback-plus.xml SHALL 配置 RollingFileAppender，支持按时间或大小滚动，并限制保留天数（maxHistory）与总容量（totalSizeCap），避免磁盘占满。

#### Scenario: 日志文件滚动与清理

- **WHEN** 应用运行且日志量达到滚动或保留策略条件
- **THEN** 生成按日期或序号滚动的历史文件
- **AND** 超过 maxHistory 或 totalSizeCap 的旧文件被清理

### Requirement: 为按模块扩展预留能力

各模块的 logback-plus.xml 为独立文件，SHALL 允许后续在单个模块中增加或修改 appender（例如接入 skylog）而不影响其他模块。

#### Scenario: 单模块可独立增加 appender

- **WHEN** 仅在 artemis-system-start 的 logback-plus.xml 中增加新的 appender 或 include
- **THEN** artemis-auth 与 artemis-gateway 的日志行为不变
- **AND** artemis-system-start 使用更新后的配置
