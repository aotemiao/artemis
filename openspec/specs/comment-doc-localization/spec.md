## ADDED Requirements

### Requirement: 注释与文档使用中文

项目面向中国开发者，代码与文档中的注释及说明性文字 SHALL 使用中文。命名（类名、方法名、变量名、包名、配置 key 等）SHALL 保持英文、符合英语母语习惯。技术术语（如 stub、contract、logical delete、Gateway、DTO、DO）在注释中 MAY 保留英文，必要时可括号补充中文。

#### Scenario: Java 类注释

- **WHEN** 编写或修改 Java 类的 Javadoc
- **THEN** 类描述、方法描述及 `@param`、`@return`、`@throws` 的说明 SHALL 使用中文；术语可保留英文

#### Scenario: 行内注释与 package-info

- **WHEN** 编写或修改行内 `//` 注释或 `package-info.java` 中的包描述
- **THEN** 内容 SHALL 使用中文；术语可保留英文

#### Scenario: 配置文件注释

- **WHEN** 在 `pom.xml`、`application*.yml`、`bootstrap*.yml` 等中添加或修改说明性注释
- **THEN** 注释 SHALL 使用中文；术语可保留英文

#### Scenario: 项目与贡献者文档

- **WHEN** 编写或修改 README、CONTRIBUTING、.cursor/rules 等面向贡献者的文档
- **THEN** 内容 SHALL 使用中文或提供中文说明；术语可保留英文

#### Scenario: 新增代码的注释约定

- **WHEN** 新增或修改代码、配置、文档
- **THEN** 新增的注释与说明性文字 SHALL 使用中文，以维持项目统一约定

### Requirement: 一次性收口既有英文注释

既有代码、配置与文档中当前为英文的注释与说明 SHALL 在本变更中一次性改为中文（术语可保留英文），使全库达到「注释与文档中文」的约定。

#### Scenario: 框架层英文注释

- **WHEN** 某 Java 文件（如 framework、core）中存在英文 Javadoc 或行内注释
- **THEN** 该注释 SHALL 被替换为中文表述，技术术语保留英文

#### Scenario: 配置与文档中的英文说明

- **WHEN** `pom.xml` 或项目文档中存在英文说明性注释或段落
- **THEN** 该内容 SHALL 被替换或补充为中文，术语可保留英文
