# 能力：dependency-management-conventions

## ADDED Requirements

### Requirement: BOM 仅根 POM 导入

项目 SHALL 在根 POM 的 `dependencyManagement` 中唯一一次 import `artemis-dependencies` BOM。所有子模块 SHALL 通过继承根 POM 获得依赖版本，MUST NOT 在子模块中再次 import 同一 BOM。

#### Scenario: 根 POM 提供 BOM

- **WHEN** 开发者查看根 `pom.xml`
- **THEN** SHALL 存在且仅存在一处对 `artemis-dependencies` 的 `<scope>import</scope>` 依赖

#### Scenario: 子模块不重复 import BOM

- **WHEN** 任意 `artemis-framework-*` 子模块的 `pom.xml` 被检查
- **THEN** SHALL NOT 包含对 `artemis-dependencies` 的 `dependencyManagement` import

### Requirement: 测试与架构测试依赖版本由 BOM 管理

JUnit Jupiter、ArchUnit 等测试与架构测试相关依赖的版本 SHALL 在 `artemis-dependencies` 的 `dependencyManagement` 中声明。使用这些依赖的子模块 SHALL 仅声明 groupId、artifactId 与 scope（如 `test`），MUST NOT 在子模块中指定 `<version>`。

#### Scenario: BOM 中声明测试依赖版本

- **WHEN** 开发者查看 `artemis-dependencies/pom.xml`
- **THEN** SHALL 在 `dependencyManagement` 中包含 `junit-jupiter` 与 `archunit` 的版本管理（含适用 scope）

#### Scenario: 子模块引用测试依赖不写版本

- **WHEN** 某模块（如 `artemis-auth`）需要 JUnit 或 ArchUnit
- **THEN** 该模块的 `pom.xml` SHALL 仅声明依赖的 groupId、artifactId 与 scope，SHALL NOT 包含 `<version>` 元素
