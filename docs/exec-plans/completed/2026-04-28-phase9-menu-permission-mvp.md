# Phase 9 Menu Permission MVP

状态：已完成
完成日期：2026-04-28

## 背景

`docs/system-requirements-ddd.md` 已把系统能力整理到身份认证、租户、组织权限、系统基础数据、审计、资源、工作流等多个上下文。结合当前仓库事实，`artemis-system` 已完成 lookup、用户目录、角色目录、用户角色绑定与内部授权快照，`artemis-auth` 和 `artemis-gateway` 也已能基于 `roleKeys` 形成最小 RBAC。

下一步不宜直接进入租户初始化或完整数据权限。租户套餐依赖菜单授权范围，数据权限依赖部门和角色数据范围，工作流又依赖用户、角色、部门、岗位办理人。因此本阶段先补齐菜单与权限点的最小闭环，让现有角色授权链路从“角色级”继续演进到“权限点级”。

需求来源：

- `docs/system-requirements-ddd.md`：`IAM-006 菜单和按钮权限`、权限码表、核心数据对象 `sys_menu` / `sys_role_menu`
- `docs/requirements-acceptance-checklist.md`：`UC-IAM-007 菜单与权限`、`BR-IAM-013 菜单同父级名称唯一，路由唯一`
- `docs/reports/PROJECT_PROGRESS_REPORT.md`：Phase B 建议继续补齐菜单、部门、租户中的一组核心主数据能力
- `openspec/specs/ddd-cola-layering/spec.md`：继续遵循 `client / adapter / app / domain / infra / start` 分层与依赖方向
- `openspec/specs/repository-structure/spec.md`：内部调用通过 colocated `*-client`，不绕过实现层
- `openspec/specs/lookup-tdd-testing/spec.md`：新增 CmdExe / QryExe / Gateway 时同步补单元测试与集成测试

## 目标

- 在 `artemis-system` 中建立菜单目录与角色菜单授权的最小 DDD/COLA 闭环。
- 将授权快照从 `roleKeys` 扩展到权限码 `permissionCodes`，为后续按钮权限和 gateway `checkPermission` 留出稳定入口。
- 为菜单与角色菜单授权补齐 API 文档、单元测试、集成测试与最小 smoke/验证说明。

## 非目标

- 本阶段不实现完整前端动态路由生成。
- 本阶段不实现租户套餐、租户初始化和套餐菜单同步。
- 本阶段不实现部门、岗位、数据权限或角色部门授权。
- 本阶段不实现完整按钮级业务拦截，只提供权限码目录与授权快照。
- 本阶段不迁移旧系统全量菜单数据，只保留可验证的最小初始化数据。

## 范围

- `artemis-modules/artemis-system`
  - 新增 `system_menus` 与 `system_role_menus` Flyway 迁移及测试 schema。
  - 新增 `SystemMenu` 领域模型、Gateway、DO、Repository、Converter。
  - 新增角色菜单绑定 Gateway 与用例。
  - 新增菜单 create / update / getById / listTree 或 list 能力。
  - 新增角色菜单 list / replace 能力。
  - 扩展内部授权快照，返回启用角色下的有效权限码。
- `artemis-auth`
  - 登录和刷新时同步 `permissionCodes` 到 Sa-Token 会话。
- `artemis-gateway`
  - `SessionBackedStpInterface#getPermissionList` 从会话读取权限码。
- 文档与验证
  - 新增或更新 `MENU_API.md`、`ROLE_API.md`、`INTERNAL_AUTH_API.md`、`CLIENT_CONTRACT.md`、`AUTH_API.md`、`GATEWAY_AUTHORIZATION.md`。

## 风险

- 菜单字段若一次性贴近旧系统全量结构，会把前端路由、外链、缓存、可见性等决策提前放大；本阶段应先保留核心字段。
- 授权快照 DTO 变更会影响 `artemis-system-client` 与 `artemis-auth`，需要保持向后兼容或同步测试。
- `sys_role_menu` 与后续租户套餐、数据权限存在扩展关系，本阶段要避免把租户规则写死进菜单模型。
- 当前工作树已有大量既有修改，实施时只应触碰本阶段相关文件。

## 分步任务

1. 细化菜单 MVP 字段与接口边界，确认本阶段只覆盖目录、菜单、按钮类型、父级、名称、排序、路由、组件、权限码、可见状态、启用状态。
2. 在 `artemis-system` 新增 `sys_menu`、`sys_role_menu` 迁移、测试 schema 与少量初始化数据。
3. 按 COLA 分层实现菜单目录模型、Gateway、infra 持久化和 app 执行器。
4. 实现角色菜单授权 list / replace，并校验角色存在、菜单存在、启用菜单权限码可被授权快照读取。
5. 扩展内部授权快照 DTO / 领域模型 / 查询执行器，增加 `permissionCodes`。
6. 让 `artemis-auth` 把 `permissionCodes` 写入会话，让 `artemis-gateway` 从会话读取权限列表。
7. 补齐 app 单元测试、infra 集成测试、adapter 测试、auth/gateway 关键路径测试。
8. 更新 API / 契约 / 网关授权文档，并运行增量验证。

## 验证

- `cmd.exe /c "... mvn test -pl artemis-modules/artemis-system/artemis-system-app,artemis-modules/artemis-system/artemis-system-infra,artemis-modules/artemis-system/artemis-system-adapter -am"`：通过。
- `cmd.exe /c "... mvn test -pl artemis-auth,artemis-gateway -am"`：通过。
- `cmd.exe /c "... mvn verify -pl artemis-modules/artemis-system/artemis-system-app,artemis-modules/artemis-system/artemis-system-infra,artemis-modules/artemis-system/artemis-system-adapter,artemis-auth,artemis-gateway -am"`：通过。
- 临时去 CR 的 `scripts/harness/check-client-contracts.sh`：通过。
- 临时去 CR 的 `scripts/harness/check-api-doc-sync.sh`：通过。
- `bash scripts/harness/verify-changed.sh working-tree`：被仓库脚本 CRLF 行尾阻塞，报错 `$'\r': command not found` / `pipefail\r: invalid option name`；本阶段以同范围 Maven verify 与关键同步脚本替代。

通过标准：

- 菜单目录和角色菜单授权 API 有测试覆盖。
- 登录 / 刷新后的会话同时包含 `roleKeys` 与 `permissionCodes`。
- `artemis-gateway` 的 Sa-Token permission list 不再恒为空。
- 新增文档、契约与验证脚本保持同步。

## 决策记录

- `2026-04-28`：选择菜单与权限点 MVP 作为下一小步，而不是租户或部门。原因是菜单授权直接承接现有用户、角色、授权快照和网关 RBAC，同时也是后续租户套餐和按钮权限的前置能力。
- `2026-04-28`：本阶段只做最小权限码闭环，不提前实现前端动态路由生成、租户套餐同步和数据权限，避免范围膨胀。
- `2026-04-28`：落库表名采用仓库现有 `system_*` 复数风格，即 `system_menus` / `system_role_menus`，需求文档中的 `sys_menu` / `sys_role_menu` 作为旧系统概念来源保留。

## 遗留问题

- 是否完全兼容旧系统权限码，需要在实施前以产品口径确认；默认先沿用 `docs/system-requirements-ddd.md` 中列出的主要权限码。
- 后端是否继续生成前端路由仍是待确认问题，本阶段只保留路由字段，不定义前端渲染契约。
- 租户套餐菜单树与角色菜单树可以复用同一菜单基础模型，但套餐同步规则留到后续租户阶段。
