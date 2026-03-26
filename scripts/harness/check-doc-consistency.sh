#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

contains_fixed_string() {
  local pattern="$1"
  local file="$2"
  if command -v rg >/dev/null 2>&1; then
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
require_file_contains "docs/README.md" "DOC_FRESHNESS_POLICY.md"
require_file_contains "docs/README.md" "DEPLOY_AND_ROLLBACK_RUNBOOK.md"
require_file_contains "docs/README.md" "PROJECT_PROGRESS_REPORT.md"
require_file_contains "docs/README.md" "SYMPHONY_TROUBLESHOOTING.md"
require_file_contains "docs/README.md" "QUALITY_ISSUE_STANDARD.md"
require_file_contains "docs/README.md" "quality-issues/"
require_file_contains "docs/README.md" "deploy-drills/"

print_step "Checking README consistency"
require_file_contains "README.md" "scripts/dev/check-service-config.sh"
require_file_contains "README.md" "scripts/dev/check-service-readiness.sh"
require_file_contains "README.md" "scripts/dev/new-domain-service.sh"
require_file_contains "README.md" "scripts/dev/package-service.sh"
require_file_contains "README.md" "scripts/dev/build-image.sh"
require_file_contains "README.md" "scripts/dev/service-status.sh"
require_file_contains "README.md" "scripts/dev/deploy-drill.sh"
require_file_contains "README.md" "scripts/dev/rollback-drill.sh"
require_file_contains "README.md" "scripts/harness/run-governance-checks.sh"
require_file_contains "README.md" "scripts/harness/check-service-catalog.sh"
require_file_contains "README.md" "scripts/harness/check-symphony-assets.sh"
require_file_contains "README.md" "scripts/smoke/all-services.sh"
require_file_contains "README.md" "ADD_DOMAIN_SERVICE_RUNBOOK.md"
require_file_contains "README.md" "ADD_DUBBO_CLIENT_RUNBOOK.md"
require_file_contains "README.md" "ADD_ARCHUNIT_RULE_RUNBOOK.md"
require_file_contains "README.md" "AGENT_REVIEW_LOOP.md"
require_file_contains "README.md" "PROJECT_PROGRESS_REPORT.md"
require_file_contains "README.md" "QUALITY_ISSUE_STANDARD.md"
require_file_contains "README.md" "DEPLOY_AND_ROLLBACK_RUNBOOK.md"
require_file_contains "README.md" "SYMPHONY_TROUBLESHOOTING.md"
require_file_contains "README.md" ".github/workflows/governance.yml"

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
require_file_contains "AGENTS.md" "scripts/harness/check-domain-service-scaffold.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-service-catalog.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-symphony-assets.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-critical-path-tests.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-duplicate-patterns.sh"
require_file_contains "AGENTS.md" "scripts/harness/run-governance-checks.sh"
require_file_contains "AGENTS.md" "scripts/smoke/all-services.sh"
require_file_contains "AGENTS.md" "docs/harness-engineering/ADD_DOMAIN_SERVICE_RUNBOOK.md"
require_file_contains "AGENTS.md" "docs/harness-engineering/ADD_DUBBO_CLIENT_RUNBOOK.md"
require_file_contains "AGENTS.md" "docs/harness-engineering/ADD_ARCHUNIT_RULE_RUNBOOK.md"
require_file_contains "AGENTS.md" "docs/harness-engineering/AGENT_REVIEW_LOOP.md"
require_file_contains "AGENTS.md" "docs/harness-engineering/PROJECT_PROGRESS_REPORT.md"
require_file_contains "AGENTS.md" "docs/harness-engineering/QUALITY_ISSUE_STANDARD.md"
require_file_contains "AGENTS.md" "docs/harness-engineering/DEPLOY_AND_ROLLBACK_RUNBOOK.md"
require_file_contains "AGENTS.md" "docs/harness-engineering/SYMPHONY_TROUBLESHOOTING.md"

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

print_step "Checking harness docs consistency"
require_file_contains "docs/harness-engineering/README.md" "SERVICE_SMOKE_RUNBOOK.md"
require_file_contains "docs/harness-engineering/README.md" "ADD_DOMAIN_SERVICE_RUNBOOK.md"
require_file_contains "docs/harness-engineering/README.md" "ADD_DUBBO_CLIENT_RUNBOOK.md"
require_file_contains "docs/harness-engineering/README.md" "ADD_ARCHUNIT_RULE_RUNBOOK.md"
require_file_contains "docs/harness-engineering/README.md" "AGENT_REVIEW_LOOP.md"
require_file_contains "docs/harness-engineering/README.md" "DOC_FRESHNESS_POLICY.md"
require_file_contains "docs/harness-engineering/README.md" "DEPLOY_AND_ROLLBACK_RUNBOOK.md"
require_file_contains "docs/harness-engineering/README.md" "PROJECT_PROGRESS_REPORT.md"
require_file_contains "docs/harness-engineering/README.md" "deploy-drills/"
require_file_contains "docs/harness-engineering/README.md" "SYMPHONY_TROUBLESHOOTING.md"
require_file_contains "docs/harness-engineering/README.md" "QUALITY_ISSUE_STANDARD.md"
require_file_contains "docs/harness-engineering/README.md" "quality-issues/"
require_file_contains "docs/harness-engineering/SERVICE_SMOKE_RUNBOOK.md" "scripts/dev/check-service-readiness.sh"
require_file_contains "docs/harness-engineering/SERVICE_SMOKE_RUNBOOK.md" "scripts/smoke/symphony-state.sh"
require_file_contains "docs/harness-engineering/SERVICE_SMOKE_RUNBOOK.md" "scripts/smoke/all-services.sh"
require_file_contains "docs/harness-engineering/DOC_FRESHNESS_POLICY.md" "Last Reviewed"
require_file_contains "docs/harness-engineering/DOC_FRESHNESS_POLICY.md" "PROJECT_PROGRESS_REPORT.md"
require_file_contains "docs/harness-engineering/DEPLOY_AND_ROLLBACK_RUNBOOK.md" "scripts/dev/build-image.sh"
require_file_contains "docs/harness-engineering/DEPLOY_AND_ROLLBACK_RUNBOOK.md" "scripts/dev/deploy-drill.sh"
require_file_contains "docs/harness-engineering/DEPLOY_AND_ROLLBACK_RUNBOOK.md" "scripts/dev/rollback-drill.sh"
require_file_contains "docs/harness-engineering/PROJECT_PROGRESS_REPORT.md" "## 汇总结论"
require_file_contains "docs/harness-engineering/PROJECT_PROGRESS_REPORT.md" "## 下一阶段演进路线"
require_file_contains "docs/harness-engineering/SYMPHONY_TROUBLESHOOTING.md" "scripts/smoke/symphony-state.sh"
require_file_contains "docs/harness-engineering/QUALITY_ISSUE_STANDARD.md" "quality-issues/archive/"

print_step "Docs consistency check passed"
