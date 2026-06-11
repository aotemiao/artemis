#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd python3

eval_id="${1:-memory-docs-governance}"

list_memory_eval_ids() {
  find docs/agent-evals/datasets -maxdepth 1 -type f -name '*.yml' -print \
    | sort \
    | while IFS= read -r path; do
      basename "$path" .yml
    done
}

check_harness_metrics_permission_posture() {
  local artifact_root="$1"
  local metrics_json="$2"

  python3 - "$artifact_root" "$metrics_json" <<'PY'
from __future__ import annotations

from collections import Counter
from pathlib import Path
import json
import sys

artifact_root = Path(sys.argv[1])
metrics_json = Path(sys.argv[2])


def read_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def stable_dimension(value: object, default: str = "unknown") -> str:
    text = str(value or "").strip().lower()
    return text or default


def approval_policy_dimension(value: object) -> str:
    if isinstance(value, str):
        return stable_dimension(value)
    if isinstance(value, dict):
        keys = sorted(stable_dimension(key, "") for key in value.keys())
        keys = [key for key in keys if key]
        return "+".join(keys) if keys else "object"
    return "unknown"


def count_list_dimension(value: object) -> str:
    if isinstance(value, list):
        return str(len(value))
    return "unknown"


run_paths = set(artifact_root.glob("agent-runs/*.json"))
run_paths.update(artifact_root.glob("**/agent-runs/*.json"))
agent_runs = [read_json(path) for path in sorted(run_paths)]

permission_approval_policy_counts: Counter[str] = Counter()
permission_thread_sandbox_counts: Counter[str] = Counter()
permission_turn_sandbox_type_counts: Counter[str] = Counter()
permission_writable_root_count_distribution: Counter[str] = Counter()
permission_allowed_writable_root_count_distribution: Counter[str] = Counter()
permission_summary_total = 0
permission_remote_worker_runs = 0
permission_network_access_runs = 0
permission_danger_full_access_allowed_runs = 0

for item in agent_runs:
    permissions = item.get("permissions") if isinstance(item.get("permissions"), dict) else {}
    if permissions:
        permission_summary_total += 1
        if permissions.get("remote_worker") is True:
            permission_remote_worker_runs += 1
        if permissions.get("network_access") is True:
            permission_network_access_runs += 1
        if permissions.get("danger_full_access_allowed") is True:
            permission_danger_full_access_allowed_runs += 1
    permission_approval_policy_counts[approval_policy_dimension(permissions.get("approval_policy"))] += 1
    permission_thread_sandbox_counts[stable_dimension(permissions.get("thread_sandbox"))] += 1
    turn_sandbox_policy = permissions.get("turn_sandbox_policy")
    if isinstance(turn_sandbox_policy, dict):
        permission_turn_sandbox_type_counts[stable_dimension(turn_sandbox_policy.get("type"))] += 1
    else:
        permission_turn_sandbox_type_counts[stable_dimension(turn_sandbox_policy)] += 1
    permission_writable_root_count_distribution[count_list_dimension(permissions.get("writable_roots"))] += 1
    permission_allowed_writable_root_count_distribution[
        count_list_dimension(permissions.get("allowed_writable_roots"))
    ] += 1

expected = {
    "summaries_with_permissions": permission_summary_total,
    "remote_worker_runs": permission_remote_worker_runs,
    "network_access_runs": permission_network_access_runs,
    "danger_full_access_allowed_runs": permission_danger_full_access_allowed_runs,
    "approval_policy_counts": dict(sorted(permission_approval_policy_counts.items())),
    "thread_sandbox_counts": dict(sorted(permission_thread_sandbox_counts.items())),
    "turn_sandbox_type_counts": dict(sorted(permission_turn_sandbox_type_counts.items())),
    "writable_root_count_distribution": dict(sorted(permission_writable_root_count_distribution.items())),
    "allowed_writable_root_count_distribution": dict(
        sorted(permission_allowed_writable_root_count_distribution.items())
    ),
}

metrics = read_json(metrics_json)
observed = metrics.get("agent_runs", {}).get("permission_posture")
if observed != expected:
    raise SystemExit(
        "harness metrics permission_posture did not match agent run summaries\n"
        + "expected: "
        + json.dumps(expected, ensure_ascii=False, sort_keys=True)
        + "\nobserved: "
        + json.dumps(observed, ensure_ascii=False, sort_keys=True)
    )

print(json.dumps(expected, indent=2, ensure_ascii=False, sort_keys=True))
PY
}

if [[ "$eval_id" == "all" || "$eval_id" == "suite" ]]; then
  shift || true
  dataset_ids=()
  if [[ "$#" -gt 0 ]]; then
    dataset_ids=("$@")
  else
    while IFS= read -r dataset_id; do
      dataset_ids+=("$dataset_id")
    done < <(list_memory_eval_ids)
  fi

  if [[ "${#dataset_ids[@]}" -eq 0 ]]; then
    echo "No executable Symphony memory eval datasets found." >&2
    exit 1
  fi

  for dataset_id in "${dataset_ids[@]}"; do
    require_repo_path_exact "docs/agent-evals/datasets/${dataset_id}.yml"
  done

  suite_run_id="symphony-agent-eval-suite-$(date +%Y%m%d%H%M%S)"
  suite_artifact_root="$(pwd)/artifacts/agent-evals/${suite_run_id}"
  suite_cases_dir="${suite_artifact_root}/cases"
  suite_logs_dir="${suite_artifact_root}/logs"
  suite_results="${suite_artifact_root}/results.jsonl"
  mkdir -p "$suite_cases_dir" "$suite_logs_dir" "${suite_artifact_root}/agent-runs"
  : >"$suite_results"

  print_step "Building Symphony start jar for eval suite"
  run_mvn -B -pl artemis-symphony/artemis-symphony-start -am clean package -DskipTests
  suite_jar="$(resolve_boot_jar artemis-symphony/artemis-symphony-start)"

  failures=0
  for dataset_id in "${dataset_ids[@]}"; do
    print_step "Running Symphony memory eval dataset: ${dataset_id}"
    log_path="${suite_logs_dir}/${dataset_id}.log"
    summary_path=""
    status="passed"
    if ! SYMPHONY_AGENT_EVAL_JAR="$suite_jar" \
      SYMPHONY_AGENT_EVAL_PARENT_DIR="$suite_cases_dir" \
      bash scripts/e2e/run-symphony-agent-eval.sh "$dataset_id" >"$log_path" 2>&1; then
      status="failed"
      failures=$((failures + 1))
      echo "Symphony memory eval dataset failed: ${dataset_id}" >&2
      tail -n 40 "$log_path" >&2 || true
    fi
    summary_path="$(awk -F': ' '/^Eval artifact:/ {print $2}' "$log_path" | tail -1)"
    python3 - "$suite_results" "$dataset_id" "$status" "$summary_path" "$log_path" <<'PY'
from pathlib import Path
import json
import sys

results_path = Path(sys.argv[1])
dataset_id = sys.argv[2]
status = sys.argv[3]
summary_path = sys.argv[4]
log_path = sys.argv[5]

record = {
    "eval_id": dataset_id,
    "status": status,
    "summary_file": summary_path,
    "log_file": log_path,
}
if summary_path:
    path = Path(summary_path)
    if path.exists():
        try:
            summary = json.loads(path.read_text(encoding="utf-8"))
            record["run_id"] = summary.get("run_id", "")
            record["history_status"] = summary.get("history_status", "")
            record["failure_reason"] = summary.get("failure_reason", "")
            record["total_tokens"] = summary.get("total_tokens", 0)
        except json.JSONDecodeError:
            record["failure_reason"] = "summary file was not valid JSON"
results_path.write_text(
    results_path.read_text(encoding="utf-8") + json.dumps(record, ensure_ascii=False) + "\n",
    encoding="utf-8",
)
PY
  done

  python3 - "$suite_artifact_root" "$suite_results" <<'PY'
from datetime import datetime, timezone
from pathlib import Path
import json
import sys

suite_root = Path(sys.argv[1])
results_path = Path(sys.argv[2])
results = [
    json.loads(line)
    for line in results_path.read_text(encoding="utf-8").splitlines()
    if line.strip()
]
passed = sum(1 for item in results if item.get("status") == "passed")
failed = len(results) - passed
summary = {
    "schema_version": 1,
    "summary_type": "symphony_memory_eval_suite",
    "status": "passed" if failed == 0 else "failed",
    "generated_at": datetime.now(timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z"),
    "total": len(results),
    "passed": passed,
    "failed": failed,
    "results": results,
    "harness_metrics": {
        "json": str(suite_root / "harness-metrics" / "latest.json"),
        "markdown": str(suite_root / "harness-metrics" / "latest.md"),
    },
}
(suite_root / "summary.json").write_text(json.dumps(summary, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
print(json.dumps(summary, indent=2, ensure_ascii=False))
PY

  print_step "Generating eval suite harness metrics snapshot"
  scripts/harness/generate-harness-metrics-report.sh \
    "$suite_artifact_root" \
    "${suite_artifact_root}/agent-runs" \
    "${suite_artifact_root}/harness-metrics"

  python3 - "$suite_artifact_root" <<'PY'
from pathlib import Path
import json
import sys

suite_root = Path(sys.argv[1])
summary_path = suite_root / "summary.json"
body = json.loads(summary_path.read_text(encoding="utf-8"))
body["harness_metrics"] = {
    "json": str(suite_root / "harness-metrics" / "latest.json"),
    "markdown": str(suite_root / "harness-metrics" / "latest.md"),
}
summary_path.write_text(json.dumps(body, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
print(json.dumps(body["harness_metrics"], indent=2, ensure_ascii=False))
PY

  print_step "Checking eval suite harness metrics permission posture"
  check_harness_metrics_permission_posture \
    "$suite_artifact_root" \
    "${suite_artifact_root}/harness-metrics/latest.json"

  print_step "Checking eval suite agent run summaries"
  scripts/harness/check-agent-run-summaries.sh "$suite_artifact_root"

  echo "Eval suite artifact: ${suite_artifact_root}/summary.json"
  if [[ "$failures" -gt 0 ]]; then
    exit 1
  fi
  exit 0
fi

dataset="docs/agent-evals/datasets/${eval_id}.yml"

require_repo_path_exact "$dataset"

run_id="symphony-agent-eval-${eval_id}-$(date +%Y%m%d%H%M%S)"
artifact_parent="${SYMPHONY_AGENT_EVAL_PARENT_DIR:-$(pwd)/artifacts/agent-evals}"
artifact_root="${artifact_parent}/${run_id}"
mkdir -p "$artifact_root"

cleanup() {
  if [[ -n "${symphony_pid:-}" ]] && kill -0 "$symphony_pid" >/dev/null 2>&1; then
    kill "$symphony_pid" >/dev/null 2>&1 || true
    wait "$symphony_pid" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

print_step "Preparing Symphony memory agent eval: ${eval_id}"

port="$(python3 - <<'PY'
import socket

with socket.socket() as sock:
    sock.bind(("127.0.0.1", 0))
    print(sock.getsockname()[1])
PY
)"

eval_meta="$(
  python3 - "$dataset" "$artifact_root" "$port" <<'PY'
from pathlib import Path
import json
import sys

dataset = Path(sys.argv[1])
artifact_root = Path(sys.argv[2])
port = sys.argv[3]

data: dict[str, str | list[str]] = {}
current_key: str | None = None
for raw_line in dataset.read_text(encoding="utf-8").splitlines():
    if not raw_line.strip() or raw_line.lstrip().startswith("#"):
        continue
    if raw_line and not raw_line.startswith(" ") and ":" in raw_line:
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
        if isinstance(existing, list):
            existing.append(stripped[2:].strip())

def bool_value(value, default: bool) -> bool:
    if value is None:
        return default
    text = str(value).strip().lower()
    if text in {"1", "true", "yes", "on"}:
        return True
    if text in {"0", "false", "no", "off"}:
        return False
    return default

def int_value(value, default: int) -> int:
    if value is None:
        return default
    text = str(value).strip()
    if not text:
        return default
    try:
        return int(text)
    except ValueError as exc:
        raise SystemExit(f"invalid integer value: {text}") from exc

def sanitize_workspace_key(value: str) -> str:
    if not value:
        return "unknown"
    return "".join(ch if ch.isalnum() or ch in "._-" else "_" for ch in value)

def expected_codex_event_names(value) -> list[str]:
    if not isinstance(value, list):
        raise SystemExit("expected_events must be a list when configured")
    names: list[str] = []
    for raw_event in value:
        event = str(raw_event or "").strip()
        if not event:
            continue
        if event.startswith("run_") or event == "retry_scheduled":
            continue
        names.append(event)
    return names

def configured_writable_root(value: str, artifact_root: Path) -> Path | None:
    marker = str(value or "").strip()
    if not marker:
        return None
    if marker == "artifact":
        return artifact_root / "outside-writable-root"
    return Path(marker)

workspace_root = artifact_root / "workspaces"
summary_dir = artifact_root / "agent-runs"
history_path = artifact_root / "symphony_runs.sqlite"
fake_codex = artifact_root / "fake-codex-app-server.py"
fake_codex_started = artifact_root / "fake-codex-started"
workflow = artifact_root / "WORKFLOW.md"
result_file = str(data["expected_workspace_file"])
expected_content = str(data["expected_workspace_contains"])
issue_identifier = str(data["issue_identifier"])
issue_id = str(data["issue_id"])
issue_title = str(data["issue_title"])
issue_state = str(data.get("issue_state", "Todo"))
issue_description = str(data.get("issue_description", ""))
expected_workspace_present = bool_value(data.get("expected_workspace_present"), True)
expected_workspace_directory_present = data.get("expected_workspace_directory_present")
expected_workspace_directory_present = (
    bool_value(expected_workspace_directory_present, False)
    if expected_workspace_directory_present is not None
    else None
)
expected_codex_started = bool_value(data.get("expected_codex_started"), True)
expected_failure_reason_contains = str(data.get("expected_failure_reason_contains", ""))
expected_failure_category = str(data.get("expected_failure_category", "")).strip()
expected_permission_network_access = data.get("expected_permission_network_access")
expected_permission_network_access = (
    bool_value(expected_permission_network_access, False)
    if expected_permission_network_access is not None
    else None
)
expected_permission_network_access_reason = str(data.get("expected_permission_network_access_reason", "")).strip()
expected_permission_writable_root_contains = str(data.get("expected_permission_writable_root_contains", "")).strip()
expected_permission_allowed_writable_root_contains = str(
    data.get("expected_permission_allowed_writable_root_contains", "")
).strip()
expected_permission_danger_full_access_allowed = data.get("expected_permission_danger_full_access_allowed")
expected_permission_danger_full_access_allowed = (
    bool_value(expected_permission_danger_full_access_allowed, False)
    if expected_permission_danger_full_access_allowed is not None
    else None
)
expected_permission_thread_sandbox = str(data.get("expected_permission_thread_sandbox", "")).strip()
expected_min_total_tokens = int_value(data.get("expected_min_total_tokens"), 0)
expected_metrics_total_runs = int_value(data.get("expected_metrics_total_runs"), 1)
expected_agent_run_count = int_value(data.get("expected_agent_run_count"), 1)
if expected_agent_run_count <= 0:
    raise SystemExit("expected_agent_run_count must be greater than 0")
expected_summary_dispatch_kind = str(data.get("expected_summary_dispatch_kind", "")).strip()
expected_summary_dispatch_kinds = data.get("expected_summary_dispatch_kinds", [])
if expected_summary_dispatch_kinds is None:
    expected_summary_dispatch_kinds = []
if not isinstance(expected_summary_dispatch_kinds, list):
    raise SystemExit("expected_summary_dispatch_kinds must be a list when configured")
expected_external_effects = data.get("expected_external_effects", [])
if expected_external_effects is None:
    expected_external_effects = []
if not isinstance(expected_external_effects, list):
    raise SystemExit("expected_external_effects must be a list when configured")
expected_external_effect_details = data.get("expected_external_effect_details", [])
if expected_external_effect_details is None:
    expected_external_effect_details = []
if not isinstance(expected_external_effect_details, list):
    raise SystemExit("expected_external_effect_details must be a list when configured")
expected_summary_workspace_files = data.get("expected_summary_workspace_files", [])
if expected_summary_workspace_files is None:
    expected_summary_workspace_files = []
if not isinstance(expected_summary_workspace_files, list):
    raise SystemExit("expected_summary_workspace_files must be a list when configured")
if expected_workspace_present and result_file not in expected_summary_workspace_files:
    expected_summary_workspace_files = [*expected_summary_workspace_files, result_file]
linear_comment_reporting_enabled = bool_value(data.get("linear_comment_reporting_enabled"), False)
linear_comment_success_template = str(data.get("linear_comment_success_template", "")).strip()
linear_comment_failure_template = str(data.get("linear_comment_failure_template", "")).strip()
linear_comment_issue_title_regex = str(data.get("linear_comment_issue_title_regex", "")).strip()
spec_driven_delivery_enabled = bool_value(data.get("spec_driven_delivery_enabled"), False)
adversarial_review_enabled = bool_value(data.get("adversarial_review_enabled"), False)
adversarial_review_issue_title_regex = str(data.get("adversarial_review_issue_title_regex", "")).strip()
prompt_must_contain = data.get("prompt_must_contain", [])
if prompt_must_contain is None:
    prompt_must_contain = []
if not isinstance(prompt_must_contain, list):
    raise SystemExit("prompt_must_contain must be a list when configured")
codex_turn_outcome = str(data.get("codex_turn_outcome", "completed")).strip().lower() or "completed"
if codex_turn_outcome not in {"completed", "completed_with_malformed", "failed", "cancelled", "approval_required", "dynamic_tool_failure", "user_input_required", "timeout", "response_timeout"}:
    raise SystemExit(f"unsupported codex_turn_outcome: {codex_turn_outcome}")
codex_startup_outcome = str(data.get("codex_startup_outcome", "started")).strip().lower() or "started"
if codex_startup_outcome not in {"started", "initialize_error"}:
    raise SystemExit(f"unsupported codex_startup_outcome: {codex_startup_outcome}")
hook_before_run_outcome = str(data.get("hook_before_run_outcome", "")).strip().lower()
if hook_before_run_outcome not in {"", "failed"}:
    raise SystemExit(f"unsupported hook_before_run_outcome: {hook_before_run_outcome}")
hook_after_create_outcome = str(data.get("hook_after_create_outcome", "")).strip().lower()
if hook_after_create_outcome not in {"", "failed"}:
    raise SystemExit(f"unsupported hook_after_create_outcome: {hook_after_create_outcome}")
agent_max_retry_backoff_ms = int_value(data.get("agent_max_retry_backoff_ms"), 1000)
if agent_max_retry_backoff_ms <= 0:
    raise SystemExit("agent_max_retry_backoff_ms must be greater than 0")
codex_turn_timeout_ms = int_value(data.get("codex_turn_timeout_ms"), 10000)
if codex_turn_timeout_ms <= 0:
    raise SystemExit("codex_turn_timeout_ms must be greater than 0")
codex_read_timeout_ms = int_value(data.get("codex_read_timeout_ms"), 3000)
if codex_read_timeout_ms <= 0:
    raise SystemExit("codex_read_timeout_ms must be greater than 0")

codex_turn_policy = ""
if "codex_turn_sandbox_network_access" in data or "codex_turn_sandbox_extra_writable_root" in data:
    network_access = "false"
    if "codex_turn_sandbox_network_access" in data:
        network_access = "true" if bool_value(data.get("codex_turn_sandbox_network_access"), False) else "false"
    workspace_issue_path = workspace_root / sanitize_workspace_key(issue_identifier)
    writable_roots = [workspace_issue_path]
    extra_writable_root = configured_writable_root(
        str(data.get("codex_turn_sandbox_extra_writable_root", "")).strip(),
        artifact_root,
    )
    if extra_writable_root:
        writable_roots.append(extra_writable_root)
    writable_roots_yaml = "\n".join(f"      - {root}" for root in writable_roots)
    codex_turn_policy = f'''
  turn_sandbox_policy:
    type: workspaceWrite
    writableRoots:
{writable_roots_yaml}
    readOnlyAccess:
      type: fullAccess
    networkAccess: {network_access}
    excludeTmpdirEnvVar: false
    excludeSlashTmp: false'''

permissions_lines: list[str] = []
network_reason = str(data.get("permissions_network_access_reason", "")).strip()
if network_reason:
    permissions_lines.append(f"  network_access_reason: {network_reason}")
allowed_writable_root = configured_writable_root(
    str(data.get("permissions_allowed_writable_root", "")).strip(),
    artifact_root,
)
if allowed_writable_root:
    permissions_lines.extend([
        "  allowed_writable_roots:",
        f"    - {allowed_writable_root}",
    ])
if "permissions_allow_danger_full_access" in data:
    danger_allowed = bool_value(data.get("permissions_allow_danger_full_access"), False)
    permissions_lines.append(f"  allow_danger_full_access: {'true' if danger_allowed else 'false'}")
permissions_block = ""
if permissions_lines:
    permissions_block = "permissions:\n" + "\n".join(permissions_lines) + "\n"

codex_approval_policy = str(data.get("codex_approval_policy", "")).strip()
codex_approval_policy_block = ""
if codex_approval_policy:
    codex_approval_policy_block = f'''
  approval_policy: {codex_approval_policy}'''
codex_thread_sandbox = str(data.get("codex_thread_sandbox", "")).strip()
codex_thread_sandbox_block = ""
if codex_thread_sandbox:
    codex_thread_sandbox_block = f'''
  thread_sandbox: {codex_thread_sandbox}'''

hooks_block = ""
hook_entries: list[str] = []
if hook_after_create_outcome == "failed":
    hook_entries.append('''  after_create: |
    echo synthetic after_create failure >&2
    exit 24''')
if hook_before_run_outcome == "failed":
    hook_entries.append('''  before_run: |
    echo synthetic before_run failure >&2
    exit 23''')
if hook_entries:
    hooks_block = "hooks:\n" + "\n".join(hook_entries) + "\n"

def yaml_single_quoted(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"

def append_yaml_literal(lines: list[str], key: str, value: str) -> None:
    lines.append(f"    {key}: |")
    value_lines = value.splitlines() or [""]
    lines.extend(f"      {line}" if line else "      " for line in value_lines)

delivery_block = ""
delivery_lines: list[str] = []
if spec_driven_delivery_enabled:
    delivery_lines.extend([
        "  spec_driven:",
        "    enabled: true",
        "    required_assets:",
        "      - docs/feature-specs/README.md",
        "      - docs/feature-specs/templates/feature-spec-template.md",
        "      - docs/patterns/spec-to-validation-mapping.md",
        "      - docs/patterns/agent-delivery-handoff.md",
        "      - docs/security/THREAT_MODEL.md",
        "      - docs/patterns/security-review-checklist.md",
        "      - docs/runbooks/AGENT_PERMISSION_RUNBOOK.md",
        "      - docs/exec-plans/templates/execution-plan-template.md",
        "      - artemis-symphony/prompts/spec-driven-delivery.md",
        "      - artemis-symphony/skills/spec-driven-delivery.md",
        "      - artemis-symphony/prompts/adversarial-review.md",
        "      - artemis-symphony/skills/adversarial-review.md",
        "      - artemis-symphony/tools/registry.json",
        "      - scripts/harness/check-feature-specs.sh",
        "      - scripts/harness/check-spec-driven-delivery-chain.sh",
    ])
if adversarial_review_enabled:
    delivery_lines.extend([
        "  adversarial_review:",
        "    enabled: true",
    ])
    if adversarial_review_issue_title_regex:
        delivery_lines.append(
            f"    issue_title_regex: {yaml_single_quoted(adversarial_review_issue_title_regex)}"
        )
if delivery_lines:
    delivery_block = "delivery:\n" + "\n".join(delivery_lines) + "\n"

linear_comments_block = ""
if linear_comment_reporting_enabled:
    if not linear_comment_success_template:
        linear_comment_success_template = "Symphony eval {{ issue.identifier }} {{ attempt.outcome }}"
    linear_comment_lines = [
        "  linear_comments:",
        "    enabled: true",
    ]
    append_yaml_literal(linear_comment_lines, "success_template", linear_comment_success_template)
    if linear_comment_failure_template:
        append_yaml_literal(linear_comment_lines, "failure_template", linear_comment_failure_template)
    if linear_comment_issue_title_regex:
        linear_comment_lines.append(
            f"    issue_title_regex: {yaml_single_quoted(linear_comment_issue_title_regex)}"
        )
    linear_comments_block = "\n".join(linear_comment_lines) + "\n"

fake_codex.write_text(f'''#!/usr/bin/env python3
import json
import os
import sys
import time

open({str(fake_codex_started)!r}, "w", encoding="utf-8").write("started\\n")

thread_id = "eval-thread"
turn_id = "eval-turn"
turn_outcome = {codex_turn_outcome!r}
startup_outcome = {codex_startup_outcome!r}
prompt_must_contain = {prompt_must_contain!r}

def send(payload):
    print(json.dumps(payload, separators=(",", ":")), flush=True)

for raw in sys.stdin:
    message = json.loads(raw)
    method = message.get("method")
    if method == "initialize":
        if startup_outcome == "initialize_error":
            send({{"id": message.get("id"), "error": {{"code": -32600, "message": "synthetic initialize failure"}}}})
            break
        send({{"id": message.get("id"), "result": {{"userAgent": "fake-symphony-agent-eval"}}}})
    elif method == "initialized":
        continue
    elif method == "thread/start":
        send({{"id": message.get("id"), "result": {{"thread": {{"id": thread_id}}}}}})
    elif method == "turn/start":
        params = message.get("params", {{}})
        cwd = params.get("cwd") or os.getcwd()
        os.makedirs(cwd, exist_ok=True)
        if turn_outcome == "response_timeout":
            while True:
                time.sleep(3600)
        input_items = params.get("input", [])
        prompt_text = "\\n".join(
            item.get("text", "")
            for item in input_items
            if isinstance(item, dict)
        )
        missing_prompt_markers = [
            marker for marker in prompt_must_contain if marker not in prompt_text
        ]
        send({{"id": message.get("id"), "result": {{"turn": {{"id": turn_id}}}}}})
        if missing_prompt_markers:
            send({{"method": "turn/failed", "params": {{"threadId": thread_id, "turn": {{"id": turn_id, "status": "failed"}}, "error": {{"message": "missing prompt markers: " + ", ".join(missing_prompt_markers)}}}}}})
            break
        send({{"method": "thread/tokenUsage/updated", "params": {{"threadId": thread_id, "turnId": turn_id, "tokenUsage": {{"total": {{"inputTokens": 7, "outputTokens": 11, "totalTokens": 18}}}}}}}})
        if turn_outcome in {{"completed", "completed_with_malformed"}}:
            if turn_outcome == "completed_with_malformed":
                print("{{not valid json", flush=True)
            result_path = os.path.join(cwd, {result_file!r})
            result_parent = os.path.dirname(result_path)
            if result_parent:
                os.makedirs(result_parent, exist_ok=True)
            with open(result_path, "w", encoding="utf-8") as out:
                out.write({expected_content!r} + "\\n")
            send({{"method": "turn/completed", "params": {{"threadId": thread_id, "turn": {{"id": turn_id, "status": "completed"}}}}}})
        elif turn_outcome == "failed":
            send({{"method": "turn/failed", "params": {{"threadId": thread_id, "turn": {{"id": turn_id, "status": "failed"}}, "error": {{"message": "synthetic eval turn failure"}}}}}})
        elif turn_outcome == "cancelled":
            send({{"method": "turn/cancelled", "params": {{"threadId": thread_id, "turn": {{"id": turn_id, "status": "cancelled"}}}}}})
        elif turn_outcome == "approval_required":
            send({{"id": "approval-1", "method": "item/commandExecution/requestApproval", "params": {{"command": "touch APPROVAL_REQUIRED_SHOULD_NOT_RUN"}}}})
        elif turn_outcome == "dynamic_tool_failure":
            send({{"id": 501, "method": "item/tool/call", "params": {{"name": "unsupported_memory_eval_tool", "arguments": {{"reason": "synthetic dynamic tool failure"}}}}}})
            for tool_raw in sys.stdin:
                tool_message = json.loads(tool_raw)
                if tool_message.get("id") == 501:
                    break
        elif turn_outcome == "user_input_required":
            send({{"id": 601, "method": "item/tool/requestUserInput", "params": {{"questions": []}}}})
        else:
            while True:
                time.sleep(3600)
        break
sys.stdout.flush()
''', encoding="utf-8")
fake_codex.chmod(0o755)

workflow.write_text(f'''---
tracker:
  kind: memory
  active_states:
    - Todo
    - In Progress
  memory_issues:
    - id: {issue_id}
      identifier: {issue_identifier}
      title: {issue_title}
      state: {issue_state}
      description: |
{chr(10).join("        " + line for line in issue_description.splitlines())}
polling:
  interval_ms: 60000
workspace:
  root: {workspace_root}
{delivery_block}
{hooks_block}reporting:
{linear_comments_block}  agent_runs:
    enabled: true
    directory: {summary_dir}
agent:
  max_concurrent_agents: 1
  max_turns: 1
  max_retry_backoff_ms: {agent_max_retry_backoff_ms}
codex:
  command: python3 {fake_codex}
{codex_approval_policy_block}
{codex_thread_sandbox_block}
  turn_timeout_ms: {codex_turn_timeout_ms}
  read_timeout_ms: {codex_read_timeout_ms}
  stall_timeout_ms: 30000
{codex_turn_policy}
{permissions_block}
---

You are working on an Artemis repository issue inside a deterministic Symphony memory eval.

Before changing code, read these minimal repository-local sources of truth:

1. `AGENTS.md`
2. `ARCHITECTURE.md`
3. `docs/agent-workflow/AGENT_DEVELOPMENT_WORKFLOW.md`

Then route by task type and read only the relevant additional assets:

- Vague request: `artemis-symphony/prompts/agent-requirement-intake.md`
- Business feature: `docs/feature-specs/README.md`, `docs/patterns/spec-to-validation-mapping.md`
- Complex implementation: `docs/exec-plans/templates/execution-plan-template.md`
- Stable rule change: create or update an OpenSpec change under `openspec/changes/`.
- Stable rule source: relevant files under `openspec/specs/`
- Security-sensitive work: `docs/security/THREAT_MODEL.md`, `docs/patterns/security-review-checklist.md`, `docs/runbooks/AGENT_PERMISSION_RUNBOOK.md`

Execution rules:

- Treat repository-local docs and specs as the primary source of truth.
- If the request is still vague after reading the sources above, use `artemis-symphony/prompts/agent-requirement-intake.md` to normalize the需求 before implementation.
- If the request has business rules, data model impact, API impact, internal RPC impact, permissions, or cross-module behavior, use `artemis-symphony/prompts/spec-driven-delivery.md` and create or update a Feature Spec under `docs/feature-specs/active/` before implementation.
- For complex implementation work, create or update an execution plan under `docs/exec-plans/active/`.
- If a task changes stable behavior rules, constraints, contracts, quality gates, or repository workflow, create or update an OpenSpec change under `openspec/changes/` and read the relevant stable specs under `openspec/specs/`.
- Map every key acceptance criterion to a test, smoke, harness script, or explicit manual validation item before final handoff.
- For this deterministic eval, write only the expected low-sensitivity workspace artifact requested by the dataset.

Complete the task described in the issue.

**Issue:** {{{{ issue.identifier }}}} - {{{{ issue.title }}}}

{{% if issue.description %}}
**Description:**
{{{{ issue.description }}}}
{{% endif %}}
''', encoding="utf-8")

print(json.dumps({
    "eval_id": str(data["id"]),
    "workflow": str(workflow),
    "workspace_root": str(workspace_root),
    "summary_dir": str(summary_dir),
    "history_path": str(history_path),
    "fake_codex_started": str(fake_codex_started),
    "issue_identifier": issue_identifier,
    "expected_workspace_file": result_file,
    "expected_workspace_contains": expected_content,
    "expected_workspace_present": expected_workspace_present,
    "expected_workspace_directory_present": expected_workspace_directory_present,
    "expected_codex_started": expected_codex_started,
    "expected_summary_status": data["expected_summary_status"],
    "expected_history_status": data["expected_history_status"],
    "expected_events": data["expected_events"],
    "expected_codex_events": expected_codex_event_names(data["expected_events"]),
    "forbidden_events": data.get("forbidden_events", []),
    "expected_failure_reason_contains": expected_failure_reason_contains,
    "expected_failure_category": expected_failure_category,
    "expected_permission_network_access": expected_permission_network_access,
    "expected_permission_network_access_reason": expected_permission_network_access_reason,
    "expected_permission_writable_root_contains": expected_permission_writable_root_contains,
    "expected_permission_allowed_writable_root_contains": expected_permission_allowed_writable_root_contains,
    "expected_permission_danger_full_access_allowed": expected_permission_danger_full_access_allowed,
    "expected_permission_thread_sandbox": expected_permission_thread_sandbox,
    "expected_min_total_tokens": expected_min_total_tokens,
    "expected_metrics_total_runs": expected_metrics_total_runs,
    "expected_agent_run_count": expected_agent_run_count,
    "expected_summary_dispatch_kind": expected_summary_dispatch_kind,
    "expected_summary_dispatch_kinds": expected_summary_dispatch_kinds,
    "expected_external_effects": expected_external_effects,
    "expected_external_effect_details": expected_external_effect_details,
    "expected_summary_workspace_files": expected_summary_workspace_files,
    "linear_comment_reporting_enabled": linear_comment_reporting_enabled,
    "prompt_must_contain": prompt_must_contain,
    "agent_max_retry_backoff_ms": agent_max_retry_backoff_ms,
    "port": port,
}))
PY
)"

workflow_path="$(python3 -c 'import json,sys; print(json.loads(sys.stdin.read())["workflow"])' <<< "$eval_meta")"
history_path="$(python3 -c 'import json,sys; print(json.loads(sys.stdin.read())["history_path"])' <<< "$eval_meta")"

if [[ -n "${SYMPHONY_AGENT_EVAL_JAR:-}" ]]; then
  jar="$SYMPHONY_AGENT_EVAL_JAR"
  if [[ ! -f "$jar" ]]; then
    echo "Configured Symphony eval jar does not exist: $jar" >&2
    exit 1
  fi
else
  print_step "Building Symphony start jar"
  run_mvn -B -pl artemis-symphony/artemis-symphony-start -am clean package -DskipTests
  jar="$(resolve_boot_jar artemis-symphony/artemis-symphony-start)"
fi

print_step "Starting Symphony eval server on port ${port}"
java -jar "$jar" \
  --server.address=127.0.0.1 \
  --server.port="$port" \
  --symphony.workflow-path="$workflow_path" \
  --symphony.history.sqlite-path="$history_path" \
  >"${artifact_root}/symphony.log" 2>&1 &
symphony_pid="$!"

python3 - "$eval_meta" "$artifact_root" <<'PY'
from pathlib import Path
import json
import sys
import time
import urllib.error
import urllib.request

meta = json.loads(sys.argv[1])
artifact_root = Path(sys.argv[2])
base = f"http://127.0.0.1:{meta['port']}"
summary_path = artifact_root / "summary.json"

def request(method: str, path: str):
    req = urllib.request.Request(base + path, method=method)
    with urllib.request.urlopen(req, timeout=2) as response:
        body = response.read().decode("utf-8")
    return json.loads(body)

deadline = time.monotonic() + 30
while True:
    try:
        request("GET", "/api/v1/state")
        break
    except Exception:
        if time.monotonic() > deadline:
            raise SystemExit("Symphony eval server did not become ready")
        time.sleep(0.25)

request("POST", "/api/v1/refresh")

issue_identifier = meta["issue_identifier"]
workspace_dir = Path(meta["workspace_root"]) / issue_identifier
workspace_file = workspace_dir / meta["expected_workspace_file"]
expected_content = meta["expected_workspace_contains"]
summary_dir = Path(meta["summary_dir"])
expected_events = set(meta["expected_events"])
expected_codex_events = set(meta.get("expected_codex_events", []))
forbidden_events = set(meta.get("forbidden_events", []))
fake_codex_started = Path(meta["fake_codex_started"])

def read_json_if_complete(path: Path):
    try:
        text = path.read_text(encoding="utf-8")
        if not text.strip():
            return None
        return json.loads(text)
    except json.JSONDecodeError:
        return None

deadline = time.monotonic() + 30
last_error = ""
while time.monotonic() < deadline:
    try:
        summaries = sorted(summary_dir.glob("*.json"))
        if not summaries:
            last_error = f"missing agent run summary under {summary_dir}"
            time.sleep(0.25)
            continue
        matched_summary = None
        matched_summary_path = None
        matching_summaries = []
        observed = []
        observed_dispatch_kinds = []
        observed_dispatch_statuses = set()
        for candidate in summaries:
            body = read_json_if_complete(candidate)
            if body is None:
                last_error = f"summary is not fully written yet: {candidate}"
                continue
            issue = body.get("issue", {})
            if issue.get("identifier") != issue_identifier:
                continue
            dispatch_kind = body.get("attempt", {}).get("dispatch_kind", "")
            matching_summaries.append((candidate, body))
            observed.append(f"{body.get('status')}/{dispatch_kind}")
            if dispatch_kind:
                observed_dispatch_kinds.append(dispatch_kind)
                observed_dispatch_statuses.add(f"{dispatch_kind}:{body.get('status')}")
        expected_agent_run_count = int(meta.get("expected_agent_run_count", 1) or 1)
        if len(matching_summaries) < expected_agent_run_count:
            last_error = (
                "missing expected agent run summaries: "
                f"{len(matching_summaries)} < {expected_agent_run_count}; observed {observed}"
            )
            time.sleep(0.25)
            continue
        expected_dispatch_kinds = set(meta.get("expected_summary_dispatch_kinds", []))
        missing_dispatch_kinds = sorted(expected_dispatch_kinds - set(observed_dispatch_kinds))
        if missing_dispatch_kinds:
            last_error = f"summary missing dispatch kinds: {missing_dispatch_kinds}; observed {observed_dispatch_kinds}"
            time.sleep(0.25)
            continue
        missing_dispatch_statuses = sorted(
            kind
            for kind in expected_dispatch_kinds
            if f"{kind}:{meta['expected_summary_status']}" not in observed_dispatch_statuses
        )
        if missing_dispatch_statuses:
            last_error = (
                "summary dispatch kinds did not reach expected status "
                f"{meta['expected_summary_status']}: {missing_dispatch_statuses}"
            )
            time.sleep(0.25)
            continue
        expected_summary_dispatch_kind = meta.get("expected_summary_dispatch_kind", "")
        for candidate, body in matching_summaries:
            dispatch_kind = body.get("attempt", {}).get("dispatch_kind", "")
            if body.get("status") != meta["expected_summary_status"]:
                continue
            if expected_summary_dispatch_kind and dispatch_kind != expected_summary_dispatch_kind:
                continue
            if body.get("issue", {}).get("identifier") == issue_identifier:
                matched_summary = body
                matched_summary_path = candidate
                break
        if matched_summary is None:
            expected_text = meta["expected_summary_status"]
            if expected_summary_dispatch_kind:
                expected_text += f"/{expected_summary_dispatch_kind}"
            last_error = f"missing expected summary {expected_text}; observed {observed}"
            time.sleep(0.25)
            continue
        expected_workspace_dir = meta.get("expected_workspace_directory_present")
        if expected_workspace_dir is not None and workspace_dir.is_dir() != expected_workspace_dir:
            last_error = f"unexpected workspace directory presence for this eval: {workspace_dir}"
            time.sleep(0.25)
            continue
        if meta["expected_workspace_present"]:
            if not workspace_file.exists():
                last_error = f"missing workspace file: {workspace_file}"
                time.sleep(0.25)
                continue
            if expected_content not in workspace_file.read_text(encoding="utf-8"):
                last_error = f"workspace file did not contain expected marker: {workspace_file}"
                time.sleep(0.25)
                continue
        elif workspace_file.exists():
            last_error = f"workspace file should not exist for this eval: {workspace_file}"
            time.sleep(0.25)
            continue
        expected_summary_workspace_files = set(meta.get("expected_summary_workspace_files", []))
        if expected_summary_workspace_files:
            artifact_inventory = matched_summary.get("workspace", {}).get("artifact_inventory", {})
            if not isinstance(artifact_inventory, dict):
                last_error = "summary workspace.artifact_inventory was not an object"
                time.sleep(0.25)
                continue
            artifact_files = artifact_inventory.get("files", [])
            if not isinstance(artifact_files, list):
                last_error = "summary workspace.artifact_inventory.files was not a list"
                time.sleep(0.25)
                continue
            observed_summary_workspace_files = {
                str(item.get("path", ""))
                for item in artifact_files
                if isinstance(item, dict) and item.get("path")
            }
            missing_summary_workspace_files = sorted(
                expected_summary_workspace_files - observed_summary_workspace_files
            )
            if missing_summary_workspace_files:
                last_error = (
                    "summary workspace artifact inventory missing files: "
                    f"{missing_summary_workspace_files}; scan_error={artifact_inventory.get('scan_error', '')}"
                )
                time.sleep(0.25)
                continue
        if (
            not meta["expected_codex_started"]
            and meta.get("expected_workspace_directory_present") is True
            and int(meta.get("agent_max_retry_backoff_ms", 0) or 0) > 0
            and time.monotonic() + 0.5 < deadline
        ):
            time.sleep((int(meta["agent_max_retry_backoff_ms"]) / 1000.0) + 0.5)
            if workspace_file.exists():
                last_error = f"workspace file should not exist after retry window: {workspace_file}"
                continue
            if fake_codex_started.exists():
                last_error = "fake Codex app-server should not have been started after retry window"
                continue
        if meta["expected_codex_started"] and not fake_codex_started.exists():
            last_error = "fake Codex app-server was not started"
            time.sleep(0.25)
            continue
        if not meta["expected_codex_started"] and fake_codex_started.exists():
            last_error = "fake Codex app-server should not have been started"
            time.sleep(0.25)
            continue
        failure_reason = matched_summary.get("failure_reason", "")
        expected_failure = meta.get("expected_failure_reason_contains", "")
        if expected_failure and expected_failure not in failure_reason:
            last_error = f"summary failure reason did not contain expected text: {expected_failure}"
            time.sleep(0.25)
            continue
        expected_failure_category = meta.get("expected_failure_category", "")
        if expected_failure_category:
            observed_failure_category = matched_summary.get("failure_category", "")
            if observed_failure_category != expected_failure_category:
                last_error = f"summary failure category did not match expected text: {observed_failure_category}"
                time.sleep(0.25)
                continue
        expected_network = meta.get("expected_permission_network_access")
        if expected_network is not None:
            observed_network = matched_summary.get("permissions", {}).get("network_access")
            if observed_network != expected_network:
                last_error = f"unexpected permission network_access: {observed_network}"
                time.sleep(0.25)
                continue
        expected_thread_sandbox = meta.get("expected_permission_thread_sandbox", "")
        if expected_thread_sandbox:
            observed_thread_sandbox = matched_summary.get("permissions", {}).get("thread_sandbox")
            if observed_thread_sandbox != expected_thread_sandbox:
                last_error = f"unexpected permission thread_sandbox: {observed_thread_sandbox}"
                time.sleep(0.25)
                continue
        expected_network_reason = meta.get("expected_permission_network_access_reason", "")
        if expected_network_reason:
            observed_network_reason = matched_summary.get("permissions", {}).get("network_access_reason")
            if observed_network_reason != expected_network_reason:
                last_error = f"unexpected permission network_access_reason: {observed_network_reason}"
                time.sleep(0.25)
                continue
        expected_writable_root = meta.get("expected_permission_writable_root_contains", "")
        if expected_writable_root:
            writable_roots = matched_summary.get("permissions", {}).get("writable_roots", [])
            if not any(expected_writable_root in str(root) for root in writable_roots):
                last_error = f"summary writable_roots did not contain expected text: {expected_writable_root}"
                time.sleep(0.25)
                continue
        expected_allowed_writable_root = meta.get("expected_permission_allowed_writable_root_contains", "")
        if expected_allowed_writable_root:
            allowed_writable_roots = matched_summary.get("permissions", {}).get("allowed_writable_roots", [])
            if not any(expected_allowed_writable_root in str(root) for root in allowed_writable_roots):
                last_error = (
                    "summary allowed_writable_roots did not contain expected text: "
                    f"{expected_allowed_writable_root}"
                )
                time.sleep(0.25)
                continue
        expected_danger_full_access_allowed = meta.get("expected_permission_danger_full_access_allowed")
        if expected_danger_full_access_allowed is not None:
            observed_danger_full_access_allowed = matched_summary.get("permissions", {}).get(
                "danger_full_access_allowed"
            )
            if observed_danger_full_access_allowed != expected_danger_full_access_allowed:
                last_error = (
                    "unexpected permission danger_full_access_allowed: "
                    f"{observed_danger_full_access_allowed}"
                )
                time.sleep(0.25)
                continue
        expected_min_total_tokens = int(meta.get("expected_min_total_tokens", 0) or 0)
        if expected_min_total_tokens > 0:
            summary_total_tokens = matched_summary.get("codex", {}).get("usage", {}).get("total_tokens", 0) or 0
            if summary_total_tokens < expected_min_total_tokens:
                last_error = f"summary total_tokens below expected minimum: {summary_total_tokens}"
                time.sleep(0.25)
                continue
        codex_event_counts = matched_summary.get("codex", {}).get("event_counts", {})
        if not isinstance(codex_event_counts, dict):
            last_error = "summary codex.event_counts was not an object"
            time.sleep(0.25)
            continue
        invalid_codex_event_counts = [
            key
            for key, value in codex_event_counts.items()
            if not isinstance(key, str) or not isinstance(value, int) or isinstance(value, bool) or value < 0
        ]
        if invalid_codex_event_counts:
            last_error = f"summary codex.event_counts contained invalid counts: {invalid_codex_event_counts}"
            time.sleep(0.25)
            continue
        missing_codex_events = sorted(
            event for event in expected_codex_events if int(codex_event_counts.get(event, 0) or 0) < 1
        )
        if missing_codex_events:
            last_error = f"summary codex.event_counts missing expected Codex events: {missing_codex_events}"
            time.sleep(0.25)
            continue
        expected_external_effects = set(meta.get("expected_external_effects", []))
        external_effect_events = matched_summary.get("external_effects", {}).get("events", [])
        if not isinstance(external_effect_events, list):
            last_error = "summary external_effects.events was not a list"
            time.sleep(0.25)
            continue
        observed_external_effects = {
            f"{event.get('type', '')}:{event.get('status', '')}"
            for event in external_effect_events
            if isinstance(event, dict)
        }
        observed_external_effect_details = {
            f"{event.get('type', '')}:{event.get('status', '')}:{event.get('error_code', '')}"
            for event in external_effect_events
            if isinstance(event, dict)
        }
        missing_external_effects = sorted(expected_external_effects - observed_external_effects)
        if missing_external_effects:
            last_error = f"summary missing external effect events: {missing_external_effects}"
            time.sleep(0.25)
            continue
        expected_external_effect_details = set(meta.get("expected_external_effect_details", []))
        missing_external_effect_details = sorted(
            expected_external_effect_details - observed_external_effect_details
        )
        if missing_external_effect_details:
            last_error = f"summary missing external effect detail events: {missing_external_effect_details}"
            time.sleep(0.25)
            continue
        runs_body = request("GET", "/api/v1/history/runs?limit=10")
        runs = runs_body.get("runs", [])
        run = next((item for item in runs if item.get("run_id") == matched_summary.get("run_id")), None)
        if not run:
            last_error = "history API did not return eval run"
            time.sleep(0.25)
            continue
        if run.get("status") != meta["expected_history_status"]:
            last_error = f"unexpected history status: {run.get('status')}"
            time.sleep(0.25)
            continue
        events_body = request("GET", f"/api/v1/history/runs/{run['run_id']}/events?limit=50")
        event_types = {item.get("event_type") for item in events_body.get("events", [])}
        missing_events = sorted(expected_events - event_types)
        if missing_events:
            last_error = f"missing history events: {missing_events}"
            time.sleep(0.25)
            continue
        unexpected_events = sorted(forbidden_events & event_types)
        if unexpected_events:
            last_error = f"forbidden history events were observed: {unexpected_events}"
            time.sleep(0.25)
            continue
        if expected_failure and expected_failure not in (run.get("failure_reason", "") or ""):
            last_error = f"history failure reason did not contain expected text: {expected_failure}"
            time.sleep(0.25)
            continue
        metrics_body = request("GET", "/api/v1/history/metrics?limit=10")
        metrics = metrics_body.get("metrics", {})
        status_counts = metrics.get("status_counts", {})
        expected_metrics_total_runs = int(meta.get("expected_metrics_total_runs", 1) or 1)
        if metrics.get("total_runs", 0) != expected_metrics_total_runs:
            last_error = (
                "history metrics total_runs did not match expected value: "
                f"{metrics.get('total_runs', 0)} != {expected_metrics_total_runs}"
            )
            time.sleep(0.25)
            continue
        if status_counts.get(meta["expected_history_status"], 0) < 1:
            last_error = f"history metrics missing status count: {meta['expected_history_status']}"
            time.sleep(0.25)
            continue
        failure_category_counts = metrics.get("failure_category_counts", {})
        if expected_failure_category and failure_category_counts.get(expected_failure_category, 0) < 1:
            last_error = f"history metrics missing failure category count: {expected_failure_category}"
            time.sleep(0.25)
            continue
        if (
            meta["expected_history_status"] == "completed"
            and metrics.get("completed_runs", 0) < 1
        ):
            last_error = "history metrics did not count the completed run"
            time.sleep(0.25)
            continue
        run_total_tokens = run.get("tokens", {}).get("total_tokens", 0) or 0
        metrics_total_tokens = metrics.get("tokens", {}).get("total_tokens", 0) or 0
        if expected_min_total_tokens > 0 and run_total_tokens < expected_min_total_tokens:
            last_error = f"history run total_tokens below expected minimum: {run_total_tokens}"
            time.sleep(0.25)
            continue
        if expected_min_total_tokens > 0 and metrics_total_tokens < expected_min_total_tokens:
            last_error = f"history metrics total_tokens below expected minimum: {metrics_total_tokens}"
            time.sleep(0.25)
            continue
        if run_total_tokens > 0 and metrics_total_tokens < run_total_tokens:
            last_error = "history metrics token total is lower than the eval run"
            time.sleep(0.25)
            continue
        result = {
            "eval_id": meta["eval_id"],
            "status": "passed",
            "run_id": run["run_id"],
            "issue_identifier": issue_identifier,
            "workspace_file": str(workspace_file) if workspace_file.exists() else "",
            "summary_file": str(matched_summary_path),
            "summary_files": [str(path) for path, _ in matching_summaries],
            "agent_run_count": len(matching_summaries),
            "dispatch_kinds": sorted(set(observed_dispatch_kinds)),
            "dispatch_statuses": sorted(observed_dispatch_statuses),
            "history_status": run.get("status"),
            "failure_reason": run.get("failure_reason", ""),
            "events": sorted(event_types),
            "total_tokens": run.get("tokens", {}).get("total_tokens", 0),
            "codex_event_counts": dict(sorted(codex_event_counts.items())),
            "external_effects": sorted(observed_external_effects),
            "external_effect_details": sorted(observed_external_effect_details),
            "workspace_artifacts": matched_summary.get("workspace", {}).get("artifact_inventory", {}),
            "metrics": {
                "total_runs": metrics.get("total_runs", 0),
                "completed_runs": metrics.get("completed_runs", 0),
                "total_tokens": metrics_total_tokens,
                "status_counts": status_counts,
                "failure_category_counts": failure_category_counts,
            },
        }
        summary_path.write_text(json.dumps(result, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
        print(json.dumps(result, indent=2, ensure_ascii=False))
        raise SystemExit(0)
    except urllib.error.URLError as exc:
        last_error = str(exc)
        time.sleep(0.25)

raise SystemExit(f"Symphony memory eval failed: {last_error}")
PY

print_step "Symphony memory agent eval completed"
print_step "Generating eval harness metrics snapshot"
scripts/harness/generate-harness-metrics-report.sh \
  "$artifact_root" \
  "$artifact_root/agent-runs" \
  "$artifact_root/harness-metrics"
python3 - "$artifact_root" <<'PY'
from pathlib import Path
import json
import sys

artifact_root = Path(sys.argv[1])
summary_path = artifact_root / "summary.json"
body = json.loads(summary_path.read_text(encoding="utf-8"))
body["harness_metrics"] = {
    "json": str(artifact_root / "harness-metrics" / "latest.json"),
    "markdown": str(artifact_root / "harness-metrics" / "latest.md"),
}
summary_path.write_text(json.dumps(body, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
print(json.dumps(body["harness_metrics"], indent=2, ensure_ascii=False))
PY
print_step "Checking eval harness metrics permission posture"
check_harness_metrics_permission_posture \
  "$artifact_root" \
  "$artifact_root/harness-metrics/latest.json"
print_step "Checking eval agent run summaries"
scripts/harness/check-agent-run-summaries.sh "$artifact_root/agent-runs"
echo "Eval artifact: ${artifact_root}/summary.json"
