#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

contains_fixed_string() {
  local pattern="$1"
  local file="$2"
  if command -v rg >/dev/null 2>&1 && rg --version >/dev/null 2>&1; then
    rg -q --fixed-strings -- "$pattern" "$file"
    return
  fi
  grep -Fq -- "$pattern" "$file"
}

require_file_contains() {
  local file="$1"
  local pattern="$2"
  require_repo_path_exact "$file"
  if ! contains_fixed_string "$pattern" "$file"; then
    echo "Missing required content in $file: $pattern" >&2
    exit 1
  fi
}

print_step "Checking agentic harness assets"

required_files=(
  "docs/patterns/security-review-checklist.md"
  "docs/runbooks/AGENT_PERMISSION_RUNBOOK.md"
  "docs/runbooks/RISK_BASED_VERIFICATION_RUNBOOK.md"
  "docs/security/THREAT_MODEL.md"
  "docs/governance/DOC_FRESHNESS_POLICY.md"
  "docs/agent-evals/README.md"
  "docs/agent-evals/datasets/memory-requirement-intake.yml"
  "docs/agent-evals/datasets/memory-feature-spec-required.yml"
  "docs/agent-evals/datasets/memory-real-tenant-package-sync.yml"
  "docs/agent-evals/datasets/memory-execution-plan-required.yml"
  "docs/agent-evals/datasets/memory-openspec-change-required.yml"
  "docs/agent-evals/datasets/memory-adversarial-review-required.yml"
  "docs/agent-evals/datasets/memory-adversarial-review-independent.yml"
  "docs/agent-evals/datasets/memory-docs-governance.yml"
  "docs/agent-evals/datasets/memory-permission-preflight.yml"
  "docs/agent-evals/datasets/memory-network-access-allowed.yml"
  "docs/agent-evals/datasets/memory-writable-root-preflight.yml"
  "docs/agent-evals/datasets/memory-after-create-hook-failure.yml"
  "docs/agent-evals/datasets/memory-before-run-hook-failure.yml"
  "docs/agent-evals/datasets/memory-codex-turn-failure.yml"
  "docs/agent-evals/datasets/memory-codex-turn-response-timeout.yml"
  "docs/agent-evals/datasets/memory-codex-turn-timeout.yml"
  "docs/agent-evals/datasets/memory-codex-turn-cancelled.yml"
  "docs/agent-evals/datasets/memory-codex-approval-required.yml"
  "docs/agent-evals/datasets/memory-codex-dynamic-tool-failure.yml"
  "docs/agent-evals/datasets/memory-codex-user-input-required.yml"
  "docs/agent-evals/datasets/memory-codex-malformed-event.yml"
  "docs/agent-evals/datasets/memory-codex-startup-failure.yml"
  "docs/agent-evals/datasets/memory-linear-comment-writeback.yml"
  "docs/agent-evals/datasets/memory-linear-comment-failure-audit.yml"
  "docs/agent-evals/fixtures/ambiguous-business-request.yml"
  "docs/agent-evals/fixtures/high-risk-permission-change.yml"
  "docs/reports/agent-runs/README.md"
  "docs/reports/agent-runs/2026-06-09-harness-market-gap-improvements.md"
  "docs/reports/harness-metrics/README.md"
  "docs/reports/deploy-drills/README.md"
  "artemis-symphony/prompts/adversarial-review.md"
  "artemis-symphony/skills/adversarial-review.md"
  "artemis-symphony/tools/README.md"
  "artemis-symphony/tools/registry.json"
  "scripts/harness/check-openspec-change-state.sh"
  "scripts/harness/check-agent-run-summaries.sh"
  "scripts/harness/generate-agent-eval-drafts.sh"
  "scripts/harness/run-agent-evals.sh"
  "scripts/harness/generate-harness-metrics-report.sh"
  "scripts/harness/generate-ci-harness-metrics.sh"
  "scripts/harness/collect-github-delivery-signal.sh"
  "scripts/harness/check-harness-metrics-report.sh"
  "scripts/harness/check-deploy-drill-reports.sh"
  "scripts/harness/check-doc-freshness.sh"
  "scripts/e2e/run-symphony-agent-eval.sh"
  "docs/asset-manifest.yml"
  ".github/copilot-instructions.md"
  "CLAUDE.md"
  "GEMINI.md"
)

for file in "${required_files[@]}"; do
  require_repo_path_exact "$file"
done

if ! repo_path_exists_exact "docs/exec-plans/active/2026-06-02-agentic-harness-optimization.md" \
  && ! repo_path_exists_exact "docs/exec-plans/completed/2026-06-02-agentic-harness-optimization.md"; then
  echo "Missing agentic harness optimization execution plan in active or completed." >&2
  exit 1
fi

require_file_contains "docs/feature-specs/templates/feature-spec-template.md" "## 异常与风险场景"
require_file_contains "docs/feature-specs/templates/feature-spec-template.md" "## 工程风险评估"
require_file_contains "docs/exec-plans/templates/execution-plan-template.md" "## 风险分类"
require_file_contains "docs/exec-plans/templates/execution-plan-template.md" "## 验证分类"
require_file_contains "docs/patterns/security-review-checklist.md" "权限、幂等、锁、事务、异常处理、SQL 性能、日志和可观测性"
require_file_contains "docs/runbooks/AGENT_PERMISSION_RUNBOOK.md" "## 权限矩阵"
require_file_contains "docs/runbooks/RISK_BASED_VERIFICATION_RUNBOOK.md" "## 变更分类"
require_file_contains "docs/security/THREAT_MODEL.md" "## 主要威胁"
require_file_contains "docs/agent-evals/README.md" "scripts/harness/run-agent-evals.sh"
require_file_contains "docs/agent-evals/README.md" "scripts/e2e/run-symphony-agent-eval.sh"
require_file_contains "docs/agent-evals/README.md" "scripts/e2e/run-symphony-agent-eval.sh all"
require_file_contains "docs/agent-evals/README.md" "harness-metrics/latest.json"
require_file_contains "docs/agent-evals/README.md" "agent_runs.permission_posture"
require_file_contains "docs/agent-evals/README.md" "expected_failure_category"
require_file_contains "docs/agent-evals/README.md" "expected_external_effects"
require_file_contains "docs/agent-evals/README.md" "expected_external_effect_details"
require_file_contains "docs/agent-evals/README.md" "expected_summary_workspace_files"
require_file_contains "docs/agent-evals/README.md" "codex.event_counts"
require_file_contains "docs/agent-evals/README.md" "linear_comment_reporting_enabled"
require_file_contains "docs/agent-evals/README.md" "adversarial_review_enabled"
require_file_contains "docs/agent-evals/README.md" "expected_summary_dispatch_kinds"
require_file_contains "docs/agent-evals/README.md" "generate-agent-eval-drafts.sh"
require_file_contains "docs/agent-evals/README.md" "artifacts/agent-eval-drafts"
require_file_contains "docs/agent-evals/README.md" "草稿必须人工复核"
require_file_contains "docs/agent-evals/README.md" "会拒绝仍带草稿元信息"
require_file_contains "docs/agent-evals/README.md" "必须等于文件名"
require_file_contains "docs/agent-evals/datasets/memory-requirement-intake.yml" "prompt_must_contain"
require_file_contains "docs/agent-evals/datasets/memory-requirement-intake.yml" "agent-requirement-intake.md"
require_file_contains "docs/agent-evals/datasets/memory-feature-spec-required.yml" "prompt_must_contain"
require_file_contains "docs/agent-evals/datasets/memory-feature-spec-required.yml" "docs/feature-specs/active"
require_file_contains "docs/agent-evals/datasets/memory-feature-spec-required.yml" "spec-driven-delivery.md"
require_file_contains "docs/agent-evals/datasets/memory-real-tenant-package-sync.yml" "docs/feature-specs/completed/2026-06-01-tenant-creation-initialization.md"
require_file_contains "docs/agent-evals/datasets/memory-real-tenant-package-sync.yml" "TENANT_PACKAGE_API.md"
require_file_contains "docs/agent-evals/datasets/memory-real-tenant-package-sync.yml" "租户套餐变更同步"
require_file_contains "docs/agent-evals/datasets/memory-execution-plan-required.yml" "prompt_must_contain"
require_file_contains "docs/agent-evals/datasets/memory-execution-plan-required.yml" "docs/exec-plans/active"
require_file_contains "docs/agent-evals/datasets/memory-execution-plan-required.yml" "execution-plan-template.md"
require_file_contains "docs/agent-evals/datasets/memory-openspec-change-required.yml" "prompt_must_contain"
require_file_contains "docs/agent-evals/datasets/memory-openspec-change-required.yml" "openspec/changes"
require_file_contains "docs/agent-evals/datasets/memory-openspec-change-required.yml" "Stable rule change"
require_file_contains "docs/agent-evals/datasets/memory-adversarial-review-required.yml" "spec_driven_delivery_enabled: true"
require_file_contains "docs/agent-evals/datasets/memory-adversarial-review-required.yml" "prompt_must_contain"
require_file_contains "docs/agent-evals/datasets/memory-adversarial-review-required.yml" "artemis-symphony/prompts/adversarial-review.md"
require_file_contains "docs/agent-evals/datasets/memory-adversarial-review-required.yml" "artemis-symphony/skills/adversarial-review.md"
require_file_contains "docs/agent-evals/datasets/memory-adversarial-review-required.yml" "artemis-symphony/tools/registry.json"
require_file_contains "docs/agent-evals/datasets/memory-adversarial-review-independent.yml" "adversarial_review_enabled: true"
require_file_contains "docs/agent-evals/datasets/memory-adversarial-review-independent.yml" "expected_summary_dispatch_kinds"
require_file_contains "docs/agent-evals/datasets/memory-adversarial-review-independent.yml" "adversarial_review"
require_file_contains "docs/agent-evals/datasets/memory-adversarial-review-independent.yml" "expected_agent_run_count: 2"
require_file_contains "docs/agent-evals/datasets/memory-docs-governance.yml" "tracker: memory"
require_file_contains "docs/agent-evals/datasets/memory-permission-preflight.yml" "expected_codex_started: false"
require_file_contains "docs/agent-evals/datasets/memory-permission-preflight.yml" "expected_failure_category: permission"
require_file_contains "docs/agent-evals/datasets/memory-permission-preflight.yml" "permissions.network_access_reason"
require_file_contains "docs/agent-evals/datasets/memory-network-access-allowed.yml" "expected_permission_network_access: true"
require_file_contains "docs/agent-evals/datasets/memory-network-access-allowed.yml" "expected_permission_network_access_reason"
require_file_contains "docs/agent-evals/datasets/memory-network-access-allowed.yml" "permissions_network_access_reason"
require_file_contains "docs/agent-evals/datasets/memory-danger-full-access-allowed.yml" "codex_thread_sandbox: danger-full-access"
require_file_contains "docs/agent-evals/datasets/memory-danger-full-access-allowed.yml" "permissions_allow_danger_full_access: true"
require_file_contains "docs/agent-evals/datasets/memory-danger-full-access-allowed.yml" "expected_permission_danger_full_access_allowed: true"
require_file_contains "docs/agent-evals/datasets/memory-writable-root-preflight.yml" "outside-writable-root"
require_file_contains "docs/agent-evals/datasets/memory-writable-root-preflight.yml" "allowed_writable_roots"
require_file_contains "docs/agent-evals/datasets/memory-writable-root-allowed.yml" "permissions_allowed_writable_root"
require_file_contains "docs/agent-evals/datasets/memory-writable-root-allowed.yml" "expected_permission_allowed_writable_root_contains"
require_file_contains "docs/agent-evals/datasets/memory-writable-root-allowed.yml" "outside-writable-root"
require_file_contains "docs/agent-evals/datasets/memory-writable-root-allowed.yml" "configured-writable-root"
require_file_contains "docs/agent-evals/datasets/memory-after-create-hook-failure.yml" "hook_after_create_outcome: failed"
require_file_contains "docs/agent-evals/datasets/memory-after-create-hook-failure.yml" "after_create_hook_failed"
require_file_contains "docs/agent-evals/datasets/memory-before-run-hook-failure.yml" "hook_before_run_outcome: failed"
require_file_contains "docs/agent-evals/datasets/memory-before-run-hook-failure.yml" "before_run hook failed"
require_file_contains "docs/agent-evals/datasets/memory-codex-turn-failure.yml" "codex_turn_outcome: failed"
require_file_contains "docs/agent-evals/datasets/memory-codex-turn-failure.yml" "thread/tokenUsage/updated"
require_file_contains "docs/agent-evals/datasets/memory-codex-turn-response-timeout.yml" "codex_turn_outcome: response_timeout"
require_file_contains "docs/agent-evals/datasets/memory-codex-turn-response-timeout.yml" "codex turn response timeout"
require_file_contains "docs/agent-evals/datasets/memory-codex-turn-timeout.yml" "codex_turn_outcome: timeout"
require_file_contains "docs/agent-evals/datasets/memory-codex-turn-timeout.yml" "codex turn timeout"
require_file_contains "docs/agent-evals/datasets/memory-codex-turn-cancelled.yml" "codex_turn_outcome: cancelled"
require_file_contains "docs/agent-evals/datasets/memory-codex-turn-cancelled.yml" "turn_cancelled"
require_file_contains "docs/agent-evals/datasets/memory-codex-approval-required.yml" "codex_turn_outcome: approval_required"
require_file_contains "docs/agent-evals/datasets/memory-codex-approval-required.yml" "approval_required"
require_file_contains "docs/agent-evals/datasets/memory-codex-dynamic-tool-failure.yml" "codex_turn_outcome: dynamic_tool_failure"
require_file_contains "docs/agent-evals/datasets/memory-codex-dynamic-tool-failure.yml" "tool_call_failed"
require_file_contains "docs/agent-evals/datasets/memory-codex-user-input-required.yml" "codex_turn_outcome: user_input_required"
require_file_contains "docs/agent-evals/datasets/memory-codex-user-input-required.yml" "turn_input_required"
require_file_contains "docs/agent-evals/datasets/memory-codex-malformed-event.yml" "codex_turn_outcome: completed_with_malformed"
require_file_contains "docs/agent-evals/datasets/memory-codex-malformed-event.yml" "malformed"
require_file_contains "docs/agent-evals/datasets/memory-codex-startup-failure.yml" "codex_startup_outcome: initialize_error"
require_file_contains "docs/agent-evals/datasets/memory-codex-startup-failure.yml" "startup_failed"
require_file_contains "docs/agent-evals/datasets/memory-linear-comment-writeback.yml" "linear_comment_reporting_enabled: true"
require_file_contains "docs/agent-evals/datasets/memory-linear-comment-writeback.yml" "linear_comment:succeeded"
require_file_contains "docs/agent-evals/datasets/memory-linear-comment-failure-audit.yml" "linear_comment_reporting_enabled: true"
require_file_contains "docs/agent-evals/datasets/memory-linear-comment-failure-audit.yml" "linear_comment:failed"
require_file_contains "docs/agent-evals/datasets/memory-linear-comment-failure-audit.yml" "linear_comment:failed:memory_comment_body"
require_file_contains "docs/reports/agent-runs/README.md" "禁止提交"
require_file_contains "docs/reports/agent-runs/README.md" "artifacts/agent-evals/<run>/agent-runs"
require_file_contains "docs/reports/agent-runs/README.md" "summary_type=symphony_agent_run"
require_file_contains "docs/reports/agent-runs/README.md" "codex.event_counts"
require_file_contains "docs/reports/agent-runs/README.md" "workspace.path"
require_file_contains "docs/reports/agent-runs/README.md" "permissions.writable_roots"
require_file_contains "docs/reports/agent-runs/README.md" "低敏相对引用"
require_file_contains "docs/reports/agent-runs/README.md" "generate-agent-eval-drafts.sh"
require_file_contains "docs/reports/agent-runs/README.md" "manual_review_required: true"
require_file_contains "docs/reports/agent-runs/2026-06-09-harness-market-gap-improvements.md" "外部副作用"
require_file_contains "docs/reports/agent-runs/2026-06-09-harness-market-gap-improvements.md" "未覆盖风险"
require_file_contains "docs/reports/harness-metrics/README.md" "scripts/harness/generate-harness-metrics-report.sh"
require_file_contains "docs/reports/harness-metrics/README.md" "scripts/harness/generate-ci-harness-metrics.sh"
require_file_contains "docs/reports/harness-metrics/README.md" "scripts/harness/check-harness-metrics-report.sh"
require_file_contains "docs/reports/harness-metrics/README.md" "scripts/e2e/run-symphony-agent-eval.sh all"
require_file_contains "docs/reports/harness-metrics/README.md" "GitHub Actions artifact"
require_file_contains "docs/reports/harness-metrics/README.md" "artifacts/harness-delivery-signals"
require_file_contains "docs/reports/harness-metrics/README.md" "harness_delivery_signal"
require_file_contains "docs/reports/harness-metrics/README.md" "collect-github-delivery-signal.sh"
require_file_contains "docs/reports/harness-metrics/README.md" "review_findings"
require_file_contains "docs/reports/harness-metrics/README.md" "eval_suites"
require_file_contains "docs/reports/harness-metrics/README.md" "稳定失败类别"
require_file_contains "docs/reports/harness-metrics/README.md" "external_effects.events"
require_file_contains "docs/reports/harness-metrics/README.md" "codex.event_counts"
require_file_contains "docs/reports/harness-metrics/README.md" "agent_runs.codex_events"
require_file_contains "docs/reports/harness-metrics/README.md" "Codex 事件统计"
require_file_contains "docs/reports/harness-metrics/README.md" "Agent eval 草稿"
require_file_contains "docs/reports/harness-metrics/README.md" "artifacts/agent-eval-drafts"
require_file_contains "docs/reports/harness-metrics/README.md" '稳定 `error_code`'
require_file_contains "docs/reports/harness-metrics/README.md" "运行环境"
require_file_contains "docs/reports/harness-metrics/README.md" "权限姿态"
require_file_contains "docs/reports/harness-metrics/README.md" "agent_runs.permission_posture"
require_file_contains "docs/reports/harness-metrics/README.md" "deploy_drill_report"
require_file_contains "docs/reports/deploy-drills/README.md" "summary_type"
require_file_contains "docs/reports/deploy-drills/README.md" "deploy_drill_report"
require_file_contains "docs/governance/DOC_FRESHNESS_POLICY.md" "## 证据 freshness"
require_file_contains "docs/governance/DOC_FRESHNESS_POLICY.md" "低敏"
require_file_contains "scripts/harness/check-doc-freshness.sh" "Checking doc evidence freshness"
require_file_contains "scripts/harness/check-doc-freshness.sh" "Doc evidence freshness check passed"
require_file_contains "artemis-symphony/WORKFLOW.md.example" "artemis-symphony/skills/adversarial-review.md"
require_file_contains "artemis-symphony/WORKFLOW.md.example" "docs/runbooks/AGENT_PERMISSION_RUNBOOK.md"
require_file_contains "artemis-symphony/prompts/README.md" "agent-requirement-intake.md"
require_file_contains "artemis-symphony/prompts/README.md" "adversarial-review.md"
require_file_contains "artemis-symphony/skills/README.md" "adversarial-review.md"
require_file_contains "artemis-symphony/README.md" "artemis-symphony/tools/registry.json"
require_file_contains "artemis-symphony/WORKFLOW.md.example" "artemis-symphony/tools/registry.json"
require_file_contains "artemis-symphony/tools/README.md" "registry.json"
require_file_contains "artemis-symphony/tools/README.md" "success:boolean"
require_file_contains "artemis-symphony/tools/README.md" "tool_call_completed"
require_file_contains "artemis-symphony/tools/README.md" "稳定错误码"
require_file_contains "artemis-symphony/tools/registry.json" "symphony_tool_registry"
require_file_contains "artemis-symphony/tools/registry.json" "linear_graphql"
require_file_contains "artemis-symphony/tools/registry.json" "external_write_allowed"
require_file_contains "scripts/harness/check-symphony-assets.sh" "allowed_statuses"
require_file_contains "scripts/harness/check-symphony-assets.sh" "required_output_keys"
require_file_contains "scripts/harness/check-symphony-assets.sh" "required_audit_events"
require_file_contains "scripts/harness/check-symphony-assets.sh" "stable error code"
require_file_contains "docs/asset-manifest.yml" "external-agent-pointer"
require_file_contains "docs/asset-manifest.yml" "memory-real-tenant-package-sync.yml"
require_file_contains "docs/asset-manifest.yml" "memory-adversarial-review-independent.yml"
require_file_contains "docs/asset-manifest.yml" "symphony-tool-registry"
require_file_contains "docs/asset-manifest.yml" "check-openspec-change-state.sh"
require_file_contains "docs/asset-manifest.yml" "check-agent-run-summaries.sh"
require_file_contains "docs/asset-manifest.yml" "generate-agent-eval-drafts.sh"
require_file_contains "docs/asset-manifest.yml" "run-symphony-agent-eval.sh"
require_file_contains "docs/asset-manifest.yml" "generate-harness-metrics-report.sh"
require_file_contains "docs/asset-manifest.yml" "generate-ci-harness-metrics.sh"
require_file_contains "docs/asset-manifest.yml" "collect-github-delivery-signal.sh"
require_file_contains "docs/asset-manifest.yml" "deploy-drill-report-policy"
require_file_contains "docs/asset-manifest.yml" "check-deploy-drill-reports.sh"
require_file_contains "docs/asset-manifest.yml" "check-doc-freshness.sh"
require_file_contains "scripts/e2e/run-symphony-agent-eval.sh" "generate-harness-metrics-report.sh"
require_file_contains "scripts/e2e/run-symphony-agent-eval.sh" "harness_metrics"
require_file_contains "scripts/e2e/run-symphony-agent-eval.sh" "failure_category_counts"
require_file_contains "scripts/e2e/run-symphony-agent-eval.sh" "summary_type\": \"symphony_memory_eval_suite"
require_file_contains "scripts/e2e/run-symphony-agent-eval.sh" "SYMPHONY_AGENT_EVAL_JAR"
require_file_contains "scripts/e2e/run-symphony-agent-eval.sh" "expected_external_effect_details"
require_file_contains "scripts/e2e/run-symphony-agent-eval.sh" "expected_codex_events"
require_file_contains "scripts/e2e/run-symphony-agent-eval.sh" "codex_event_counts"
require_file_contains "scripts/e2e/run-symphony-agent-eval.sh" "linear_comment_reporting_enabled"
require_file_contains "scripts/e2e/run-symphony-agent-eval.sh" "adversarial_review_enabled"
require_file_contains "scripts/e2e/run-symphony-agent-eval.sh" "expected_summary_workspace_files"
require_file_contains "scripts/e2e/run-symphony-agent-eval.sh" "expected_summary_dispatch_kinds"
require_file_contains "scripts/e2e/run-symphony-agent-eval.sh" "artemis-symphony/tools/registry.json"
require_file_contains "scripts/e2e/run-symphony-agent-eval.sh" "Checking eval agent run summaries"
require_file_contains "scripts/e2e/run-symphony-agent-eval.sh" "Checking eval suite agent run summaries"
require_file_contains "scripts/e2e/run-symphony-agent-eval.sh" "check_harness_metrics_permission_posture"
require_file_contains "scripts/e2e/run-symphony-agent-eval.sh" "Checking eval harness metrics permission posture"
require_file_contains "scripts/e2e/run-symphony-agent-eval.sh" "Checking eval suite harness metrics permission posture"
require_file_contains "scripts/harness/run-agent-evals.sh" "expected_external_effect_details"
require_file_contains "scripts/harness/run-agent-evals.sh" "type:status:error_code"
require_file_contains "scripts/harness/run-agent-evals.sh" "datasets that start Codex"
require_file_contains "scripts/harness/run-agent-evals.sh" "adversarial_review_enabled"
require_file_contains "scripts/harness/run-agent-evals.sh" "expected_summary_dispatch_kinds"
require_file_contains "scripts/harness/run-agent-evals.sh" "dataset_draft_only_keys"
require_file_contains "scripts/harness/run-agent-evals.sh" "draft-only key"
require_file_contains "scripts/harness/run-agent-evals.sh" "dataset id must match file name"
require_file_contains "scripts/harness/run-agent-evals.sh" "duplicate dataset id"
require_file_contains "scripts/harness/check-agentic-harness-assets.sh" "Checking agent eval draft promotion guard"
require_file_contains "scripts/harness/check-agentic-harness-assets.sh" "dataset id must match file name"
require_file_contains "scripts/harness/generate-harness-metrics-report.sh" "eval_suites"
require_file_contains "scripts/harness/generate-harness-metrics-report.sh" "**/agent-runs/*.json"
require_file_contains "scripts/harness/generate-harness-metrics-report.sh" "codex_events"
require_file_contains "scripts/harness/generate-harness-metrics-report.sh" "Codex event counts"
require_file_contains "scripts/harness/generate-harness-metrics-report.sh" "external_effect_error_code_counts"
require_file_contains "scripts/harness/generate-harness-metrics-report.sh" "error_code_counts"
require_file_contains "scripts/harness/generate-harness-metrics-report.sh" "environment_java_major_counts"
require_file_contains "scripts/harness/generate-harness-metrics-report.sh" "permission_posture"
require_file_contains "scripts/harness/generate-harness-metrics-report.sh" "permission_turn_sandbox_type_counts"
require_file_contains "scripts/harness/generate-harness-metrics-report.sh" "collect_deploy_drill_reports"
require_file_contains "scripts/harness/generate-harness-metrics-report.sh" "deploy_drill_status_counts"
require_file_contains "scripts/harness/generate-harness-metrics-report.sh" "agent_eval_drafts"
require_file_contains "scripts/harness/generate-harness-metrics-report.sh" "draft_failure_category_counts"
require_file_contains "scripts/harness/generate-harness-metrics-report.sh" "delivery_signals_dir"
require_file_contains "scripts/harness/generate-harness-metrics-report.sh" "harness_delivery_signal"
require_file_contains "scripts/harness/generate-harness-metrics-report.sh" "review_finding_severity_counts"
require_file_contains "scripts/harness/generate-ci-harness-metrics.sh" "HARNESS_METRICS_OUTPUT_DIR"
require_file_contains "scripts/harness/generate-ci-harness-metrics.sh" "GITHUB_STEP_SUMMARY"
require_file_contains "scripts/harness/generate-ci-harness-metrics.sh" "HARNESS_METRICS_DELIVERY_SIGNALS_DIR"
require_file_contains "scripts/harness/generate-ci-harness-metrics.sh" "HARNESS_METRICS_DEPLOY_DRILLS_DIR"
require_file_contains "scripts/harness/generate-ci-harness-metrics.sh" "HARNESS_METRICS_AGENT_EVAL_DRAFTS_DIR"
require_file_contains "scripts/harness/collect-github-delivery-signal.sh" "GITHUB_EVENT_PATH"
require_file_contains "scripts/harness/collect-github-delivery-signal.sh" "harness_delivery_signal"
require_file_contains "scripts/harness/collect-github-delivery-signal.sh" "review_findings"
require_file_contains "scripts/harness/check-harness-metrics-report.sh" "fixture-suite-case"
require_file_contains "scripts/harness/check-harness-metrics-report.sh" "memory_comment_body"
require_file_contains "scripts/harness/check-harness-metrics-report.sh" "CI metrics wrapper"
require_file_contains "scripts/harness/check-harness-metrics-report.sh" "collect-github-delivery-signal.sh"
require_file_contains "scripts/harness/check-harness-metrics-report.sh" "harness_delivery_signal"
require_file_contains "scripts/harness/check-harness-metrics-report.sh" "review_findings"
require_file_contains "scripts/harness/check-harness-metrics-report.sh" "external effect error code"
require_file_contains "scripts/harness/check-harness-metrics-report.sh" "Codex event counts"
require_file_contains "scripts/harness/check-harness-metrics-report.sh" "Java major"
require_file_contains "scripts/harness/check-harness-metrics-report.sh" "network_access_runs"
require_file_contains "scripts/harness/check-harness-metrics-report.sh" "turn sandbox type"
require_file_contains "scripts/harness/check-harness-metrics-report.sh" "drill smoke"
require_file_contains "scripts/harness/check-harness-metrics-report.sh" "draft failure category"
require_file_contains "scripts/harness/check-deploy-drill-reports.sh" "deploy_drill_report"
require_file_contains "scripts/harness/check-agent-run-summaries.sh" "possible sensitive run summary content"
require_file_contains "scripts/harness/check-agent-run-summaries.sh" "scan_path"
require_file_contains "scripts/harness/check-agent-run-summaries.sh" "schema-lite"
require_file_contains "scripts/harness/check-agent-run-summaries.sh" "required_nested_keys"
require_file_contains "scripts/harness/check-agent-run-summaries.sh" "parent_run_id"
require_file_contains "scripts/harness/check-agent-run-summaries.sh" "codex.event_counts"
require_file_contains "scripts/harness/check-agent-run-summaries.sh" "workspace.path must be a low-sensitive relative reference"
require_file_contains "scripts/harness/check-agent-run-summaries.sh" "permissions.writable_roots[0] must be a low-sensitive relative reference"
require_file_contains "scripts/harness/check-agent-run-summaries.sh" "--self-test"
require_file_contains "scripts/harness/generate-agent-eval-drafts.sh" "--self-test"
require_file_contains "scripts/harness/generate-agent-eval-drafts.sh" "manual_review_required: true"
require_file_contains "scripts/harness/generate-agent-eval-drafts.sh" "summary_type\") != \"symphony_agent_run\""
require_file_contains "scripts/harness/check-agentic-harness-assets.sh" "generate-agent-eval-drafts.sh --self-test"
require_file_contains ".gitignore" "artifacts/agent-eval-drafts/"
require_file_contains ".github/copilot-instructions.md" "AGENTS.md"
require_file_contains "CLAUDE.md" "AGENTS.md"
require_file_contains "GEMINI.md" "AGENTS.md"
require_file_contains ".github/workflows/governance.yml" "scripts/harness/run-governance-checks.sh"
require_file_contains ".github/workflows/governance.yml" "scripts/harness/generate-ci-harness-metrics.sh"
require_file_contains ".github/workflows/governance.yml" "scripts/harness/collect-github-delivery-signal.sh"
require_file_contains ".github/workflows/governance.yml" "harness-metrics-governance"
require_file_contains ".github/workflows/verify.yml" "scripts/harness/full-verify.sh"
require_file_contains ".github/workflows/verify.yml" "scripts/harness/generate-ci-harness-metrics.sh"
require_file_contains ".github/workflows/verify.yml" "scripts/harness/collect-github-delivery-signal.sh"
require_file_contains ".github/workflows/verify.yml" "harness-metrics-verify"

if contains_fixed_string "Governance - Markdown Links" ".github/workflows/verify.yml"; then
  echo "Verify workflow still contains duplicated per-check governance steps." >&2
  exit 1
fi

print_step "Checking agent eval draft generator"
bash scripts/harness/generate-agent-eval-drafts.sh --self-test

print_step "Checking agent eval draft promotion guard"
tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT
mkdir -p "$tmp_dir/fixtures" "$tmp_dir/datasets"
cat >"$tmp_dir/fixtures/fixture.yml" <<'YAML'
id: dataset-guard-fixture
title: Dataset guard fixture
risk_level: low
issue: |
  Validate dataset guard fixture.
expected_assets:
  - docs/agent-evals/README.md
expected_validations:
  - scripts/harness/run-agent-evals.sh
review_focus:
  - dataset guard
YAML

run_rejected_dataset_fixture() {
  local dataset_name="$1"
  local expected_diagnostic="$2"
  local output_path="$tmp_dir/${dataset_name}.out"
  shift 2

  rm -f "$tmp_dir/datasets/"*.yml
  cat >"$tmp_dir/datasets/${dataset_name}.yml"
  if AGENT_EVAL_FIXTURES_DIR="$tmp_dir/fixtures" AGENT_EVAL_DATASETS_DIR="$tmp_dir/datasets" \
    scripts/harness/run-agent-evals.sh >"$output_path" 2>&1; then
    cat "$output_path" >&2
    echo "Agent eval dataset guard did not reject invalid fixture: ${dataset_name}" >&2
    exit 1
  fi
  if ! contains_fixed_string "$expected_diagnostic" "$output_path"; then
    cat "$output_path" >&2
    echo "Agent eval dataset guard failed without expected diagnostic: ${expected_diagnostic}" >&2
    exit 1
  fi
}

run_rejected_dataset_fixture "draft" "draft-only key manual_review_required" <<'YAML'
# Generated from a low-sensitive symphony_agent_run summary.
id: memory-regression-fixture
title: Memory regression fixture
risk_level: medium
tracker: memory
issue_id: eval-memory-regression-fixture
issue_identifier: EVAL-DRAFT
issue_title: Generated failure regression
issue_state: Todo
issue_description: |
  Generated regression seed from low-sensitive Symphony run summary run-fixture.
  Replace this synthetic scenario with the smallest stable reproduction before committing.
manual_review_required: true
source_summary: artifacts/agent-runs/run-fixture.json
source_run_id: run-fixture
expected_workspace_file: EVAL_RESULT.md
expected_workspace_contains: should not be written
expected_workspace_present: false
expected_codex_started: false
expected_summary_status: failed
expected_history_status: failed
expected_failure_reason_contains: permission
expected_failure_category: permission
expected_events:
  - run_started
  - run_failed
forbidden_events:
  - session_started
required_validations:
  - scripts/harness/run-agent-evals.sh
YAML

run_rejected_dataset_fixture "wrong-name" "dataset id must match file name (wrong-name)" <<'YAML'
id: memory-regression-fixture
title: Memory regression fixture
risk_level: medium
tracker: memory
issue_id: eval-memory-regression-fixture
issue_identifier: EVAL-DATASET
issue_title: Dataset id mismatch fixture
issue_state: Todo
issue_description: |
  Validate dataset id and filename alignment.
expected_workspace_file: EVAL_RESULT.md
expected_workspace_contains: should not be written
expected_workspace_present: false
expected_codex_started: false
expected_summary_status: failed
expected_history_status: failed
expected_failure_reason_contains: permission
expected_failure_category: permission
expected_events:
  - run_started
  - run_failed
forbidden_events:
  - session_started
required_validations:
  - scripts/harness/run-agent-evals.sh
YAML

print_step "Running agent workflow eval fixtures"
scripts/harness/run-agent-evals.sh

print_step "Agentic harness asset check passed"
