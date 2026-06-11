#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

usage() {
  cat <<'USAGE'
Usage: scripts/harness/collect-github-delivery-signal.sh [event_json] [output_json]

Collects low-sensitivity GitHub Actions event counts for Harness metrics.

Defaults:
  event_json  = $GITHUB_EVENT_PATH
  output_json = artifacts/harness-delivery-signals/github-event.json
USAGE
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

if [[ "$#" -gt 2 ]]; then
  usage >&2
  exit 1
fi

run_in_repo_root
require_cmd python3

event_json="${1:-${GITHUB_EVENT_PATH:-}}"
output_json="${2:-${HARNESS_DELIVERY_SIGNAL_OUTPUT:-artifacts/harness-delivery-signals/github-event.json}}"
mkdir -p "$(dirname "$output_json")"

python3 - "$event_json" "$output_json" \
  "${GITHUB_EVENT_NAME:-}" \
  "${GITHUB_REPOSITORY:-}" \
  "${GITHUB_RUN_ID:-}" <<'PY'
from __future__ import annotations

from datetime import datetime, timezone
from pathlib import Path
import json
import sys

event_path = Path(sys.argv[1]) if sys.argv[1] else None
output_path = Path(sys.argv[2])
event_name_env = sys.argv[3].strip()
repository = sys.argv[4].strip()
run_id = sys.argv[5].strip()


def read_payload(path: Path | None) -> dict:
    if path is None or not path.exists():
        return {}
    return json.loads(path.read_text(encoding="utf-8"))


def infer_event_name(payload: dict) -> str:
    if event_name_env:
        return event_name_env
    if "pull_request" in payload and "review" in payload:
        return "pull_request_review"
    if "pull_request" in payload and "comment" in payload:
        return "pull_request_review_comment"
    if "pull_request" in payload:
        return "pull_request"
    if "commits" in payload:
        return "push"
    return "unknown"


def parse_time(value: object) -> datetime | None:
    if not isinstance(value, str) or not value.strip():
        return None
    try:
        return datetime.fromisoformat(value.replace("Z", "+00:00"))
    except ValueError:
        return None


def seconds_between(start: object, end: object) -> int | None:
    started = parse_time(start)
    ended = parse_time(end)
    if started is None or ended is None:
        return None
    return max(0, int((ended - started).total_seconds()))


def bump(counts: dict[str, int], key: str) -> None:
    normalized = (key or "unknown").strip().lower() or "unknown"
    counts[normalized] = counts.get(normalized, 0) + 1


payload = read_payload(event_path)
event_name = infer_event_name(payload)
action = str(payload.get("action") or "").strip()
pull_request = payload.get("pull_request") if isinstance(payload.get("pull_request"), dict) else {}
review = payload.get("review") if isinstance(payload.get("review"), dict) else {}

pull_requests = {
    "created": 0,
    "merged": 0,
    "reverted": 0,
    "merge_time_seconds": [],
}
review_findings = {
    "total": 0,
    "severity_counts": {},
    "category_counts": {},
}

if event_name == "pull_request":
    if action == "opened":
        pull_requests["created"] = 1
    if action == "closed" and pull_request.get("merged") is True:
        pull_requests["merged"] = 1
        merge_time = seconds_between(pull_request.get("created_at"), pull_request.get("merged_at"))
        if merge_time is not None:
            pull_requests["merge_time_seconds"].append(merge_time)
elif event_name == "push":
    commits = payload.get("commits") if isinstance(payload.get("commits"), list) else []
    reverted = 0
    for commit in commits:
        if not isinstance(commit, dict):
            continue
        message = str(commit.get("message") or "")
        if message.startswith("Revert ") or "This reverts commit" in message:
            reverted += 1
    pull_requests["reverted"] = reverted

if event_name == "pull_request_review":
    state = str(review.get("state") or "").strip().lower()
    if action == "submitted" and state == "changes_requested":
        review_findings["total"] = 1
        bump(review_findings["severity_counts"], "unknown")
        bump(review_findings["category_counts"], "changes_requested")
elif event_name in {"pull_request_review_comment", "pull_request_review_thread"}:
    if action in {"created", "resolved", "unresolved"}:
        review_findings["total"] = 1
        bump(review_findings["severity_counts"], "unknown")
        bump(review_findings["category_counts"], "review_comment")

body = {
    "schema_version": 1,
    "summary_type": "harness_delivery_signal",
    "provider": "github",
    "generated_at": datetime.now(timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z"),
    "source": {
        "event_name": event_name,
        "action": action,
        "repository": repository,
        "run_id": run_id,
        "event_payload_available": bool(payload),
    },
    "pull_requests": pull_requests,
    "review_findings": review_findings,
}

output_path.write_text(json.dumps(body, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print(f"Wrote {output_path}")
PY
