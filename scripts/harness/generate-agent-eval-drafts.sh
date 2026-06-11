#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd python3

print_step "Generating agent eval dataset drafts"

python3 - "$@" <<'PY'
from __future__ import annotations

from pathlib import Path
import json
import re
import sys
import tempfile
from typing import Any

repo = Path.cwd()
args = sys.argv[1:]
output_dir = repo / "artifacts/agent-eval-drafts"
scan_args: list[str] = []
self_test = False

index = 0
while index < len(args):
    arg = args[index]
    if arg == "--self-test":
        self_test = True
        index += 1
    elif arg == "--output-dir":
        if index + 1 >= len(args):
            raise SystemExit("--output-dir requires a value")
        output_dir = Path(args[index + 1])
        if not output_dir.is_absolute():
            output_dir = repo / output_dir
        index += 2
    else:
        scan_args.append(arg)
        index += 1

if self_test and scan_args:
    raise SystemExit("--self-test cannot be combined with summary paths")


def string(value: Any, default: str = "") -> str:
    return value if isinstance(value, str) else default


def obj(value: Any) -> dict[str, Any]:
    return value if isinstance(value, dict) else {}


def list_value(value: Any) -> list[Any]:
    return value if isinstance(value, list) else []


def safe_slug(value: str, default: str = "run") -> str:
    slug = re.sub(r"[^a-z0-9]+", "-", value.lower()).strip("-")
    return slug[:80] or default


def yaml_scalar(value: str) -> str:
    if not value:
        return "''"
    if re.fullmatch(r"[A-Za-z0-9_.:/@+-]+", value) and value.lower() not in {"true", "false", "null"}:
        return value
    return "'" + value.replace("'", "''") + "'"


def yaml_bool(value: bool) -> str:
    return "true" if value else "false"


def block(value: str, indent: str = "  ") -> list[str]:
    lines = value.splitlines() or [""]
    return [f"{indent}{line}" if line else indent.rstrip() for line in lines]


def rel_path(path: Path) -> str:
    resolved = path.resolve()
    try:
        return str(resolved.relative_to(repo))
    except ValueError:
        return str(resolved)


def safe_source_path(path: Path) -> str:
    resolved = path.resolve()
    try:
        return str(resolved.relative_to(repo))
    except ValueError:
        return f"external-summary/{path.name}"


def candidate_json_files(paths: list[str]) -> list[Path]:
    if not paths:
        defaults = [repo / "artifacts/agent-runs", repo / "artifacts/agent-evals"]
        paths = [str(path) for path in defaults if path.exists()]
    files: list[Path] = []
    for raw in paths:
        path = Path(raw)
        if not path.is_absolute():
            path = repo / path
        if not path.exists():
            raise SystemExit(f"summary path does not exist: {raw}")
        if path.is_file():
            files.append(path)
        else:
            files.extend(sorted(path.rglob("*.json")))
    return sorted(dict.fromkeys(files))


def load_summary(path: Path) -> dict[str, Any] | None:
    try:
        body = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        raise SystemExit(f"{rel_path(path)}:{exc.lineno}: invalid JSON ({exc.msg})") from exc
    if not isinstance(body, dict) or body.get("summary_type") != "symphony_agent_run":
        return None
    if body.get("status") == "completed":
        return None
    return body


def codex_started(event_counts: dict[str, Any]) -> bool:
    for event in event_counts:
        if event not in {"run_started", "run_failed", "run_completed", "retry_scheduled"}:
            return True
    return False


def infer_turn_outcome(event_counts: dict[str, Any], failure_category: str) -> str:
    events = set(event_counts)
    if "tool_call_failed" in events:
        return "dynamic_tool_failure"
    if "approval_required" in events:
        return "approval_required"
    if "turn_input_required" in events:
        return "user_input_required"
    if "turn_cancelled" in events:
        return "cancelled"
    if "turn_failed" in events:
        return "failed"
    if "turn_ended_with_error" in events and "thread/tokenUsage/updated" in events:
        return "timeout"
    if "turn_ended_with_error" in events:
        return "response_timeout"
    if failure_category == "codex_runtime":
        return "failed"
    return ""


def risk_level(failure_category: str, permissions: dict[str, Any]) -> str:
    if failure_category == "permission":
        return "high"
    if permissions.get("network_access") is True:
        return "high"
    return "medium"


def external_effect_lines(events: list[Any]) -> tuple[list[str], list[str]]:
    effects: list[str] = []
    details: list[str] = []
    for item in events:
        if not isinstance(item, dict):
            continue
        effect_type = string(item.get("type")).strip()
        status = string(item.get("status")).strip()
        error_code = string(item.get("error_code")).strip()
        if effect_type and status:
            value = f"{effect_type}:{status}"
            if value not in effects:
                effects.append(value)
            if error_code:
                detail = f"{effect_type}:{status}:{error_code}"
                if detail not in details:
                    details.append(detail)
    return effects, details


def generate_draft(summary: dict[str, Any], source_path: Path) -> tuple[str, str]:
    issue = obj(summary.get("issue"))
    codex = obj(summary.get("codex"))
    permissions = obj(summary.get("permissions"))
    external_effects = obj(summary.get("external_effects"))
    event_counts = obj(codex.get("event_counts"))
    failure_category = string(summary.get("failure_category"), "unknown").strip() or "unknown"
    run_id = string(summary.get("run_id"), "run").strip() or "run"
    source_identifier = string(issue.get("identifier"), run_id).strip() or run_id
    draft_id = f"memory-regression-{safe_slug(source_identifier)}-{safe_slug(failure_category)}"
    title = f"Memory regression draft for {failure_category}"
    issue_title = string(issue.get("title"), "Generated failure regression").strip() or "Generated failure regression"
    failure_reason = string(summary.get("failure_reason"), failure_category).strip() or failure_category
    failure_reason = " ".join(failure_reason.split())[:180]
    source_reference = safe_source_path(source_path)
    started = codex_started(event_counts)
    events = ["run_started"]
    for event in sorted(event_counts):
        if isinstance(event, str) and event and event not in events:
            events.append(event)
    if "run_failed" not in events:
        events.append("run_failed")
    forbidden_events: list[str] = []
    if not started:
        forbidden_events.extend(["session_started", "thread/tokenUsage/updated", "turn_completed"])
    elif "turn_completed" not in event_counts:
        forbidden_events.append("turn_completed")
    effects, effect_details = external_effect_lines(list_value(external_effects.get("events")))
    turn_outcome = infer_turn_outcome(event_counts, failure_category)
    total_tokens = obj(codex.get("usage")).get("total_tokens")

    lines = [
        "# Generated from a low-sensitive symphony_agent_run summary.",
        "# Manual review is required before moving this draft into docs/agent-evals/datasets/.",
        f"# source_summary: {source_reference}",
        f"# source_run_id: {run_id}",
        f"id: {yaml_scalar(draft_id)}",
        f"title: {yaml_scalar(title)}",
        f"risk_level: {risk_level(failure_category, permissions)}",
        "tracker: memory",
        f"issue_id: {yaml_scalar('eval-' + draft_id)}",
        f"issue_identifier: {yaml_scalar(source_identifier if source_identifier.startswith('EVAL-') else 'EVAL-DRAFT')}",
        f"issue_title: {yaml_scalar(issue_title)}",
        "issue_state: Todo",
        "issue_description: |",
        *block(
            "\n".join(
                [
                    f"Generated regression seed from low-sensitive Symphony run summary {run_id}.",
                    f"Source failure category: {failure_category}.",
                    "Replace this synthetic scenario with the smallest stable reproduction before committing.",
                ]
            )
        ),
        "manual_review_required: true",
        f"source_summary: {yaml_scalar(source_reference)}",
        f"source_run_id: {yaml_scalar(run_id)}",
        "expected_workspace_file: EVAL_RESULT.md",
        "expected_workspace_contains: should not be written",
        "expected_workspace_present: false",
        f"expected_codex_started: {yaml_bool(started)}",
        "expected_summary_status: failed",
        "expected_history_status: failed",
        f"expected_failure_reason_contains: {yaml_scalar(failure_reason)}",
        f"expected_failure_category: {yaml_scalar(failure_category)}",
    ]
    if permissions.get("network_access") is True:
        lines.append("expected_permission_network_access: true")
    if isinstance(total_tokens, int) and total_tokens > 0:
        lines.append(f"expected_min_total_tokens: {total_tokens}")
    if failure_category == "codex_startup" or "startup_failed" in event_counts:
        lines.append("codex_startup_outcome: initialize_error")
    elif turn_outcome:
        lines.append(f"codex_turn_outcome: {turn_outcome}")
    lines.append("expected_events:")
    lines.extend(f"  - {event}" for event in events)
    if forbidden_events:
        lines.append("forbidden_events:")
        lines.extend(f"  - {event}" for event in forbidden_events)
    if effects:
        lines.append("expected_external_effects:")
        lines.extend(f"  - {effect}" for effect in effects)
    if effect_details:
        lines.append("expected_external_effect_details:")
        lines.extend(f"  - {detail}" for detail in effect_details)
    lines.extend(
        [
            "required_validations:",
            "  - scripts/e2e/run-symphony-agent-eval.sh",
            "",
        ]
    )
    return draft_id, "\n".join(lines)


def write_drafts(paths: list[str], destination: Path) -> list[Path]:
    destination.mkdir(parents=True, exist_ok=True)
    written: list[Path] = []
    for path in candidate_json_files(paths):
        summary = load_summary(path)
        if summary is None:
            continue
        draft_id, text = generate_draft(summary, path)
        target = destination / f"{draft_id}.yml"
        target.write_text(text, encoding="utf-8")
        written.append(target)
    return written


def fixture_summary(path: Path) -> None:
    body = {
        "schema_version": 1,
        "summary_type": "symphony_agent_run",
        "run_id": "run-fixture-1",
        "status": "failed",
        "failure_reason": "codex turn failed: synthetic fixture failure",
        "failure_category": "codex_runtime",
        "issue": {
            "id": "issue-fixture",
            "identifier": "EVAL-99",
            "title": "Synthetic failed run",
            "state": "Todo",
        },
        "codex": {
            "event_counts": {
                "session_started": 1,
                "thread/tokenUsage/updated": 1,
                "turn_failed": 1,
            },
            "usage": {
                "total_tokens": 42,
            },
        },
        "permissions": {
            "network_access": False,
        },
        "external_effects": {
            "events": [
                {
                    "type": "tracker_state_update",
                    "status": "succeeded",
                    "error_code": "",
                }
            ]
        },
    }
    path.write_text(json.dumps(body, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


if self_test:
    with tempfile.TemporaryDirectory() as tmp:
        root = Path(tmp)
        summary_path = root / "summary.json"
        fixture_summary(summary_path)
        destination = root / "drafts"
        written = write_drafts([str(summary_path)], destination)
        if len(written) != 1:
            raise SystemExit(f"expected one draft, got {len(written)}")
        text = written[0].read_text(encoding="utf-8")
        required = [
            "manual_review_required: true",
            "source_summary: external-summary/summary.json",
            "expected_summary_status: failed",
            "expected_failure_category: codex_runtime",
            "codex_turn_outcome: failed",
            "expected_min_total_tokens: 42",
            "tracker_state_update:succeeded",
        ]
        missing = [marker for marker in required if marker not in text]
        if missing:
            raise SystemExit(f"draft fixture missing markers: {missing}")
    print("Agent eval draft generator self-test passed.")
    raise SystemExit(0)

written = write_drafts(scan_args, output_dir)
if not written:
    print("No failed symphony_agent_run summaries found.")
else:
    print(f"Generated {len(written)} agent eval dataset draft(s):")
    for path in written:
        print(f"  - {rel_path(path)}")
PY
