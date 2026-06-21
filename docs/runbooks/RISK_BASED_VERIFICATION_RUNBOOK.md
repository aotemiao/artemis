# Risk Based Verification Runbook

Status: maintained
Last Reviewed: 2026-06-02
Review Cadence: 90 days

本 runbook 用于按变更风险选择最小验证集，避免所有任务都跑全量验证，也避免高风险任务只跑文档检查。

## 变更分类

| 变更类型 | 典型文件 | 最小验证入口 |
|----------|----------|--------------|
| docs-only | `README.md`、`docs/**/*.md`、`openspec/**/*.md` | `scripts/harness/run-governance-checks.sh` |
| feature-spec | `docs/feature-specs/**` | `scripts/harness/check-feature-specs.sh`、`scripts/harness/check-spec-driven-delivery-chain.sh` |
| workflow-assets | `artemis-symphony/prompts/**`、`artemis-symphony/skills/**`、`WORKFLOW.md.example` | `scripts/harness/check-agentic-harness-assets.sh`、`SymphonyToolRegistryTest`（JUnit） |
| contract-api | `*-client/**`、Controller、`docs/api/**` | `scripts/harness/check-api-doc-sync.sh`、`scripts/harness/check-client-contracts.sh`、模块级 Maven verify |
| service-code | Java 源码或 POM | `scripts/harness/verify-changed.sh working-tree` |
| data-migration | migration、schema、repository、gateway | 相关 infra 集成测试、模块级 Maven verify |
| smoke-needed | 服务启动、网关、认证、Nacos、Docker | 对应 `scripts/smoke/*.sh` 或 runbook 人工验收 |
| security-sensitive | 权限、认证、事务、锁、SQL、审计 | `docs/patterns/security-review-checklist.md` + 对应测试 / smoke / harness |

## 默认规则

- 小型文档改动可以只跑 governance，但如果改了稳定规则，仍需 OpenSpec 同步检查。
- 涉及 Java 或 POM 的改动优先使用 `scripts/harness/verify-changed.sh working-tree`。
- 涉及根 POM、Surefire、Failsafe、JaCoCo、Mockito 或测试 JVM 参数的改动，至少先跑触发失败的模块级 Maven verify，再跑 `scripts/harness/full-verify.sh`。
- 涉及测试 fake、临时目录、网络、SSH、HOME 展开或外部 transport 的改动，必须在受限环境下跑对应模块级 Maven verify，确认默认测试不依赖本地端口、真实 HOME、外部网络或真实 SSH。
- 涉及跨模块、高风险权限或数据一致性的改动使用 `scripts/harness/full-verify.sh`。
- 如果无法自动化验证，handoff 必须说明人工验收范围和剩余风险。

## Handoff 要求

最终说明至少列出：

- 变更分类。
- 实际执行的验证命令。
- 未执行推荐验证的原因。
- 高风险项对应的安全审查结论。
