# Design

## 分层

新增 Feature Spec 层后，需求到交付链路变为：

1. `docs/feature-specs/` 描述业务需求与验收标准。
2. `docs/exec-plans/` 描述复杂任务如何分阶段落地。
3. `openspec/` 描述长期工程规则如何变化。
4. `scripts/harness/check-feature-specs.sh` 检查 Feature Spec 和可复用资产的最低结构。
5. Symphony prompt / skill 引导 agent 在 handoff 中回写验收映射与验证证据。

## 设计取舍

- Feature Spec 不放入 OpenSpec，因为它描述单个业务需求，不是长期工程规则。
- OpenSpec 新增 `spec-driven-delivery`，只记录“什么时候必须建立 Feature Spec、必须映射验证入口”等长期规则。
- 守门脚本先做结构性检查，不试图理解业务语义，避免把治理脚本做成脆弱的自然语言解析器。

## 验证

- `scripts/harness/check-feature-specs.sh`
- `scripts/harness/run-governance-checks.sh`
- `scripts/harness/verify-changed.sh working-tree`
