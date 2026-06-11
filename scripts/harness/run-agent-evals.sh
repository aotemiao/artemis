#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd python3

print_step "Running agent workflow eval fixtures"

python3 - <<'PY'
from pathlib import Path
import os
import re
import sys

repo = Path.cwd()
fixtures_root = Path(os.environ.get("AGENT_EVAL_FIXTURES_DIR", "docs/agent-evals/fixtures"))
datasets_root = Path(os.environ.get("AGENT_EVAL_DATASETS_DIR", "docs/agent-evals/datasets"))
if not fixtures_root.is_absolute():
    fixtures_root = repo / fixtures_root
if not datasets_root.is_absolute():
    datasets_root = repo / datasets_root
fixtures = sorted(fixtures_root.glob("*.yml"))
datasets = sorted(datasets_root.glob("*.yml"))
required_keys = {
    "id",
    "title",
    "risk_level",
    "issue",
    "expected_assets",
    "expected_validations",
    "review_focus",
}
path_keys = {"expected_assets", "expected_validations"}
valid_risks = {"low", "medium", "high"}
dataset_required_keys = {
    "id",
    "title",
    "risk_level",
    "tracker",
    "issue_id",
    "issue_identifier",
    "issue_title",
    "expected_workspace_file",
    "expected_workspace_contains",
    "expected_summary_status",
    "expected_history_status",
    "expected_events",
    "required_validations",
}
dataset_bool_keys = {
    "expected_workspace_present",
    "expected_workspace_directory_present",
    "expected_codex_started",
    "expected_permission_network_access",
    "expected_permission_danger_full_access_allowed",
    "codex_turn_sandbox_network_access",
    "permissions_allow_danger_full_access",
    "spec_driven_delivery_enabled",
    "linear_comment_reporting_enabled",
    "adversarial_review_enabled",
}
dataset_optional_scalar_keys = {
    "expected_failure_reason_contains",
    "expected_failure_category",
    "expected_summary_dispatch_kind",
    "expected_permission_thread_sandbox",
    "expected_permission_network_access_reason",
    "expected_permission_writable_root_contains",
    "expected_permission_allowed_writable_root_contains",
    "codex_turn_sandbox_extra_writable_root",
    "permissions_network_access_reason",
    "permissions_allowed_writable_root",
    "codex_turn_outcome",
    "codex_startup_outcome",
    "codex_approval_policy",
    "codex_thread_sandbox",
    "hook_before_run_outcome",
    "hook_after_create_outcome",
    "linear_comment_success_template",
    "linear_comment_failure_template",
    "linear_comment_issue_title_regex",
    "adversarial_review_issue_title_regex",
}
dataset_optional_int_keys = {
    "expected_agent_run_count",
    "expected_metrics_total_runs",
    "expected_min_total_tokens",
    "codex_read_timeout_ms",
    "codex_turn_timeout_ms",
    "agent_max_retry_backoff_ms",
}
dataset_draft_only_keys = {
    "manual_review_required",
    "source_summary",
    "source_run_id",
}
dataset_draft_only_markers = {
    "Generated from a low-sensitive symphony_agent_run summary",
    "Manual review is required before moving this draft into docs/agent-evals/datasets/",
    "Generated regression seed from low-sensitive Symphony run summary",
    "Replace this synthetic scenario with the smallest stable reproduction before committing",
}
errors: list[str] = []
dataset_ids: dict[str, str] = {}

def display_path(path: Path) -> str:
    try:
        return str(path.relative_to(repo))
    except ValueError:
        return str(path)

if not fixtures:
    errors.append(f"{display_path(fixtures_root)}: no eval fixtures found")
if not datasets:
    errors.append(f"{display_path(datasets_root)}: no executable eval datasets found")

def parse_fixture(path: Path) -> dict[str, list[str] | str]:
    data: dict[str, list[str] | str] = {}
    current_key: str | None = None
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        if not raw_line.strip() or raw_line.lstrip().startswith("#"):
            continue
        if re.match(r"^[A-Za-z0-9_]+:", raw_line):
            key, value = raw_line.split(":", 1)
            key = key.strip()
            value = value.strip()
            current_key = key
            if value == "|":
                data[key] = ""
            elif value:
                data[key] = value
            else:
                data[key] = []
            continue
        if current_key is None:
            continue
        stripped = raw_line.strip()
        if isinstance(data.get(current_key), str):
            data[current_key] = f"{data[current_key]}\n{stripped}".strip()
        elif stripped.startswith("- "):
            existing = data.setdefault(current_key, [])
            if not isinstance(existing, list):
                errors.append(f"{display_path(path)}: key {current_key} mixes scalar and list values")
                continue
            existing.append(stripped[2:].strip())
    return data

for path in fixtures:
    rel = display_path(path)
    data = parse_fixture(path)
    missing = sorted(required_keys - data.keys())
    for key in missing:
        errors.append(f"{rel}: missing key {key}")
    risk = data.get("risk_level")
    if isinstance(risk, str) and risk not in valid_risks:
        errors.append(f"{rel}: invalid risk_level {risk}")
    for key in path_keys:
        value = data.get(key)
        if not isinstance(value, list) or not value:
            errors.append(f"{rel}: {key} must be a non-empty list")
            continue
        for item in value:
            candidate = repo / item
            if not candidate.exists():
                errors.append(f"{rel}: referenced path does not exist: {item}")
    review_focus = data.get("review_focus")
    if not isinstance(review_focus, list) or not review_focus:
        errors.append(f"{rel}: review_focus must be a non-empty list")

for path in datasets:
    rel = display_path(path)
    raw_text = path.read_text(encoding="utf-8")
    data = parse_fixture(path)
    for key in sorted(dataset_draft_only_keys & data.keys()):
        errors.append(
            f"{rel}: draft-only key {key} is not allowed in committed datasets; "
            "review the draft and remove generated metadata"
        )
    for marker in sorted(dataset_draft_only_markers):
        if marker in raw_text:
            errors.append(
                f"{rel}: contains draft-only marker {marker!r}; "
                "replace generated scenario text before committing"
            )
    missing = sorted(dataset_required_keys - data.keys())
    for key in missing:
        errors.append(f"{rel}: missing key {key}")
    dataset_id = data.get("id")
    if isinstance(dataset_id, str) and dataset_id:
        expected_id = path.stem
        if dataset_id != expected_id:
            errors.append(f"{rel}: dataset id must match file name ({expected_id})")
        previous_path = dataset_ids.get(dataset_id)
        if previous_path is not None:
            errors.append(f"{rel}: duplicate dataset id {dataset_id} also declared in {previous_path}")
        else:
            dataset_ids[dataset_id] = rel
    risk = data.get("risk_level")
    if isinstance(risk, str) and risk not in valid_risks:
        errors.append(f"{rel}: invalid risk_level {risk}")
    tracker = data.get("tracker")
    if tracker != "memory":
        errors.append(f"{rel}: only tracker=memory datasets are currently supported")
    expected_events = data.get("expected_events")
    if not isinstance(expected_events, list) or not expected_events:
        errors.append(f"{rel}: expected_events must be a non-empty list")
    else:
        codex_events = [
            event
            for event in expected_events
            if isinstance(event, str)
            and event.strip()
            and not event.strip().startswith("run_")
            and event.strip() != "retry_scheduled"
        ]
        if data.get("expected_codex_started") != "false" and not codex_events:
            errors.append(f"{rel}: datasets that start Codex must declare at least one Codex event")
    forbidden_events = data.get("forbidden_events")
    if forbidden_events is not None and not isinstance(forbidden_events, list):
        errors.append(f"{rel}: forbidden_events must be a list when configured")
    expected_external_effects = data.get("expected_external_effects")
    if expected_external_effects is not None:
        if not isinstance(expected_external_effects, list) or not expected_external_effects:
            errors.append(f"{rel}: expected_external_effects must be a non-empty list when configured")
        else:
            for item in expected_external_effects:
                if not isinstance(item, str) or ":" not in item:
                    errors.append(f"{rel}: expected_external_effects entries must use type:status format")
    expected_external_effect_details = data.get("expected_external_effect_details")
    if expected_external_effect_details is not None:
        if not isinstance(expected_external_effect_details, list) or not expected_external_effect_details:
            errors.append(
                f"{rel}: expected_external_effect_details must be a non-empty list when configured"
            )
        else:
            for item in expected_external_effect_details:
                if not isinstance(item, str) or item.count(":") != 2:
                    errors.append(
                        f"{rel}: expected_external_effect_details entries must use type:status:error_code format"
                    )
    prompt_must_contain = data.get("prompt_must_contain")
    if prompt_must_contain is not None:
        if not isinstance(prompt_must_contain, list) or not prompt_must_contain:
            errors.append(f"{rel}: prompt_must_contain must be a non-empty list when configured")
    expected_summary_dispatch_kinds = data.get("expected_summary_dispatch_kinds")
    if expected_summary_dispatch_kinds is not None:
        if not isinstance(expected_summary_dispatch_kinds, list) or not expected_summary_dispatch_kinds:
            errors.append(
                f"{rel}: expected_summary_dispatch_kinds must be a non-empty list when configured"
            )
    expected_summary_workspace_files = data.get("expected_summary_workspace_files")
    if expected_summary_workspace_files is not None:
        if not isinstance(expected_summary_workspace_files, list) or not expected_summary_workspace_files:
            errors.append(
                f"{rel}: expected_summary_workspace_files must be a non-empty list when configured"
            )
    for key in dataset_bool_keys:
        value = data.get(key)
        if value is not None and value not in {"true", "false"}:
            errors.append(f"{rel}: {key} must be true or false when configured")
    for key in dataset_optional_scalar_keys:
        value = data.get(key)
        if value is not None and not isinstance(value, str):
            errors.append(f"{rel}: {key} must be a scalar string when configured")
    for key in dataset_optional_int_keys:
        value = data.get(key)
        if value is not None:
            try:
                parsed = int(str(value))
                if parsed < 0:
                    errors.append(f"{rel}: {key} must be greater than or equal to 0 when configured")
                if key in {"expected_agent_run_count", "expected_metrics_total_runs"} and parsed <= 0:
                    errors.append(f"{rel}: {key} must be greater than 0 when configured")
            except ValueError:
                errors.append(f"{rel}: {key} must be an integer when configured")
    codex_turn_outcome = data.get("codex_turn_outcome")
    if codex_turn_outcome is not None and codex_turn_outcome not in {"completed", "completed_with_malformed", "failed", "cancelled", "approval_required", "dynamic_tool_failure", "user_input_required", "timeout", "response_timeout"}:
        errors.append(f"{rel}: codex_turn_outcome must be completed, completed_with_malformed, failed, cancelled, approval_required, dynamic_tool_failure, user_input_required, timeout, or response_timeout when configured")
    codex_startup_outcome = data.get("codex_startup_outcome")
    if codex_startup_outcome is not None and codex_startup_outcome not in {"started", "initialize_error"}:
        errors.append(f"{rel}: codex_startup_outcome must be started or initialize_error when configured")
    codex_approval_policy = data.get("codex_approval_policy")
    if codex_approval_policy is not None and codex_approval_policy not in {"never", "on-request", "on-failure", "untrusted"}:
        errors.append(f"{rel}: codex_approval_policy must be never, on-request, on-failure, or untrusted when configured")
    hook_before_run_outcome = data.get("hook_before_run_outcome")
    if hook_before_run_outcome is not None and hook_before_run_outcome not in {"failed"}:
        errors.append(f"{rel}: hook_before_run_outcome must be failed when configured")
    hook_after_create_outcome = data.get("hook_after_create_outcome")
    if hook_after_create_outcome is not None and hook_after_create_outcome not in {"failed"}:
        errors.append(f"{rel}: hook_after_create_outcome must be failed when configured")
    summary_status = data.get("expected_summary_status")
    history_status = data.get("expected_history_status")
    if summary_status == "failed" or history_status == "failed":
        if "expected_failure_reason_contains" not in data:
            errors.append(f"{rel}: failed datasets must declare expected_failure_reason_contains")
        if data.get("expected_codex_started") == "false" and not forbidden_events:
            errors.append(f"{rel}: datasets that stop before Codex must declare forbidden_events")
    validations = data.get("required_validations")
    if not isinstance(validations, list) or not validations:
        errors.append(f"{rel}: required_validations must be a non-empty list")
    else:
        for item in validations:
            candidate = repo / item
            if not candidate.exists():
                errors.append(f"{rel}: referenced validation path does not exist: {item}")

if errors:
    print("Agent eval fixture check failed:", file=sys.stderr)
    for error in errors:
        print(f"  - {error}", file=sys.stderr)
    raise SystemExit(1)

print(f"Agent eval fixture check passed ({len(fixtures)} fixtures, {len(datasets)} datasets).")
PY
