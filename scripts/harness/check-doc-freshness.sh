#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd python3

docs=(
  "README.md"
  "AGENTS.md"
  "ARCHITECTURE.md"
  "QUALITY_SCORE.md"
  "docs/README.md"
  "docs/api/README.md"
  "docs/feature-specs/README.md"
  "docs/feature-specs/examples/menu-permission-feature-spec.md"
  "docs/feature-specs/completed/2026-06-01-tenant-creation-initialization.md"
  "docs/exec-plans/README.md"
  "docs/patterns/README.md"
  "docs/patterns/spec-to-validation-mapping.md"
  "docs/patterns/agent-delivery-handoff.md"
  "docs/governance/CHECKLIST.md"
  "docs/reports/ROADMAP.md"
  "docs/reports/PROJECT_PROGRESS_REPORT.md"
  "docs/runbooks/SERVICE_SMOKE_RUNBOOK.md"
  "docs/runbooks/ADD_DOMAIN_SERVICE_RUNBOOK.md"
  "docs/runbooks/ADD_DUBBO_CLIENT_RUNBOOK.md"
  "docs/runbooks/ADD_ARCHUNIT_RULE_RUNBOOK.md"
  "docs/agent-workflow/AGENT_DEVELOPMENT_WORKFLOW.md"
  "docs/agent-workflow/AGENT_REVIEW_LOOP.md"
  "docs/governance/DOC_FRESHNESS_POLICY.md"
  "docs/runbooks/DEPLOY_AND_ROLLBACK_RUNBOOK.md"
  "docs/runbooks/SYMPHONY_TROUBLESHOOTING.md"
  "docs/governance/QUALITY_ISSUE_STANDARD.md"
  "docs/governance/quality-issues/README.md"
  "docs/governance/quality-issues/archive/2026-03-23-credential-request-duplication.md"
  "docs/reports/deploy-drills/README.md"
  "docs/reports/deploy-drills/2026-06-01-sample-report-template.md"
)

max_cadence_days=90

print_step "Checking doc freshness metadata"

for doc in "${docs[@]}"; do
  require_repo_path_exact "$doc"
  python3 - "$doc" "$max_cadence_days" <<'PY'
from datetime import date
from pathlib import Path
import re
import sys

path = Path(sys.argv[1])
max_cadence_days = int(sys.argv[2])
text = path.read_text(encoding="utf-8")
head = "\n".join(text.splitlines()[:15])

def fail(message: str) -> None:
    print(f"{path}: {message}", file=sys.stderr)
    raise SystemExit(1)

if not re.search(r"^Status:\s+\S.*$", head, re.MULTILINE):
    fail("missing Status metadata")

reviewed_match = re.search(r"^Last Reviewed:\s+(\d{4}-\d{2}-\d{2})$", head, re.MULTILINE)
if not reviewed_match:
    fail("missing Last Reviewed metadata")

cadence_match = re.search(r"^Review Cadence:\s+(\d+)\s+days$", head, re.MULTILINE)
if not cadence_match:
    fail("missing Review Cadence metadata")

reviewed = date.fromisoformat(reviewed_match.group(1))
cadence_days = int(cadence_match.group(1))

if cadence_days > max_cadence_days:
    fail(f"review cadence exceeds maximum allowed {max_cadence_days} days")

today = date.today()
age_days = (today - reviewed).days

if age_days < 0:
    fail("Last Reviewed is in the future")

if age_days > cadence_days:
    fail(f"document review is stale by {age_days - cadence_days} days")

print(f"{path}: OK ({age_days} days old)")
PY
done

print_step "Docs freshness check passed"
