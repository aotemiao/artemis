# Phase 5 Checklist

## 背景

本 checklist 对应 Artemis 从“平台骨架阶段”进入“业务落地阶段”的第一条主线：把 `artemis-system` 的固定账号凭证 stub 升级为真实用户目录 MVP，并让 `artemis-auth` 继续复用既有契约完成认证。

## Checklist

- [x] 归档已完成的 Phase 1-4 计划与演进计划，并更新 docs 索引
- [x] 为 `artemis-system` 新增用户表 Flyway 迁移与测试 schema
- [x] 建立用户目录领域模型、Gateway、DO、Repository 与 Converter
- [x] 新增用户目录 app 层命令 / 查询执行器
- [x] 新增用户目录 REST API（create / update / getById / page）
- [x] 用真实用户表替换 `UserCredentialsGatewayImpl` 中的固定账号 stub
- [x] 回写用户 API / 内部认证 API / 架构与计划文档
- [x] 补齐 app / infra / adapter / auth 关键路径测试并通过验证

## 完成标准

- `artemis-system` 不再依赖硬编码账号完成认证校验
- `artemis-auth -> artemis-system-client -> artemis-system` 契约保持兼容
- 用户目录具备最小可用的新增、更新、按 ID 查询与分页查询能力
- 文档、脚本、治理与验证入口与本次改动保持同步

## 已交付产物

- `artemis-modules/artemis-system/artemis-system-start/src/main/resources/db/migration/V2__system_users.sql`
- `artemis-modules/artemis-system/artemis-system-domain/src/main/java/com/aotemiao/artemis/system/domain/model/SystemUser.java`
- `artemis-modules/artemis-system/artemis-system-domain/src/main/java/com/aotemiao/artemis/system/domain/gateway/SystemUserGateway.java`
- `artemis-modules/artemis-system/artemis-system-app/src/main/java/com/aotemiao/artemis/system/app/command/CreateSystemUserCmdExe.java`
- `artemis-modules/artemis-system/artemis-system-app/src/main/java/com/aotemiao/artemis/system/app/command/UpdateSystemUserCmdExe.java`
- `artemis-modules/artemis-system/artemis-system-app/src/main/java/com/aotemiao/artemis/system/app/query/FindSystemUserByIdQryExe.java`
- `artemis-modules/artemis-system/artemis-system-app/src/main/java/com/aotemiao/artemis/system/app/query/SystemUserPageQryExe.java`
- `artemis-modules/artemis-system/artemis-system-infra/src/main/java/com/aotemiao/artemis/system/infra/gateway/SystemUserGatewayImpl.java`
- `artemis-modules/artemis-system/artemis-system-adapter/src/main/java/com/aotemiao/artemis/system/adapter/web/SystemUserController.java`
- `artemis-modules/artemis-system/USER_API.md`
- `docs/exec-plans/completed/2026-03-24-phase1-checklist.md`
- `docs/exec-plans/completed/2026-03-24-phase2-checklist.md`
- `docs/exec-plans/completed/2026-03-24-phase3-checklist.md`
- `docs/exec-plans/completed/2026-03-24-phase4-checklist.md`

## 验证

- `scripts/harness/verify-changed.sh working-tree`
- `scripts/harness/full-verify.sh`

## 结果

Phase 5 已完成当前仓库范围内的 checklist。`artemis-system` 现在具备最小可用的用户目录与真实凭证校验，`artemis-auth` 继续通过既有内部契约完成认证，下一步可直接进入角色目录与用户-角色关系的业务补齐。
