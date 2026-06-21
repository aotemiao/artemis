## Why

Harness 自度量子系统（harness metrics 记分牌、GitHub delivery signal 采集、agent eval 草稿生成）与配套的文档 ceremony 守门（doc-consistency 魔法字符串校验、doc-freshness 时间戳新鲜度校验）形成了“治理治理自身”的闭环：约 2,950 行 shell 只为产出并校验关于 harness 自身的报告，没有任何人类或应用据此决策。其中 doc-freshness 还按当天日期判定每份文档是否超过 review cadence，到期即让 CI / pre-commit 失败，制造周期性的“刷新时间戳” make-work。这些资产硬依赖 `python3`，在部分开发机（如 Windows）上根本无法运行，违背“多系统可运行”目标；其对脚本、文档字符串和逐 dataset 内容的严苛钉死，又使 harness 自身难以演进。

## What Changes

- 删除自度量脚本：`generate-harness-metrics-report.sh`、`check-harness-metrics-report.sh`、`generate-ci-harness-metrics.sh`、`collect-github-delivery-signal.sh`、`generate-agent-eval-drafts.sh`。
- 删除文档 ceremony 守门：`check-doc-consistency.sh`（魔法字符串钉死）、`check-doc-freshness.sh`（时间戳定时炸弹）。
- 将 466 行的 `check-agentic-harness-assets.sh` 重写为约 35 行可移植 POSIX sh，只校验关键入口资产与模板必备小节，去除对已删机器与各文档字符串的钉死。
- `run-governance-checks.sh` 改写为 POSIX sh，去掉 Docs Consistency / Docs Freshness / Harness Metrics Report 三项子检查。
- 从 `.github/workflows/verify.yml`、`.github/workflows/governance.yml` 移除 metrics / delivery-signal 采集与上传步骤。
- 将 `scripts/e2e/run-symphony-agent-eval.sh` 从已删的 metrics 生成器解耦，保留其确定性行为评测能力。
- 删除随之失效的文档与注册表：`docs/reports/harness-metrics/`、`docs/governance/DOC_FRESHNESS_POLICY.md`、`docs/asset-manifest.yml`；精简 `AGENTS.md` 脚本目录及相关文档引用。

## Capabilities

### Modified Capabilities

- `harness-governance`：移除“生成 Harness 指标报告”“CI 生成并上传 Harness 指标快照”“检查 Harness 文档证据 freshness”“从 GitHub Actions event 采集低敏 delivery signal”“从失败 agent run 生成 eval dataset 草稿”五个场景；资产必备清单不再包含 harness metrics report generator；保留的 Symphony memory eval 场景不再断言 harness metrics 快照与权限姿态一致性。

## Impact

- 不影响业务服务、公共框架、网关、认证服务或 Symphony 的运行时能力，也不改变内部 Dubbo 契约或对外 REST API。
- 治理脚本层从约 5,501 行降到约 2,550 行；harness 核心治理入口不再把 `python3` 作为硬前置，可在 Linux / macOS / Windows Git Bash 下运行。
- 保留全部真实质量门：构建 / 测试 / ArchUnit / Checkstyle / Spotless、契约与结构守门、OpenSpec 同步、冒烟与真实链路 e2e。
- 历史 completed 文档中对已删脚本的描述作为带日期的记录保留，不回改。
