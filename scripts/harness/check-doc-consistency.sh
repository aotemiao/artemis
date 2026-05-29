#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

contains_fixed_string() {
  local pattern="$1"
  local file="$2"
  if command -v rg >/dev/null 2>&1 && rg --version >/dev/null 2>&1; then
    rg -q --fixed-strings "$pattern" "$file"
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

require_file_not_contains() {
  local file="$1"
  local pattern="$2"
  require_repo_path_exact "$file"
  if contains_fixed_string "$pattern" "$file"; then
    echo "Outdated content found in $file: $pattern" >&2
    exit 1
  fi
}

print_step "Checking docs index and core entry docs"
require_file_contains "docs/README.md" "SERVICE_SMOKE_RUNBOOK.md"
require_file_contains "docs/README.md" "ADD_DOMAIN_SERVICE_RUNBOOK.md"
require_file_contains "docs/README.md" "ADD_DUBBO_CLIENT_RUNBOOK.md"
require_file_contains "docs/README.md" "ADD_ARCHUNIT_RULE_RUNBOOK.md"
require_file_contains "docs/README.md" "AGENT_REVIEW_LOOP.md"
require_file_contains "docs/README.md" "AGENT_DEVELOPMENT_WORKFLOW.md"
require_file_contains "docs/README.md" "DOC_FRESHNESS_POLICY.md"
require_file_contains "docs/README.md" "DEPLOY_AND_ROLLBACK_RUNBOOK.md"
require_file_contains "docs/README.md" "PROJECT_PROGRESS_REPORT.md"
require_file_contains "docs/README.md" "SYMPHONY_TROUBLESHOOTING.md"
require_file_contains "docs/README.md" "QUALITY_ISSUE_STANDARD.md"
require_file_contains "docs/README.md" "quality-issues/"
require_file_contains "docs/README.md" "deploy-drills/"

print_step "Checking README consistency"
require_file_contains "README.md" "如何最快了解项目"
require_file_contains "README.md" "分层收敛方向"
require_file_contains "README.md" "文档与目录职责"
require_file_contains "README.md" "事实源"
require_file_contains "README.md" "执行入口"
require_file_contains "README.md" "验证守门"
require_file_contains "README.md" "任务过程"
require_file_contains "README.md" "编排资产"
require_file_contains "README.md" "运行与交付资产"
require_file_contains "README.md" "openspec/specs/"
require_file_contains "README.md" "docs/runbooks/"
require_file_contains "README.md" "docs/governance/"
require_file_contains "README.md" "docs/reports/"
require_file_contains "README.md" "docs/agent-workflow/"
require_file_contains "README.md" "scripts/dev/"
require_file_contains "README.md" "scripts/harness/"
require_file_contains "README.md" "scripts/smoke/"
require_file_contains "README.md" "artemis-symphony/skills/"
require_file_contains "README.md" "artemis-symphony/prompts/"
require_file_contains "README.md" "docs/exec-plans/active/"
require_file_contains "README.md" "PROJECT_PROGRESS_REPORT.md"
require_file_contains "README.md" "QUALITY_SCORE.md"
require_file_contains "README.md" "AGENT_DEVELOPMENT_WORKFLOW.md"
require_file_contains "README.md" "DEPLOY_AND_ROLLBACK_RUNBOOK.md"
require_file_contains "README.md" ".github/workflows/governance.yml"
require_file_contains "README.md" ".github/workflows/verify.yml"

print_step "Checking AGENTS consistency"
require_file_contains "AGENTS.md" "scripts/dev/check-service-config.sh"
require_file_contains "AGENTS.md" "scripts/dev/check-service-readiness.sh"
require_file_contains "AGENTS.md" "scripts/dev/new-domain-service.sh"
require_file_contains "AGENTS.md" "scripts/dev/package-service.sh"
require_file_contains "AGENTS.md" "scripts/dev/build-image.sh"
require_file_contains "AGENTS.md" "scripts/dev/service-status.sh"
require_file_contains "AGENTS.md" "scripts/dev/deploy-drill.sh"
require_file_contains "AGENTS.md" "scripts/dev/rollback-drill.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-api-doc-sync.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-client-contracts.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-capability-package-structure.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-domain-service-scaffold.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-service-catalog.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-symphony-assets.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-critical-path-tests.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-duplicate-patterns.sh"
require_file_contains "AGENTS.md" "scripts/harness/run-governance-checks.sh"
require_file_contains "AGENTS.md" "scripts/smoke/all-services.sh"
require_file_contains "AGENTS.md" "docs/runbooks/ADD_DOMAIN_SERVICE_RUNBOOK.md"
require_file_contains "AGENTS.md" "docs/runbooks/ADD_DUBBO_CLIENT_RUNBOOK.md"
require_file_contains "AGENTS.md" "docs/runbooks/ADD_ARCHUNIT_RULE_RUNBOOK.md"
require_file_contains "AGENTS.md" "docs/agent-workflow/AGENT_REVIEW_LOOP.md"
require_file_contains "AGENTS.md" "docs/reports/PROJECT_PROGRESS_REPORT.md"
require_file_contains "AGENTS.md" "docs/governance/QUALITY_ISSUE_STANDARD.md"
require_file_contains "AGENTS.md" "docs/runbooks/DEPLOY_AND_ROLLBACK_RUNBOOK.md"
require_file_contains "AGENTS.md" "docs/runbooks/SYMPHONY_TROUBLESHOOTING.md"

print_step "Checking ARCHITECTURE drift"
require_file_contains "ARCHITECTURE.md" "scripts/dev/package-service.sh"
require_file_contains "ARCHITECTURE.md" "scripts/dev/build-image.sh"
require_file_contains "ARCHITECTURE.md" "scripts/dev/check-service-config.sh"
require_file_contains "ARCHITECTURE.md" "scripts/dev/check-service-readiness.sh"
require_file_contains "ARCHITECTURE.md" "scripts/dev/new-domain-service.sh"
require_file_contains "ARCHITECTURE.md" "scripts/dev/service-status.sh"
require_file_contains "ARCHITECTURE.md" "scripts/dev/deploy-drill.sh"
require_file_contains "ARCHITECTURE.md" "scripts/dev/rollback-drill.sh"
require_file_contains "ARCHITECTURE.md" "scripts/harness/run-governance-checks.sh"
require_file_contains "ARCHITECTURE.md" "scripts/harness/check-service-catalog.sh"
require_file_contains "ARCHITECTURE.md" "scripts/harness/check-symphony-assets.sh"
require_file_contains "ARCHITECTURE.md" "PROJECT_PROGRESS_REPORT.md"
require_file_not_contains 'ARCHITECTURE.md' 'mvn verify` 级别的统一守门尚未完全固化到 Maven lifecycle'

print_step "Checking asset directory docs consistency"
require_file_contains "docs/runbooks/SERVICE_SMOKE_RUNBOOK.md" "scripts/dev/check-service-readiness.sh"
require_file_contains "docs/runbooks/SERVICE_SMOKE_RUNBOOK.md" "scripts/smoke/symphony-state.sh"
require_file_contains "docs/runbooks/SERVICE_SMOKE_RUNBOOK.md" "scripts/smoke/all-services.sh"
require_file_contains "docs/governance/DOC_FRESHNESS_POLICY.md" "Last Reviewed"
require_file_contains "docs/governance/DOC_FRESHNESS_POLICY.md" "PROJECT_PROGRESS_REPORT.md"
require_file_contains "docs/runbooks/DEPLOY_AND_ROLLBACK_RUNBOOK.md" "scripts/dev/build-image.sh"
require_file_contains "docs/runbooks/DEPLOY_AND_ROLLBACK_RUNBOOK.md" "scripts/dev/deploy-drill.sh"
require_file_contains "docs/runbooks/DEPLOY_AND_ROLLBACK_RUNBOOK.md" "scripts/dev/rollback-drill.sh"
require_file_contains "docs/reports/PROJECT_PROGRESS_REPORT.md" "## 汇总结论"
require_file_contains "docs/reports/PROJECT_PROGRESS_REPORT.md" "## 下一阶段演进路线"
require_file_contains "docs/runbooks/SYMPHONY_TROUBLESHOOTING.md" "scripts/smoke/symphony-state.sh"
require_file_contains "docs/governance/QUALITY_ISSUE_STANDARD.md" "quality-issues/archive/"

print_step "Docs consistency check passed"
