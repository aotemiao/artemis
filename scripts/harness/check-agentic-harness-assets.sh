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

print_step "Checking agentic harness assets"

required_files=(
  "docs/patterns/security-review-checklist.md"
  "docs/runbooks/AGENT_PERMISSION_RUNBOOK.md"
  "docs/runbooks/RISK_BASED_VERIFICATION_RUNBOOK.md"
  "docs/security/THREAT_MODEL.md"
  "docs/agent-evals/README.md"
  "docs/agent-evals/fixtures/ambiguous-business-request.yml"
  "docs/agent-evals/fixtures/high-risk-permission-change.yml"
  "docs/reports/agent-runs/README.md"
  "artemis-symphony/prompts/adversarial-review.md"
  "artemis-symphony/skills/adversarial-review.md"
  "scripts/harness/run-agent-evals.sh"
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
require_file_contains "docs/reports/agent-runs/README.md" "禁止提交"
require_file_contains "artemis-symphony/WORKFLOW.md.example" "artemis-symphony/skills/adversarial-review.md"
require_file_contains "artemis-symphony/WORKFLOW.md.example" "docs/runbooks/AGENT_PERMISSION_RUNBOOK.md"
require_file_contains "artemis-symphony/prompts/README.md" "agent-requirement-intake.md"
require_file_contains "artemis-symphony/prompts/README.md" "adversarial-review.md"
require_file_contains "artemis-symphony/skills/README.md" "adversarial-review.md"
require_file_contains "docs/asset-manifest.yml" "external-agent-pointer"
require_file_contains ".github/copilot-instructions.md" "AGENTS.md"
require_file_contains "CLAUDE.md" "AGENTS.md"
require_file_contains "GEMINI.md" "AGENTS.md"
require_file_contains ".github/workflows/governance.yml" "scripts/harness/run-governance-checks.sh"
require_file_contains ".github/workflows/verify.yml" "scripts/harness/full-verify.sh"

if contains_fixed_string "Governance - Markdown Links" ".github/workflows/verify.yml"; then
  echo "Verify workflow still contains duplicated per-check governance steps." >&2
  exit 1
fi

scripts/harness/run-agent-evals.sh

print_step "Agentic harness asset check passed"
