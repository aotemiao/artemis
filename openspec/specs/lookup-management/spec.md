## ADDED Requirements

### Requirement: Lookup Type 聚合根

系统 SHALL 提供 **Lookup Type** 聚合根，表示一类参考数据（如 user_gender、order_status）。每个 Lookup Type SHALL 包含：唯一编码（code）、名称（name）、可选描述（description）、审计字段（createTime、updateTime、createBy、updateBy）及逻辑删除标识（deleted）。SHALL 对应表 `lookup_types`，主键为 id（Long）。

#### Scenario: 创建 Lookup Type

- **WHEN** 调用方提供 code、name、description 创建 Lookup Type
- **THEN** 系统 SHALL 持久化该类型，并返回包含 id 的结果；code SHALL 在系统内唯一

#### Scenario: 分页查询 Lookup Type

- **WHEN** 调用方请求 Lookup Type 分页列表（PageRequest）
- **THEN** 系统 SHALL 返回 PageResult&lt;LookupType&gt;，且不包含已逻辑删除的类型

### Requirement: Lookup Item 归属类型

系统 SHALL 提供 **Lookup Item**，表示某 Lookup Type 下的一个选项（如 value=1、label=Male）。每个 Lookup Item SHALL 包含：所属类型（lookupTypeId 或 typeCode）、选项值（value）、展示标签（label）、排序（sortOrder）、审计字段与逻辑删除。SHALL 对应表 `lookup_items`，外键指向 lookup_types；同一类型下 value SHALL 唯一。

#### Scenario: 按类型编码查询 Lookup Item 列表

- **WHEN** 调用方请求某 typeCode 下的全部 Lookup Item（如 GET /api/lookup-types/user_gender/items）
- **THEN** 系统 SHALL 返回该类型下未逻辑删除的 Item 列表，按 sortOrder 排序

### Requirement: Lookup Type 的 CRUD 与 Gateway

系统 SHALL 在 domain 层定义 **LookupTypeGateway** 接口，声明保存、按 id 查询、分页查询、按 id 删除（逻辑删除）及按 typeCode 查询 Item 列表等方法。infra 层 SHALL 使用 Spring Data JDBC 实现该 Gateway（LookupType 聚合根含 LookupItem 集合；按 typeCode 查 Item 可为独立查询）。分页 SHALL 使用 artemis-framework-core 的 PageRequest 与 PageResult。

#### Scenario: Gateway 实现持久化

- **WHEN** 应用层调用 LookupTypeGateway.save(LookupType)
- **THEN** infra 实现 SHALL 持久化 lookup_types 及关联的 lookup_items，并填充审计字段

### Requirement: Lookup REST API

系统 SHALL 提供 REST 资源：**Lookup Type** 的创建（POST）、更新（PUT）、删除（DELETE）、按 id 查询（GET）、分页查询（GET，query 参数 page、size）；**Lookup Item** 的按类型查询列表（GET /api/lookup-types/{typeCode}/items）。API 路径与 DTO 命名 SHALL 使用英文（lookup-types、lookup-items、LookupTypeDTO、LookupItemDTO）。

#### Scenario: 前端获取某类型下拉选项

- **WHEN** 客户端请求 GET /api/lookup-types/user_gender/items
- **THEN** 系统 SHALL 返回 200 及该类型下 Item 列表（含 value、label、sortOrder 等），按 sortOrder 升序
