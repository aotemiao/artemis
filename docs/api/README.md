# API Docs Index

Status: maintained
Last Reviewed: 2026-05-29
Review Cadence: 90 days

这个目录是 Artemis 对外 REST API 文档的统一入口，面向前端开发、联调测试与调用方排障。

当前各模块文档仍以“路由、用途、请求字段、返回概要”为主，尚未达到完整 OpenAPI / Swagger 级别。本入口先统一整理可用文档、补齐通用调用约定，并定义后续渐进增强的补齐标准。

## 使用方式

建议按下面顺序阅读：

1. 先看本文件的“通用约定”，确认鉴权、分页、状态码与错误处理规则。
2. 再进入具体模块文档，查看路由、用途与当前已沉淀的字段说明。
3. 如果接口涉及网关鉴权或上下文透传，同时阅读 `artemis-gateway/GATEWAY_AUTHORIZATION.md`。

## 文档入口

### 认证服务

- [认证服务 API](../../artemis-auth/AUTH_API.md)
  认证、登出、在线用户、注册、刷新、动态租户切换。

### 系统服务

- [字典类型 API](../../artemis-modules/artemis-system/LOOKUP_API.md)
  字典类型与字典项查询。
- [系统用户 API](../../artemis-modules/artemis-system/USER_API.md)
  用户目录、角色绑定。
- [系统角色 API](../../artemis-modules/artemis-system/ROLE_API.md)
  角色目录、菜单权限、数据权限。
- [菜单 API](../../artemis-modules/artemis-system/MENU_API.md)
  菜单树、权限点维护。
- [参数配置 API](../../artemis-modules/artemis-system/CONFIG_API.md)
  系统参数配置。
- [通知公告 API](../../artemis-modules/artemis-system/NOTICE_API.md)
  公告管理。
- [部门 API](../../artemis-modules/artemis-system/DEPARTMENT_API.md)
  部门树与数据权限基础数据。
- [岗位 API](../../artemis-modules/artemis-system/POST_API.md)
  岗位目录。
- [租户应用 API](../../artemis-modules/artemis-system/CLIENT_API.md)
  OAuth 客户端或租户应用配置。
- [登录日志 API](../../artemis-modules/artemis-system/LOGIN_INFO_API.md)
  登录日志查询。
- [操作日志 API](../../artemis-modules/artemis-system/OPER_LOG_API.md)
  操作日志查询。
- [租户 API](../../artemis-modules/artemis-system/TENANT_API.md)
  租户管理。
- [租户套餐 API](../../artemis-modules/artemis-system/TENANT_PACKAGE_API.md)
  租户套餐管理。
- [内部认证 API](../../artemis-modules/artemis-system/INTERNAL_AUTH_API.md)
  仅供内部服务调用，不经网关对外暴露。

### 资源服务

- [资源服务 API](../../artemis-modules/artemis-resource/SERVICE_API.md)
  OSS 文件、OSS 配置、站内消息。

### 工作流服务

- [工作流服务 API](../../artemis-modules/artemis-workflow/SERVICE_API.md)
  流程分类、SpEL 表达式、流程定义。

### 网关与鉴权补充

- [网关授权约定](../../artemis-gateway/GATEWAY_AUTHORIZATION.md)
  白名单、登录后透传头、最小 RBAC、内部接口阻断规则。

## 通用约定

### 基础路径

- 网关统一入口默认是 `http://localhost:8080`。
- 认证服务接口默认经网关暴露为 `/auth/**`。
- 业务服务接口默认经网关暴露为 `/api/**`。
- 直接连服务实例调试时，路径前缀通常不变，但端口会切换到各服务自己的 `server.port`。

### 鉴权方式

- 除白名单路径外，所有网关接口默认要求登录。
- 当前白名单见 [网关授权约定](../../artemis-gateway/GATEWAY_AUTHORIZATION.md)。
- 已登录请求通常通过 Sa-Token 的 token 机制鉴权；联调时应携带登录接口返回的 token。
- 网关会向下游服务透传：
  - `X-User-Id`
  - `X-Role-Keys`
- 超级管理员相关接口可能额外依赖 `super-admin` 角色。
- 多租户请求可通过请求体中的 `tenantId` 或 `X-Tenant-Id` 头指定租户，具体以模块文档说明为准。

### 请求与响应格式

- 请求体默认使用 `application/json`，文件上传接口除外。
- 文件上传接口通常使用 `multipart/form-data`。
- 成功响应以 JSON 为主，下载类接口会直接返回二进制流。
- 当前各模块文档对统一响应包裹结构描述仍不完整；联调时应优先以实际 Controller 返回类型和前端网关联调结果为准，并逐步把样例回写到模块文档。

### 分页规范

当前已公开的分页接口普遍使用以下查询参数：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `page` | `integer` | 否 | 页码，通常从 `1` 开始 |
| `size` | `integer` | 否 | 每页条数 |

约定：

- 如模块文档未额外说明，分页列表接口应至少在说明中标注 `page`、`size`。
- 后续补齐样例时，应补出实际返回中的列表字段、总数、当前页和每页大小。
- 若未来新增排序、筛选字段，应在对应模块文档中单独列出，不在本总入口做猜测性约束。

分页请求示例：

```http
GET /api/users?page=1&size=20 HTTP/1.1
Host: localhost:8080
Authorization: Bearer <token>
```

分页响应示例（字段名以实际实现为准，先作为文档补齐模板）：

```json
{
  "code": 200,
  "message": "OK",
  "data": {
    "records": [
      {
        "id": 1001,
        "username": "admin",
        "displayName": "系统管理员"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 20
  }
}
```

### 状态码约定

当前仓库文档已明确或可稳定推断的状态码如下：

| 状态码 | 含义 | 适用场景 |
|--------|------|----------|
| `200` | 请求成功 | 查询、修改成功并返回 JSON 数据 |
| `201` | 已创建 | 新增成功且明确使用创建语义时，后续应在模块文档中确认 |
| `204` | 无响应体成功 | 例如 `POST /auth/logout` |
| `400` | 请求参数错误 | 参数校验失败、格式错误 |
| `401` | 未登录或 token 无效 | 访问非白名单接口但未通过鉴权 |
| `403` | 已登录但无权限 | 角色不足，或内部接口被网关阻断 |
| `404` | 资源不存在 | 按 ID 查询的数据不存在 |
| `409` | 资源冲突 | 唯一键冲突、状态冲突等 |
| `500` | 服务内部错误 | 未分类异常 |

说明：

- 当前并非所有模块文档都已显式声明状态码；本表作为统一联调约定与补齐目标。
- 如果某接口已有更精确的状态码约束，应以模块文档为准，并同步回写到本目录。

### 错误码与错误响应

当前仓库已经存在业务错误场景，但错误码字典尚未汇总成统一文档。补齐前先采用以下约定：

- 模块文档至少应说明：
  - 常见参数校验失败场景
  - 常见业务冲突场景
  - 鉴权失败场景
- 后续新增或补全文档时，建议使用下列模板记录错误响应：

```json
{
  "code": "SYSTEM_USER_NOT_FOUND",
  "message": "指定用户不存在",
  "details": null,
  "traceId": "8f0f6d7f5fca4f0c"
}
```

如果当前实现返回的是数值型业务码或通用错误结构，也应按实际返回补入模块文档，不要求所有模块立刻统一格式。

### 示例请求与响应补齐规则

后续补全文档时，建议每个公开接口至少补齐以下信息：

| 项目 | 要求 |
|------|------|
| 用途 | 一句话说明接口解决什么问题 |
| 鉴权 | 是否需要登录、角色要求、是否支持 `X-Tenant-Id` |
| 请求路径 | 包含路径变量说明 |
| 查询参数 | 名称、类型、是否必填、含义 |
| 请求体 | 字段名、类型、是否必填、示例值 |
| 成功响应 | 状态码、响应字段、示例 JSON |
| 错误响应 | 常见状态码、业务错误示例 |

推荐模板：

~~~md
## POST /api/example

- 用途：创建示例资源
- 鉴权：需要登录；要求 `super-admin`
- 请求头：
  - `Authorization: Bearer <token>`
  - `X-Tenant-Id: 000000`

### 请求体

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `name` | `string` | 是 | 名称 | `示例名称` |
| `enabled` | `boolean` | 否 | 是否启用 | `true` |

### 成功响应

- 状态码：`200`

```json
{
  "code": 200,
  "message": "OK",
  "data": {
    "id": 1,
    "name": "示例名称",
    "enabled": true
  }
}
```

### 错误响应

| 状态码 | 说明 |
|--------|------|
| `400` | 请求字段校验失败 |
| `409` | 名称已存在 |
~~~

## 当前补齐优先级

建议优先补齐下列文档，因为它们最常被前端、网关联调或跨服务调用方使用：

1. `artemis-auth/AUTH_API.md`
2. `artemis-modules/artemis-system/USER_API.md`
3. `artemis-modules/artemis-system/ROLE_API.md`
4. `artemis-modules/artemis-system/MENU_API.md`
5. `artemis-modules/artemis-system/LOOKUP_API.md`

原因：

- 这些接口覆盖登录、权限、基础目录与前端初始化的核心链路。
- 现有文档已经有稳定路由清单，适合按模板渐进补示例，不需要先重构 Controller。

## 与现有守门的关系

- 路由级同步检查仍以各模块现有 `*_API.md` / `SERVICE_API.md` 为准。
- `docs/api/README.md` 负责提供统一入口和通用联调约定，不替代模块级路由事实来源。
- 当模块文档补充请求示例、字段类型和错误说明时，必须保留 `ROUTE: METHOD /path` 行，确保 `scripts/harness/check-api-doc-sync.sh` 继续可用。
