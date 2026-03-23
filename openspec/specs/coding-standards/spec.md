## ADDED Requirements

### Requirement: 不可变优先原则

值对象和 DTO SHALL 优先使用 Java `record` 类型。当 `record` 不适用时（如需要继承），SHALL 使用 `@Value`（Lombok）或手动实现不可变类。所有 DTO 字段 MUST NOT 提供 setter 方法。

#### Scenario: 值对象定义

- **WHEN** 定义用户邮箱值对象
- **THEN** SHALL 使用 `record Email(String value)` 形式，包含必要的校验逻辑在 compact constructor 中

#### Scenario: 响应 DTO 定义

- **WHEN** 定义 API 响应 DTO
- **THEN** SHALL 使用 `record UserVO(Long id, String username, String nickname)` 形式

### Requirement: Builder 模式用于复杂对象构造

当对象构造参数超过 3 个时，SHALL 使用 Builder 模式。领域实体和聚合根 SHALL 提供静态工厂方法或 Builder，MUST NOT 暴露公开无参构造器后逐个 set。

#### Scenario: 聚合根创建

- **WHEN** 创建用户聚合根
- **THEN** SHALL 通过 `User.create(CreateUserCmd cmd)` 静态工厂方法或 `User.builder().username(...).email(...).build()` 构造

### Requirement: 防御性编程与 Fail-Fast

公开方法 SHALL 在入口处校验参数，发现非法输入立即抛出异常。SHALL 使用以下方式之一：
- Bean Validation（`@NotNull`、`@NotBlank` 等）用于 DTO 层
- 自定义 `Assert` 工具类用于 domain 层
- `Objects.requireNonNull()` 用于构造器参数

MUST NOT 允许非法状态的对象通过校验点继续流转。

#### Scenario: 领域层参数校验

- **WHEN** 领域服务接收到参数
- **THEN** SHALL 在方法入口使用断言校验，无效参数立即抛出 `BizException`

### Requirement: 统一异常体系

项目 SHALL 定义以下异常层级：

- `BaseException`：抽象基类，包含 error code 和 message
- `BizException extends BaseException`：业务异常（可预期，如参数错误、业务规则违反）
- `SysException extends BaseException`：系统异常（不可预期，如数据库连接失败）

异常 SHALL 携带枚举类型的错误码（`ErrorCode`），错误码 SHALL 包含 HTTP 状态码映射。全局异常处理器 SHALL 统一捕获并转换为标准响应格式。

#### Scenario: 业务异常抛出与处理

- **WHEN** 用户名已存在时创建用户
- **THEN** 领域服务 SHALL 抛出 `BizException(ErrorCode.USER_ALREADY_EXISTS)`，全局异常处理器捕获后返回 `{"code": "USER_ALREADY_EXISTS", "message": "用户名已存在"}` 格式响应

### Requirement: 统一响应格式

所有 REST API SHALL 返回统一的响应包装结构：

```
{
  "code": "string",      // 业务状态码，成功为 "OK"
  "message": "string",   // 描述信息
  "data": <T>           // 业务数据，可为 null
}
```

分页查询 SHALL 返回包含分页信息的结构。

#### Scenario: 成功响应

- **WHEN** API 调用成功
- **THEN** SHALL 返回 `{"code": "OK", "message": "success", "data": {...}}`

#### Scenario: 分页查询响应

- **WHEN** 分页查询用户列表
- **THEN** SHALL 返回统一响应包装中的 `PageResult` 结构，默认字段至少包含 `content`、`total`、`totalPages`

### Requirement: 类命名后缀约定

项目 SHALL 遵循以下类名后缀约定：

| 层 | 类型 | 后缀 | 示例 |
|----|------|------|------|
| adapter | 控制器 | `Controller` | `UserController` |
| adapter | 请求 DTO | `Request` / `Cmd` | `CreateUserRequest` |
| adapter | 响应 DTO | `DTO` / `Response` / `VO` | `UserDTO`、`LoginResponse` |
| adapter | 转换器 | `Assembler` | `UserAssembler` |
| app | 命令执行器 | `CmdExe` | `CreateUserCmdExe` |
| app | 查询执行器 | `QryExe` | `ListUserQryExe` |
| app | 应用服务 | `AppService` | `UserAppService` |
| domain | 聚合根/实体 | 无后缀 | `User`、`Role` |
| domain | 值对象 | 无后缀 | `Email`、`PhoneNumber` |
| domain | 领域服务 | `DomainService` | `UserDomainService` |
| domain | Gateway 接口 | `Gateway` | `UserGateway` |
| domain | 领域事件 | `Event` | `UserCreatedEvent` |
| infra | Gateway 实现 | `GatewayImpl` | `UserGatewayImpl` |
| infra | 数据对象 | `DO` | `UserDO` |
| infra | Repository | `Repository` | `UserRepository` |
| infra | Mapper（仅 MyBatis 场景） | `Mapper` | `UserMapper` |
| infra | 对象转换器 | `Converter` | `UserConverter` |

#### Scenario: 按约定命名新类

- **WHEN** 在 infra 层实现角色持久化
- **THEN** 默认 SHALL 创建 `RoleGatewayImpl`（实现 `RoleGateway`）、`RoleDO`（数据对象）、`RoleRepository`（Spring Data JDBC 仓库）、`RoleConverter`（DO-Entity 转换）；若该模块已明确选择 MyBatis 持久化，则 MAY 使用 `RoleMapper`

### Requirement: 禁止跨层引用

代码中 MUST NOT 出现以下跨层引用：
- adapter 层直接 import infra 层的类
- app 层直接 import infra 层的类
- domain 层 import 任何其他层的类
- infra 层 import adapter 层或 app 层的类

此约束 SHALL 由 Maven 模块依赖关系在编译期保证。

#### Scenario: 编译期依赖检查

- **WHEN** 开发者在 app 层代码中 import infra 层的 Repository 类或 Mapper 类
- **THEN** Maven 编译 SHALL 失败，因为 app 模块未依赖 infra 模块

### Requirement: Nacos 配置动态刷新

依赖 Nacos 配置中心中可动态变更的配置项（通过 `@Value` 等注入）的 Bean SHALL 使用 `@RefreshScope`（或项目约定的等效机制），以便 Nacos 配置更新后无需重启应用即可生效。

#### Scenario: 可刷新配置 Bean

- **WHEN** 某 Bean 需要读取 Nacos 中会变更的配置（如开关、超时时间）
- **THEN** 该 Bean SHALL 标注 `@RefreshScope`，确保配置变更后下次访问时重新创建并注入新值

---

事务边界与 `spring-tx` 引入方式见 [transaction-dependency-convention](../transaction-dependency-convention/spec.md)。
