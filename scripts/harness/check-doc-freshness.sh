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
  "docs/security/THREAT_MODEL.md"
  "docs/api/README.md"
  "docs/feature-specs/README.md"
  "docs/feature-specs/examples/menu-permission-feature-spec.md"
  "docs/feature-specs/completed/2026-06-01-tenant-creation-initialization.md"
  "docs/agent-evals/README.md"
  "docs/exec-plans/README.md"
  "docs/patterns/README.md"
  "docs/patterns/spec-to-validation-mapping.md"
  "docs/patterns/agent-delivery-handoff.md"
  "docs/patterns/security-review-checklist.md"
  "docs/governance/CHECKLIST.md"
  "docs/reports/ROADMAP.md"
  "docs/reports/PROJECT_PROGRESS_REPORT.md"
  "docs/runbooks/SERVICE_SMOKE_RUNBOOK.md"
  "docs/runbooks/ADD_DOMAIN_SERVICE_RUNBOOK.md"
  "docs/runbooks/ADD_DUBBO_CLIENT_RUNBOOK.md"
  "docs/runbooks/ADD_ARCHUNIT_RULE_RUNBOOK.md"
  "docs/runbooks/AGENT_PERMISSION_RUNBOOK.md"
  "docs/runbooks/RISK_BASED_VERIFICATION_RUNBOOK.md"
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
  "docs/reports/agent-runs/README.md"
)

max_cadence_days=90

print_step "Checking doc freshness metadata"

for doc in "${docs[@]}"; do
  require_repo_path_exact "$doc"
  python3 - "$doc" "$max_cadence_days" <<'PY'
from datetime import date, datetime
from pathlib import Path
import re
import sys
from zoneinfo import ZoneInfo

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

today = datetime.now(ZoneInfo("Asia/Shanghai")).date()
age_days = (today - reviewed).days

if age_days < 0:
    fail("Last Reviewed is in the future")

if age_days > cadence_days:
    fail(f"document review is stale by {age_days - cadence_days} days")

print(f"{path}: OK ({age_days} days old)")
PY
done

print_step "Checking doc evidence freshness"

python3 - <<'PY'
from pathlib import Path
import sys

repo = Path.cwd()
checks = {
    "QUALITY_SCORE.md": [
        "## 验证与变更快照",
        "scripts/harness/verify-changed.sh working-tree",
        "scripts/e2e/run-symphony-agent-eval.sh",
        "Harness metrics 快照",
    ],
    "docs/governance/CHECKLIST.md": [
        "scripts/harness/check-agentic-harness-assets.sh",
        "scripts/e2e/run-symphony-agent-eval.sh",
        "run environment 快照",
        "CI artifact 快照",
    ],
    "docs/reports/ROADMAP.md": [
        "memory agent eval 可真实启动 Symphony",
        "Harness metrics report generator",
        "run environment",
        "deploy drill",
    ],
    "docs/reports/agent-runs/README.md": [
        "## 验证证据",
        "summary_type=symphony_agent_run",
        "codex.event_counts",
        "environment",
        "不应包含事件 payload",
    ],
    "docs/reports/harness-metrics/README.md": [
        "scripts/harness/generate-harness-metrics-report.sh",
        "artifacts/agent-evals/",
        "summary_type=harness_delivery_signal",
        "summary_type=deploy_drill_report",
        "不会读取完整 prompt",
    ],
    "docs/reports/deploy-drills/README.md": [
        "## 指标摘要",
        "summary_type",
        "deploy_drill_report",
        "scripts/harness/check-deploy-drill-reports.sh",
    ],
    "docs/governance/DOC_FRESHNESS_POLICY.md": [
        "## 证据 freshness",
        "验证入口",
        "低敏",
        "artifacts",
    ],
}

errors: list[str] = []
for rel, markers in checks.items():
    path = repo / rel
    if not path.exists():
        errors.append(f"{rel}: missing file")
        continue
    text = path.read_text(encoding="utf-8")
    for marker in markers:
        if marker not in text:
            errors.append(f"{rel}: missing evidence freshness marker: {marker}")

if errors:
    print("Doc evidence freshness check failed:", file=sys.stderr)
    for error in errors:
        print(f"  - {error}", file=sys.stderr)
    raise SystemExit(1)

print("Doc evidence freshness check passed.")
PY

print_step "Docs freshness check passed"
