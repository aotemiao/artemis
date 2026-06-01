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

print_step "Checking spec-driven delivery chain"

require_repo_path_exact "docs/feature-specs/README.md"
require_repo_path_exact "docs/feature-specs/templates/feature-spec-template.md"
require_repo_path_exact "docs/exec-plans/templates/execution-plan-template.md"
require_repo_path_exact "docs/patterns/spec-to-validation-mapping.md"
require_repo_path_exact "docs/patterns/agent-delivery-handoff.md"
require_repo_path_exact "artemis-symphony/skills/spec-driven-delivery.md"
require_repo_path_exact "artemis-symphony/prompts/spec-driven-delivery.md"
require_repo_path_exact "openspec/specs/spec-driven-delivery/spec.md"
require_repo_path_exact "scripts/harness/check-feature-specs.sh"
require_repo_path_exact "scripts/harness/check-deploy-drill-reports.sh"

require_file_contains "docs/feature-specs/templates/feature-spec-template.md" "## 验证映射"
require_file_contains "docs/exec-plans/templates/execution-plan-template.md" "## 验收映射"
require_file_contains "docs/exec-plans/templates/execution-plan-template.md" "## 回滚策略"
require_file_contains "artemis-symphony/skills/spec-driven-delivery.md" "scripts/harness/check-feature-specs.sh"
require_file_contains "artemis-symphony/prompts/spec-driven-delivery.md" "docs/feature-specs/active/"
require_file_contains "artemis-symphony/WORKFLOW.md.example" "artemis-symphony/skills/spec-driven-delivery.md"
require_file_contains "artemis-symphony/WORKFLOW.md.example" "artemis-symphony/prompts/spec-driven-delivery.md"
require_file_contains "artemis-symphony/WORKFLOW.md.example" "delivery:"
require_file_contains "artemis-symphony/WORKFLOW.md.example" "spec_driven:"
require_file_contains "artemis-symphony/WORKFLOW.md.example" "enabled: true"
require_file_contains "artemis-symphony/artemis-symphony-config/src/main/java/com/aotemiao/artemis/symphony/config/ServiceConfig.java" "isSpecDrivenDeliveryEnabled"
require_file_contains "artemis-symphony/artemis-symphony-orchestrator/src/main/java/com/aotemiao/artemis/symphony/orchestrator/AgentRunner.java" "appendSpecDrivenDeliveryGuidance"
require_file_contains "artemis-symphony/artemis-symphony-start/src/main/java/com/aotemiao/artemis/symphony/api/SymphonyStateController.java" "delivery"
require_file_contains "scripts/harness/run-governance-checks.sh" "check-feature-specs.sh"
require_file_contains "scripts/harness/run-governance-checks.sh" "check-deploy-drill-reports.sh"
require_file_contains "README.md" "docs/feature-specs/"
require_file_contains "AGENTS.md" "openspec/specs/spec-driven-delivery/spec.md"

print_step "Spec-driven delivery chain check passed"
