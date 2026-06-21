## 1. 核对覆盖

- [x] 1.1 逐一核对 26 个 dataset 场景与 JUnit 方法的对应关系
- [x] 1.2 为 hook 失败盲区补 `AgentRunnerTest` 两个单元测试
- [x] 1.3 `mvn test -pl artemis-symphony-orchestrator -Dtest=AgentRunnerTest` 全绿（6/6）

## 2. 删除冗余评测层

- [x] 2.1 删除 `scripts/e2e/run-symphony-agent-eval.sh` 与 `scripts/harness/run-agent-evals.sh`
- [x] 2.2 删除 `docs/agent-evals/`（dataset + fixture + README）

## 3. 规范与文档

- [x] 3.1 从 `openspec/specs/harness-governance/spec.md` 移除 memory eval 场景与资产项
- [x] 3.2 更新入口/索引/验证映射文档中的评测层引用
- [x] 3.3 增加本 OpenSpec change 轨迹

## 4. 验证

- [x] 4.1 受影响治理检查（doc-links / agentic-harness-assets / agent-run-summaries / spec-driven-delivery-chain / feature-specs / symphony-assets）实跑通过
- [ ] 4.2 CI 全量 `run-governance-checks.sh` + `full-verify.sh`（含 `mvn verify`）
