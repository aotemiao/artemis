#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

run_governance_check() {
  local name="$1"
  local script="$2"

  print_step "Governance: ${name}"
  if ! "$script"; then
    echo "Governance sub-check failed: ${name} (${script})" >&2
    exit 1
  fi
}

print_step "Running governance checks"
run_governance_check "Markdown Links" scripts/harness/check-doc-links.sh
run_governance_check "Docs Consistency" scripts/harness/check-doc-consistency.sh
run_governance_check "Docs Freshness" scripts/harness/check-doc-freshness.sh
run_governance_check "API Doc Sync" scripts/harness/check-api-doc-sync.sh
run_governance_check "Client Contracts" scripts/harness/check-client-contracts.sh
run_governance_check "Domain Service Scaffold" scripts/harness/check-domain-service-scaffold.sh
run_governance_check "Service Catalog" scripts/harness/check-service-catalog.sh
run_governance_check "Symphony Assets" scripts/harness/check-symphony-assets.sh
run_governance_check "Critical Path Tests" scripts/harness/check-critical-path-tests.sh
run_governance_check "Duplicate Patterns" scripts/harness/check-duplicate-patterns.sh
run_governance_check "Quality Issue Archive" scripts/harness/check-quality-issue-archive.sh

print_step "Governance checks completed"
