#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

required_files=(
  "artemis-symphony/skills/new-domain-service.md"
  "artemis-symphony/skills/new-dubbo-client.md"
  "artemis-symphony/skills/add-archunit-rule.md"
  "artemis-symphony/skills/contract-change.md"
  "artemis-symphony/skills/deploy-drill.md"
  "artemis-symphony/skills/expand-existing-service.md"
  "artemis-symphony/prompts/self-review-and-handoff.md"
  "artemis-symphony/prompts/contract-change-review.md"
  "artemis-symphony/prompts/deploy-drill-report.md"
  "artemis-symphony/prompts/phase-delivery-plan.md"
)

print_step "Checking Symphony assets"
for file in "${required_files[@]}"; do
  [[ -f "$file" ]] || {
    echo "Missing Symphony asset: $file" >&2
    exit 1
  }
done

grep -Fq "contract-change.md" artemis-symphony/README.md
grep -Fq "deploy-drill.md" artemis-symphony/README.md
grep -Fq "expand-existing-service.md" artemis-symphony/README.md
grep -Fq "contract-change.md" artemis-symphony/WORKFLOW.md.example
grep -Fq "deploy-drill.md" artemis-symphony/WORKFLOW.md.example
grep -Fq "phase-delivery-plan.md" artemis-symphony/README.md

print_step "Symphony asset check passed"
