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
  if ! contains_fixed_string "$pattern" "$file"; then
    echo "Missing required content in $file: $pattern" >&2
    exit 1
  fi
}

require_file_not_contains() {
  local file="$1"
  local pattern="$2"
  if contains_fixed_string "$pattern" "$file"; then
    echo "Outdated content found in $file: $pattern" >&2
    exit 1
  fi
}

print_step "Checking docs index and core entry docs"
require_file_contains "docs/README.md" "SERVICE_SMOKE_RUNBOOK.md"
require_file_contains "docs/README.md" "DOC_FRESHNESS_POLICY.md"
require_file_contains "docs/README.md" "DEPLOY_AND_ROLLBACK_RUNBOOK.md"
require_file_contains "docs/README.md" "SYMPHONY_TROUBLESHOOTING.md"

print_step "Checking README consistency"
require_file_contains "README.md" "scripts/dev/package-service.sh"
require_file_contains "README.md" "scripts/dev/build-image.sh"
require_file_contains "README.md" "DEPLOY_AND_ROLLBACK_RUNBOOK.md"
require_file_contains "README.md" "SYMPHONY_TROUBLESHOOTING.md"

print_step "Checking AGENTS consistency"
require_file_contains "AGENTS.md" "scripts/dev/package-service.sh"
require_file_contains "AGENTS.md" "scripts/dev/build-image.sh"
require_file_contains "AGENTS.md" "docs/harness-engineering/DEPLOY_AND_ROLLBACK_RUNBOOK.md"
require_file_contains "AGENTS.md" "docs/harness-engineering/SYMPHONY_TROUBLESHOOTING.md"

print_step "Checking ARCHITECTURE drift"
require_file_contains "ARCHITECTURE.md" "scripts/dev/package-service.sh"
require_file_contains "ARCHITECTURE.md" "scripts/dev/build-image.sh"
require_file_not_contains 'ARCHITECTURE.md' 'mvn verify` 级别的统一守门尚未完全固化到 Maven lifecycle'

print_step "Checking harness docs consistency"
require_file_contains "docs/harness-engineering/README.md" "SERVICE_SMOKE_RUNBOOK.md"
require_file_contains "docs/harness-engineering/README.md" "DOC_FRESHNESS_POLICY.md"
require_file_contains "docs/harness-engineering/README.md" "DEPLOY_AND_ROLLBACK_RUNBOOK.md"
require_file_contains "docs/harness-engineering/README.md" "SYMPHONY_TROUBLESHOOTING.md"
require_file_contains "docs/harness-engineering/SERVICE_SMOKE_RUNBOOK.md" "scripts/smoke/symphony-state.sh"
require_file_contains "docs/harness-engineering/DOC_FRESHNESS_POLICY.md" "Last Reviewed"
require_file_contains "docs/harness-engineering/DEPLOY_AND_ROLLBACK_RUNBOOK.md" "scripts/dev/build-image.sh"
require_file_contains "docs/harness-engineering/SYMPHONY_TROUBLESHOOTING.md" "scripts/smoke/symphony-state.sh"

print_step "Docs consistency check passed"
