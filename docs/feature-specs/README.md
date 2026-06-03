# Feature Specs

Status: maintained
Last Reviewed: 2026-06-02
Review Cadence: 90 days

本目录存放业务需求级 Spec，用于把模糊需求收敛成可评审、可拆解、可验证的交付输入。

## 职责

Feature Spec 回答“这次需求要交付什么”。它不同于 OpenSpec：

- Feature Spec 描述某个业务需求、用户故事、业务规则、接口影响和验收标准。
- OpenSpec 描述长期工程规则、模块边界、契约约束和质量门。
- 执行计划描述这次如何分阶段落地 Feature Spec。

## 目录约定

- `templates/feature-spec-template.md`
  新需求 Spec 模板。
- `examples/`
  可复用的示例 Spec。示例必须保持轻量，不承诺对应功能已经实现。
- `active/`
  进行中需求的 Spec。文件名建议为 `YYYY-MM-DD-<topic>.md`。
- `completed/`
  已交付需求的 Spec 归档。

当前已归档的核心业务 Spec：

- `completed/2026-06-02-user-directory-authentication.md`
- `completed/2026-06-02-role-directory-user-bindings.md`
- `completed/2026-06-02-internal-authorization-snapshot.md`
- `completed/2026-06-02-gateway-minimal-rbac.md`
- `completed/2026-06-02-menu-permission-mvp.md`
- `completed/2026-06-01-tenant-creation-initialization.md`

## 使用规则

当任务符合以下任一情况时，应先建立或更新 Feature Spec：

- 需求来自 PRD、issue、口头描述且验收标准不清晰。
- 需求涉及业务规则、数据模型、API 或跨模块协作。
- 需求需要 agent 分阶段执行，且后续需要验收映射。
- 需求会驱动新的执行计划、测试或 smoke。

小型修复、纯文档更新、纯脚本修复可以不建 Feature Spec，但最终交付说明仍需说清楚验证方式。

## 验收标准

每个非示例 Feature Spec 必须包含：

- `## 背景`
- `## 目标`
- `## 非目标`
- `## 用户故事`
- `## 业务规则`
- `## 数据与接口影响`
- `## 异常与风险场景`
- `## 工程风险评估`
- `## 验收标准`
- `## 验证映射`
- `## 关联资产`

`## 验证映射` 至少要列出一条可执行验证入口，例如单元测试、集成测试、smoke、harness 脚本或人工验收检查。

`## 工程风险评估` 用于提前暴露权限、幂等、并发、事务、异常码、SQL 性能、日志、可观测性和数据迁移风险。涉及高风险项时，应同步参考 `docs/patterns/security-review-checklist.md` 与 `docs/runbooks/RISK_BASED_VERIFICATION_RUNBOOK.md`。
