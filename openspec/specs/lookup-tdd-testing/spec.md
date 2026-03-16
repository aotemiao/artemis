# Spec: Lookup TDD 与测试

## Requirements

### Requirement: Lookup App 层单元测试

系统 SHALL 为 lookup 相关的 App 层执行器（CmdExe、QryExe）提供单元测试。测试 SHALL 以 LookupTypeGateway 为边界进行 mock；SHALL 验证执行器在给定 Gateway 返回或异常时的行为，以及对 Gateway 的调用参数。SHALL 覆盖 CreateLookupTypeCmdExe、UpdateLookupTypeCmdExe、DeleteLookupTypeCmdExe、FindLookupTypeByIdQryExe、LookupTypePageQryExe、GetLookupItemsByTypeCodeQryExe 至少各一个正向或关键场景。

#### Scenario: CreateLookupTypeCmdExe 调用 Gateway.save

- **WHEN** 单元测试以合法 CreateLookupTypeCmd 调用 CreateLookupTypeCmdExe.execute，且 Mock LookupTypeGateway.save 返回已持久化的 LookupType
- **THEN** 测试 SHALL 断言 execute 返回的 LookupType 与 Gateway 返回一致，且 save 被调用一次且传入的聚合根 code/name 与 Command 一致

#### Scenario: GetLookupItemsByTypeCodeQryExe 返回 Gateway 结果

- **WHEN** 单元测试以 GetLookupItemsByTypeCodeQry(typeCode) 调用 GetLookupItemsByTypeCodeQryExe.execute，且 Mock Gateway.findItemsByTypeCode 返回给定 List&lt;LookupItem&gt;
- **THEN** 测试 SHALL 断言 execute 返回的列表与 Gateway 返回一致，且 findItemsByTypeCode 被以相同 typeCode 调用

### Requirement: Lookup Gateway 实现集成测试

系统 SHALL 为 LookupTypeGateway 的 infra 实现（LookupTypeGatewayImpl）提供集成测试。测试 SHALL 使用真实或内嵌数据库，不 mock 持久化；SHALL 覆盖 save、findById、findPage、deleteById、findItemsByTypeCode 至少各一个关键路径（如：保存后能按 id 查到、按 typeCode 查到 items 且排序正确）。

#### Scenario: Gateway save 后 findById 可查

- **WHEN** 集成测试调用 LookupTypeGateway.save(LookupType) 持久化一个类型，再调用 findById(该类型 id)
- **THEN** 系统 SHALL 返回 Optional 包含该类型，且数据与保存一致

#### Scenario: findItemsByTypeCode 按 sortOrder 返回

- **WHEN** 数据库中某 typeCode 下存在多条 LookupItem（含不同 sortOrder），集成测试调用 findItemsByTypeCode(typeCode)
- **THEN** 系统 SHALL 返回该类型下未逻辑删除的 Item 列表，且按 sortOrder 升序排列

### Requirement: TDD 流程约定

系统 SHALL 遵循以下 TDD 约定：在 artemis-system 中新增或修改 CmdExe/QryExe 时，SHALL 先写或同步编写该执行器的单元测试（mock Gateway）；新增或修改 Gateway 实现时，SHALL 提供集成测试覆盖被修改或新增的 Gateway 方法关键路径。测试类命名 SHALL 与实现对应（如 *CmdExeTest、*QryExeTest、*GatewayImplTest 或 *GatewayImplIntegrationTest）。

#### Scenario: 新增 QryExe 时存在对应单元测试

- **WHEN** 代码库中在 artemis-system-app 新增一个 *QryExe 类
- **THEN** 同一模块的 src/test 下 SHALL 存在对应 *QryExeTest 类，且至少包含一个调用 execute 并断言的测试方法

#### Scenario: 修改 Gateway 实现时存在集成测试

- **WHEN** 代码库中修改 LookupTypeGatewayImpl 的某一方法实现
- **THEN** artemis-system-infra 的测试中 SHALL 存在对该方法的集成测试（或覆盖该路径的已有测试）

### Requirement: 测试技术栈与放置位置

系统 SHALL 在 artemis-system-app 的测试中使用 JUnit 5 与 Mockito；SHALL 在 artemis-system-infra 的集成测试中使用 Spring Boot Test 与内嵌 DB 或 Testcontainers（与项目现有约定一致）。App 层测试 SHALL 置于各模块 src/test/java 下与 main 同包结构；测试类 SHALL 命名为 *ExeTest 或 *GatewayImplTest（及变体）以与实现一一对应。

#### Scenario: App 模块具备测试依赖

- **WHEN** 构建 artemis-system-app
- **THEN** 其 pom SHALL 包含 junit-jupiter 与 mockito 的 test scope 依赖，且现有 Lookup 相关 *ExeTest 可被 mvn test 执行通过
