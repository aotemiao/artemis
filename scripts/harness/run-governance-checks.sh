#!/bin/sh
# 仓库级治理检查编排器。POSIX sh，可跨平台运行；子检查各自保留原 shebang。
set -eu

cd "$(git rev-parse --show-toplevel)"

run_governance_check() {
  name="$1"
  script="$2"
  echo
  echo "==> Governance: ${name}"
  if ! bash "$script"; then
    echo "Governance sub-check failed: ${name} (${script})" >&2
    exit 1
  fi
}

echo
echo "==> Running governance checks"
run_governance_check "Markdown Links" scripts/harness/check-doc-links.sh
run_governance_check "OpenSpec Change State" scripts/harness/check-openspec-change-state.sh
run_governance_check "Feature Specs" scripts/harness/check-feature-specs.sh
run_governance_check "Spec Driven Delivery Chain" scripts/harness/check-spec-driven-delivery-chain.sh
run_governance_check "Agentic Harness Assets" scripts/harness/check-agentic-harness-assets.sh
run_governance_check "Agent Run Summaries" scripts/harness/check-agent-run-summaries.sh
run_governance_check "API Doc Sync" scripts/harness/check-api-doc-sync.sh
run_governance_check "Client Contracts" scripts/harness/check-client-contracts.sh
run_governance_check "Capability Package Structure" scripts/harness/check-capability-package-structure.sh
run_governance_check "Domain Service Scaffold" scripts/harness/check-domain-service-scaffold.sh
run_governance_check "Service Catalog" scripts/harness/check-service-catalog.sh
run_governance_check "Deploy Drill Reports" scripts/harness/check-deploy-drill-reports.sh
run_governance_check "Critical Path Tests" scripts/harness/check-critical-path-tests.sh
run_governance_check "Duplicate Patterns" scripts/harness/check-duplicate-patterns.sh
run_governance_check "Quality Issue Archive" scripts/harness/check-quality-issue-archive.sh

echo
echo "==> Governance checks completed"
