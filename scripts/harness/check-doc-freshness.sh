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
  "docs/exec-plans/README.md"
  "docs/harness-engineering/README.md"
  "docs/harness-engineering/CHECKLIST.md"
  "docs/harness-engineering/ROADMAP.md"
  "docs/harness-engineering/PROJECT_PROGRESS_REPORT.md"
  "docs/harness-engineering/SERVICE_SMOKE_RUNBOOK.md"
  "docs/harness-engineering/ADD_DOMAIN_SERVICE_RUNBOOK.md"
  "docs/harness-engineering/ADD_DUBBO_CLIENT_RUNBOOK.md"
  "docs/harness-engineering/ADD_ARCHUNIT_RULE_RUNBOOK.md"
  "docs/harness-engineering/AGENT_REVIEW_LOOP.md"
  "docs/harness-engineering/DOC_FRESHNESS_POLICY.md"
  "docs/harness-engineering/DEPLOY_AND_ROLLBACK_RUNBOOK.md"
  "docs/harness-engineering/SYMPHONY_TROUBLESHOOTING.md"
  "docs/harness-engineering/QUALITY_ISSUE_STANDARD.md"
  "docs/harness-engineering/quality-issues/README.md"
  "docs/harness-engineering/quality-issues/active/README.md"
  "docs/harness-engineering/quality-issues/archive/README.md"
  "docs/harness-engineering/quality-issues/archive/2026-03-23-credential-request-duplication.md"
)

max_cadence_days=90

print_step "Checking doc freshness metadata"

for doc in "${docs[@]}"; do
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
