## MODIFIED Requirements

### Requirement: Maven 多模块顶层聚合结构

根 POM（`com.aotemiao:artemis`）SHALL 作为聚合 POM，packaging 类型为 `pom`，声明所有顶层模块。顶层模块 SHALL 包含以下固定成员：

- `artemis-dependencies`：BOM 模块
- `artemis-framework`：公共能力聚合模块
- `artemis-gateway`：API 网关
- `artemis-auth`：认证授权服务
- `artemis-modules`：业务微服务聚合模块
- `artemis-visual`：运维基础设施聚合模块
- `artemis-symphony`：编码代理编排服务（Linear + WORKFLOW.md + Codex app-server，独立可运行）

#### Scenario: 根 POM 模块声明完整

- **WHEN** 开发者查看根 `pom.xml` 的 `<modules>` 节
- **THEN** SHALL 包含上述固定顶层模块列表（含 `artemis-symphony`），不再包含任何集中式 API 契约聚合模块
