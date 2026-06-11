# Doc Freshness Policy

Status: maintained
Last Reviewed: 2026-06-10
Review Cadence: 90 days

本文件用于把“文档不要漂移”这件事变成仓库内的明确规则，而不是依赖维护者记忆。

## 适用范围

当前纳入 freshness 守门的核心文档包括：

- `README.md`
- `AGENTS.md`
- `ARCHITECTURE.md`
- `QUALITY_SCORE.md`
- `docs/README.md`
- `docs/api/README.md`
- `docs/feature-specs/README.md`
- `docs/feature-specs/examples/menu-permission-feature-spec.md`
- `docs/feature-specs/completed/2026-06-01-tenant-creation-initialization.md`
- `docs/exec-plans/README.md`
- `docs/patterns/README.md`
- `docs/patterns/spec-to-validation-mapping.md`
- `docs/patterns/agent-delivery-handoff.md`
- `docs/governance/CHECKLIST.md`
- `docs/reports/ROADMAP.md`
- `docs/reports/PROJECT_PROGRESS_REPORT.md`
- `docs/runbooks/SERVICE_SMOKE_RUNBOOK.md`
- `docs/runbooks/ADD_DOMAIN_SERVICE_RUNBOOK.md`
- `docs/runbooks/ADD_DUBBO_CLIENT_RUNBOOK.md`
- `docs/runbooks/ADD_ARCHUNIT_RULE_RUNBOOK.md`
- `docs/agent-workflow/AGENT_DEVELOPMENT_WORKFLOW.md`
- `docs/agent-workflow/AGENT_REVIEW_LOOP.md`
- `docs/runbooks/DEPLOY_AND_ROLLBACK_RUNBOOK.md`
- `docs/runbooks/SYMPHONY_TROUBLESHOOTING.md`
- `docs/governance/QUALITY_ISSUE_STANDARD.md`
- `docs/governance/quality-issues/README.md`
- `docs/governance/quality-issues/archive/2026-03-23-credential-request-duplication.md`
- `docs/reports/deploy-drills/README.md`
- `docs/reports/deploy-drills/2026-06-01-sample-report-template.md`

## 必须满足的规则

1. 每份核心文档都要在文件头部包含：
   `Status:`
   `Last Reviewed: YYYY-MM-DD`
   `Review Cadence: <days> days`
2. `Last Reviewed` 不能早于当前日期，也不能超过声明的 `Review Cadence`
3. `Review Cadence` 当前上限固定为 `90 days`
4. 相关脚本、CI、runbook 或入口文档发生结构变化时，同步回写对应文档的审阅日期

## 证据 freshness

文档 freshness 不能只靠 `Last Reviewed`。当文档声明某个 Harness、Symphony、agent eval、metrics、部署演练或运行摘要能力已经可用时，还必须能指向仓库内可重复的验证入口和低敏证据边界。

关键治理文档至少应说明：

- 可复跑的验证入口，例如 `scripts/harness/verify-changed.sh working-tree`、`scripts/e2e/run-symphony-agent-eval.sh all` 或对应专项检查脚本。
- 证据来源，例如 `artifacts/agent-evals/`、`artifacts/harness-metrics/`、`artifacts/harness-delivery-signals/` 或 `docs/reports/deploy-drills/` 中的摘要。
- 低敏边界，例如不提交完整 prompt、聊天记录、工具输出、外部系统响应、密钥、用户名、home 目录或完整环境变量。

`scripts/harness/check-doc-freshness.sh` 会对核心 Harness / Symphony 报告做轻量 evidence marker 检查，避免只更新审阅日期却没有可验证运行证据的“假新鲜”。

## 守门入口

- 本地：`scripts/harness/check-doc-freshness.sh`
- 全量验证：`scripts/harness/full-verify.sh`
- CI：`.github/workflows/verify.yml`

## 周期性巡检

GitHub Actions `verify` 工作流已增加每周巡检触发，用于重复执行文档 freshness、链接、一致性、Maven 验证与镜像构建检查。
