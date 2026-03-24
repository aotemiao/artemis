## ADDED Requirements

### Requirement: 内部契约文档同步检查

每个 `*-client` 契约模块 SHALL 提供对应的契约文档，并通过仓库脚本校验公开接口、核心 DTO 与文档声明保持同步。若 Java 契约发生变化而文档未同步，检查 SHALL 失败。

#### Scenario: Dubbo 接口新增方法但文档未更新

- **WHEN** `*-client` 模块中的公开接口或公开 DTO 发生签名变化
- **THEN** 契约检查脚本 SHALL 发现文档缺失对应声明并报告失败

### Requirement: REST API 文档同步检查

对外或跨服务调用的 REST Controller SHALL 具有对应 API 文档，并通过仓库脚本校验 Controller 中的映射与文档列出的路由保持同步。该检查至少 SHALL 覆盖：

- `artemis-auth`
- `artemis-system`

#### Scenario: Controller 新增路由但 API 文档未回写

- **WHEN** Controller 中新增 `@GetMapping`、`@PostMapping`、`@PutMapping` 或 `@DeleteMapping`
- **THEN** API 文档同步检查 SHALL 失败，并指出缺失的 `METHOD path` 条目

### Requirement: 关键路径测试与覆盖率基线

仓库 SHALL 为关键路径建立最小测试与覆盖率基线。基线至少 SHALL 覆盖：

- `artemis-system` 的 lookup 执行器与内部认证执行器
- `artemis-system` 的 infra 关键 Gateway 路径
- `artemis-auth` 的跨服务契约依赖约束
- `artemis-symphony` 的状态接口或关键运行时路径

覆盖率基线 SHALL 以 `mvn verify` 可执行的方式固化，阈值可以保守，但不得为空。

#### Scenario: 覆盖率低于基线

- **WHEN** 模块测试执行后生成的覆盖率低于仓库规定阈值
- **THEN** `mvn verify` SHALL 失败并指出违反基线的模块
