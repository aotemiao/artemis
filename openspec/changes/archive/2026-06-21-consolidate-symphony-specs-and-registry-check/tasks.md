## 1. 合并碎片 Symphony spec

- [x] 1.1 将 4 个碎片 spec 的 Requirement 合并进 `symphony-runtime-alignment`
- [x] 1.2 删除 4 个碎片能力目录，确认无外部按名引用

## 2. registry 校验转 JUnit

- [x] 2.1 新增 `SymphonyToolRegistryTest`（registry 契约 + doc<->code 错误码同步）
- [x] 2.2 `mvn test -Dtest=SymphonyToolRegistryTest` 通过
- [x] 2.3 删除 `check-symphony-assets.sh`，资产存在并入 `check-agentic-harness-assets.sh`
- [x] 2.4 从 `run-governance-checks.sh` 与 `.githooks/pre-commit` 移除 Symphony Assets

## 3. 文档与规范

- [x] 3.1 更新 spec / runbook / 模板 / 文档中的 `check-symphony-assets.sh` 引用
- [x] 3.2 增加本 OpenSpec change 轨迹

## 4. 验证

- [x] 4.1 受影响治理检查实跑通过；`bash -n` / `sh -n` 通过
- [ ] 4.2 CI 全量 `run-governance-checks.sh` + `full-verify.sh`
