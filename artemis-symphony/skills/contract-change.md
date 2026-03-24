# 契约变更 Skill

适用场景：需要修改 `*-client`、`artemis-api-*`、Dubbo DTO 或对外 API 文档。

## 先读

- `openspec/specs/repository-structure/spec.md`
- `openspec/specs/contract-doc-guardrails/spec.md`
- `docs/harness-engineering/ADD_DUBBO_CLIENT_RUNBOOK.md`

## 输出要求

- 同步更新 Java 契约、API 文档与 `CLIENT_CONTRACT.md`
- 说明是否涉及兼容性变化
- 至少执行 `scripts/harness/check-api-doc-sync.sh` 与 `scripts/harness/check-client-contracts.sh`
