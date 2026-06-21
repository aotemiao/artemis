## 1. 删除自度量与 ceremony 脚本

- [x] 1.1 删除 metrics / delivery-signal / eval-draft 脚本（5 个）
- [x] 1.2 删除 `check-doc-consistency.sh` 与 `check-doc-freshness.sh`

## 2. 精简保留的守门入口

- [x] 2.1 将 `check-agentic-harness-assets.sh` 重写为可移植 POSIX sh
- [x] 2.2 将 `run-governance-checks.sh` 改写为 POSIX sh 并移除三项 ceremony 检查
- [x] 2.3 从 verify / governance workflow 移除 metrics 步骤
- [x] 2.4 将 `run-symphony-agent-eval.sh` 从 metrics 生成器解耦

## 3. 文档与规范

- [x] 3.1 删除 `docs/reports/harness-metrics/`、`docs/governance/DOC_FRESHNESS_POLICY.md`、`docs/asset-manifest.yml`
- [x] 3.2 精简 `AGENTS.md` 脚本目录，更新 `README.md` / `docs/README.md` / eval 文档引用
- [x] 3.3 更新 `openspec/specs/harness-governance/spec.md`
- [x] 3.4 增加本 OpenSpec change 轨迹

## 4. 验证

- [x] 4.1 `bash -n` 全量脚本语法检查通过
- [x] 4.2 无可执行死引用、无 markdown 死链
- [ ] 4.3 在具备 python3 的环境运行 `scripts/harness/run-governance-checks.sh` 与 `scripts/harness/full-verify.sh`（本机 python3 为 Windows Store stub，需在 CI / Linux 验证）
