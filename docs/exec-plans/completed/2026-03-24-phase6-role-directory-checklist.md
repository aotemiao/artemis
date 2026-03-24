# Phase 6 Checklist

## 背景

本 checklist 对应 Artemis 从“用户目录可用”继续推进到“角色目录可用”的下一条主线：在 `artemis-system` 中补齐最小角色目录与用户-角色绑定能力。

范围参考：

- 本地 `RuoYi-Cloud-Plus` 的 `SysRole`、`SysUserRole` 与 `SysRoleController`
- 本地 `COLA` 的 `client / adapter / app / domain / infra / start` 分层边界

本阶段先做最小可用闭环，不一次性扩展到完整权限点、菜单树和数据权限。

## Checklist

- [x] 为 `artemis-system` 新增角色表与用户角色关系表 Flyway 迁移、测试 schema 与初始化数据
- [x] 建立角色目录领域模型、Gateway、DO、Repository 与 Converter
- [x] 新增角色目录 app 层命令 / 查询与用户角色绑定用例
- [x] 新增角色目录 REST API（create / update / getById / page）
- [x] 新增用户角色绑定 REST API（listUserRoles / replaceUserRoles）
- [x] 回写角色 API、架构与执行计划文档
- [x] 补齐 app / infra / adapter 关键路径测试并通过验证

## 完成标准

- `artemis-system` 具备最小可用的角色新增、更新、按 ID 查询与分页查询能力
- `artemis-system` 具备按用户查询角色、批量替换用户角色绑定能力
- 角色目录与用户目录共享一致的 COLA 分层、文档与验证入口
- 本阶段产物可以直接作为后续权限点、菜单和内部授权查询的基础

## 已交付产物

- `artemis-modules/artemis-system/artemis-system-start/src/main/resources/db/migration/V3__system_roles.sql`
- `artemis-modules/artemis-system/artemis-system-domain/src/main/java/com/aotemiao/artemis/system/domain/model/SystemRole.java`
- `artemis-modules/artemis-system/artemis-system-domain/src/main/java/com/aotemiao/artemis/system/domain/gateway/SystemRoleGateway.java`
- `artemis-modules/artemis-system/artemis-system-domain/src/main/java/com/aotemiao/artemis/system/domain/gateway/UserRoleBindingGateway.java`
- `artemis-modules/artemis-system/artemis-system-app/src/main/java/com/aotemiao/artemis/system/app/command/CreateSystemRoleCmdExe.java`
- `artemis-modules/artemis-system/artemis-system-app/src/main/java/com/aotemiao/artemis/system/app/command/UpdateSystemRoleCmdExe.java`
- `artemis-modules/artemis-system/artemis-system-app/src/main/java/com/aotemiao/artemis/system/app/command/ReplaceUserRolesCmdExe.java`
- `artemis-modules/artemis-system/artemis-system-app/src/main/java/com/aotemiao/artemis/system/app/query/FindSystemRoleByIdQryExe.java`
- `artemis-modules/artemis-system/artemis-system-app/src/main/java/com/aotemiao/artemis/system/app/query/SystemRolePageQryExe.java`
- `artemis-modules/artemis-system/artemis-system-app/src/main/java/com/aotemiao/artemis/system/app/query/ListUserRolesQryExe.java`
- `artemis-modules/artemis-system/artemis-system-infra/src/main/java/com/aotemiao/artemis/system/infra/gateway/SystemRoleGatewayImpl.java`
- `artemis-modules/artemis-system/artemis-system-infra/src/main/java/com/aotemiao/artemis/system/infra/gateway/UserRoleBindingGatewayImpl.java`
- `artemis-modules/artemis-system/artemis-system-adapter/src/main/java/com/aotemiao/artemis/system/adapter/web/SystemRoleController.java`
- `artemis-modules/artemis-system/ROLE_API.md`

## 验证

- `source scripts/lib/common.sh && run_mvn -pl artemis-modules/artemis-system/artemis-system-start -am verify`
- `scripts/harness/verify-changed.sh working-tree`

## 结果

Phase 6 已完成当前仓库范围内的 checklist。`artemis-system` 现在具备最小可用的角色目录与用户-角色绑定能力，且 `infra` 集成测试已切回配置项驱动的 JDBC 自动配置，后续可以直接在此基础上补内部授权快照、权限点与菜单能力。
