#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd python3

evals_dir="${1:-artifacts/agent-evals}"
agent_runs_dir="${2:-artifacts/agent-runs}"
output_dir="${3:-docs/reports/harness-metrics/generated}"
delivery_signals_dir="${4:-artifacts/harness-delivery-signals}"
deploy_drills_dir="${5:-docs/reports/deploy-drills}"
agent_eval_drafts_dir="${6:-artifacts/agent-eval-drafts}"

mkdir -p "$output_dir"

python3 - "$evals_dir" "$agent_runs_dir" "$output_dir" "$delivery_signals_dir" "$deploy_drills_dir" "$agent_eval_drafts_dir" <<'PY'
from __future__ import annotations

from collections import Counter
from datetime import datetime, timezone
from pathlib import Path
import json
import re
import statistics
import sys

evals_dir = Path(sys.argv[1])
agent_runs_dir = Path(sys.argv[2])
output_dir = Path(sys.argv[3])
delivery_signals_dir = Path(sys.argv[4])
deploy_drills_dir = Path(sys.argv[5])
agent_eval_drafts_dir = Path(sys.argv[6])
output_dir.mkdir(parents=True, exist_ok=True)


def read_json(path: Path) -> dict:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        raise SystemExit(f"Unable to read JSON artifact {path}: {exc}") from exc


def pct(part: int | float, total: int | float) -> float:
    if not total:
        return 0.0
    return round(float(part) * 100.0 / float(total), 2)


def avg(values: list[int | float]) -> float:
    if not values:
        return 0.0
    return round(float(statistics.mean(values)), 2)


def collect_eval_summary_documents(base: Path) -> list[dict]:
    if not base.exists():
        return []
    return [read_json(path) for path in sorted(base.glob("**/summary.json"))]


def collect_agent_run_summaries(base: Path, eval_base: Path) -> list[dict]:
    paths: set[Path] = set()
    if base.exists():
        paths.update(base.glob("*.json"))
    if eval_base.exists():
        paths.update(eval_base.glob("**/agent-runs/*.json"))
    return [read_json(path) for path in sorted(paths)]


def collect_delivery_signals(base: Path) -> list[dict]:
    if not base.exists():
        return []
    documents: list[dict] = []
    for path in sorted(base.glob("**/*.json")):
        document = read_json(path)
        if str(document.get("summary_type") or "").strip() == "harness_delivery_signal":
            documents.append(document)
    return documents


def collect_deploy_drill_reports(base: Path) -> list[dict]:
    if not base.exists():
        return []
    documents: list[dict] = []
    for path in sorted(base.glob("*.md")):
        if path.name == "README.md":
            continue
        text = path.read_text(encoding="utf-8")
        if re.search(r"^Status:\s*template\s*$", text, re.MULTILINE):
            continue
        for match in re.finditer(r"```json\s*(\{.*?\})\s*```", text, re.DOTALL):
            try:
                document = json.loads(match.group(1))
            except json.JSONDecodeError as exc:
                raise SystemExit(f"Invalid deploy drill JSON summary in {path}: {exc}") from exc
            if str(document.get("summary_type") or "").strip() == "deploy_drill_report":
                documents.append(document)
                break
    return documents


def parse_simple_yaml(path: Path) -> dict[str, object]:
    data: dict[str, object] = {}
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
                data[key] = value.strip("'\"")
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
                existing.append(stripped[2:].strip().strip("'\""))
    return data


def collect_agent_eval_drafts(base: Path) -> list[dict]:
    if not base.exists():
        return []
    documents: list[dict] = []
    for path in sorted(base.glob("*.yml")):
        document = parse_simple_yaml(path)
        if document:
            document["_path"] = str(path)
            documents.append(document)
    return documents


def count_metric(value: object) -> int:
    if isinstance(value, bool):
        return 1 if value else 0
    if isinstance(value, int):
        return value
    if isinstance(value, float):
        return int(value)
    return 0


def numeric_values(value: object) -> list[float]:
    if isinstance(value, (int, float)) and not isinstance(value, bool):
        return [float(value)]
    if isinstance(value, list):
        return [float(item) for item in value if isinstance(item, (int, float)) and not isinstance(item, bool)]
    return []


def add_counts(counter: Counter[str], value: object) -> int:
    total = 0
    if not isinstance(value, dict):
        return total
    for raw_key, raw_count in value.items():
        key = str(raw_key or "").strip().lower()
        if not key:
            continue
        count = count_metric(raw_count)
        if count <= 0:
            continue
        counter[key] += count
        total += count
    return total


def add_event_counts(counter: Counter[str], value: object) -> int:
    total = 0
    if not isinstance(value, dict):
        return total
    for raw_key, raw_count in value.items():
        key = str(raw_key or "").strip()
        if not key:
            continue
        count = count_metric(raw_count)
        if count <= 0:
            continue
        counter[key] += count
        total += count
    return total


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


def status_counts(items: list[dict]) -> dict[str, int]:
    counts: Counter[str] = Counter()
    for item in items:
        status = str(item.get("status") or "unknown").strip().lower()
        counts[status or "unknown"] += 1
    return dict(sorted(counts.items()))


def java_major(version: object) -> str:
    text = str(version or "").strip()
    if not text:
        return "unknown"
    parts = text.split(".")
    if parts[0] == "1" and len(parts) > 1 and parts[1].isdigit():
        return parts[1]
    digits = ""
    for char in text:
        if char.isdigit():
            digits += char
            continue
        break
    return digits or "unknown"


def failure_category(status: str, reason: str) -> str:
    normalized_status = (status or "").strip().lower()
    normalized_reason = (reason or "").strip().lower()
    if normalized_status in {"completed", "succeeded"}:
        return "none"
    if normalized_status in {"interrupted", "terminated", "running"}:
        return normalized_status
    if not normalized_reason:
        return "unknown_failure"
    if (
        "permission preflight" in normalized_reason
        or "network_access_reason" in normalized_reason
        or "writable root" in normalized_reason
        or "danger-full-access" in normalized_reason
        or "sandbox" in normalized_reason
    ):
        return "permission"
    if "after_create" in normalized_reason or "before_run" in normalized_reason or "hook failed" in normalized_reason:
        return "workspace_hook"
    if "startup" in normalized_reason or "initialize" in normalized_reason or "app-server handshake" in normalized_reason:
        return "codex_startup"
    if "response timeout" in normalized_reason or "turn timeout" in normalized_reason or "timeout" in normalized_reason:
        return "codex_timeout"
    if "approval" in normalized_reason:
        return "approval_required"
    if "dynamic tool" in normalized_reason or "tool_call" in normalized_reason:
        return "dynamic_tool"
    if "user input" in normalized_reason or "input_required" in normalized_reason:
        return "user_input_required"
    if "codex" in normalized_reason or "turn" in normalized_reason:
        return "codex_runtime"
    return "unknown_failure"


eval_summary_documents = collect_eval_summary_documents(evals_dir)
eval_suites = [
    item
    for item in eval_summary_documents
    if str(item.get("summary_type") or "").strip() == "symphony_memory_eval_suite"
]
evals = [
    item
    for item in eval_summary_documents
    if str(item.get("summary_type") or "").strip() != "symphony_memory_eval_suite"
]
agent_runs = collect_agent_run_summaries(agent_runs_dir, evals_dir)
delivery_signals = collect_delivery_signals(delivery_signals_dir)
deploy_drills = collect_deploy_drill_reports(deploy_drills_dir)
agent_eval_drafts = collect_agent_eval_drafts(agent_eval_drafts_dir)

eval_passed = sum(1 for item in evals if str(item.get("status", "")).lower() == "passed")
suite_passed = sum(1 for item in eval_suites if str(item.get("status", "")).lower() == "passed")
suite_case_total = sum(
    int(item.get("total", 0) or 0)
    for item in eval_suites
    if isinstance(item.get("total", 0), int)
)
suite_case_passed = sum(
    int(item.get("passed", 0) or 0)
    for item in eval_suites
    if isinstance(item.get("passed", 0), int)
)
suite_case_failed = sum(
    int(item.get("failed", 0) or 0)
    for item in eval_suites
    if isinstance(item.get("failed", 0), int)
)
run_completed = sum(1 for item in agent_runs if str(item.get("status", "")).lower() == "completed")
run_retried = sum(
    1
    for item in agent_runs
    if item.get("retry", {}).get("scheduled") is True
    and str(item.get("retry", {}).get("dispatch_kind") or "retry") == "retry"
)
run_durations = [
    float(item.get("duration_seconds", 0) or 0)
    for item in agent_runs
    if isinstance(item.get("duration_seconds", 0), (int, float))
]
run_turn_counts = [
    int(item.get("attempt", {}).get("turn_count", 0) or 0)
    for item in agent_runs
    if isinstance(item.get("attempt", {}).get("turn_count", 0), int)
]
run_tokens = [
    int(item.get("codex", {}).get("usage", {}).get("total_tokens", 0) or 0)
    for item in agent_runs
    if isinstance(item.get("codex", {}).get("usage", {}).get("total_tokens", 0), int)
]
eval_tokens = [
    int(item.get("total_tokens", 0) or 0)
    for item in evals
    if isinstance(item.get("total_tokens", 0), int)
]

codex_event_counts: Counter[str] = Counter()
codex_event_total = 0
for item in agent_runs:
    codex = item.get("codex") if isinstance(item.get("codex"), dict) else {}
    codex_event_total += add_event_counts(codex_event_counts, codex.get("event_counts"))

failure_categories: Counter[str] = Counter()
for item in agent_runs:
    status = str(item.get("status", "")).lower()
    if status == "completed":
        continue
    category = str(item.get("failure_category") or "").strip()
    if not category:
        category = failure_category(status, str(item.get("failure_reason") or ""))
    failure_categories[category] += 1

external_effects = {
    "tracker_state_claimed": sum(
        1 for item in agent_runs if item.get("external_effects", {}).get("tracker_state_claimed") is True
    ),
    "linear_comment_attempted": sum(
        1 for item in agent_runs if item.get("external_effects", {}).get("linear_comment_attempted") is True
    ),
}
external_effect_events = [
    event
    for item in agent_runs
    for event in item.get("external_effects", {}).get("events", [])
    if isinstance(event, dict)
]
external_effect_status_counts: Counter[str] = Counter()
external_effect_type_counts: Counter[str] = Counter()
external_effect_type_status_counts: Counter[str] = Counter()
external_effect_error_code_counts: Counter[str] = Counter()
for event in external_effect_events:
    status = str(event.get("status") or "unknown").strip().lower()
    event_type = str(event.get("type") or "unknown").strip().lower()
    normalized_status = status or "unknown"
    normalized_type = event_type or "unknown"
    error_code = str(event.get("error_code") or "").strip()
    external_effect_status_counts[normalized_status] += 1
    external_effect_type_counts[normalized_type] += 1
    external_effect_type_status_counts[f"{normalized_type}:{normalized_status}"] += 1
    if error_code:
        external_effect_error_code_counts[error_code] += 1

environment_java_major_counts: Counter[str] = Counter()
environment_os_name_counts: Counter[str] = Counter()
environment_os_arch_counts: Counter[str] = Counter()
environment_spring_profile_counts: Counter[str] = Counter()
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
    environment = item.get("environment") if isinstance(item.get("environment"), dict) else {}
    java = environment.get("java") if isinstance(environment.get("java"), dict) else {}
    os = environment.get("os") if isinstance(environment.get("os"), dict) else {}
    environment_java_major_counts[java_major(java.get("version"))] += 1
    os_name = str(os.get("name") or "unknown").strip().lower()
    os_arch = str(os.get("arch") or "unknown").strip().lower()
    environment_os_name_counts[os_name or "unknown"] += 1
    environment_os_arch_counts[os_arch or "unknown"] += 1
    profiles = environment.get("spring_profiles")
    if isinstance(profiles, list) and profiles:
        for profile in profiles:
            normalized_profile = str(profile or "").strip().lower()
            if normalized_profile:
                environment_spring_profile_counts[normalized_profile] += 1
    else:
        environment_spring_profile_counts["default"] += 1
    permissions = item.get("permissions") if isinstance(item.get("permissions"), dict) else {}
    if isinstance(permissions, dict) and permissions:
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

workspace_artifact_file_counts: list[int] = []
workspace_artifact_byte_counts: list[int] = []
workspace_artifact_truncated_runs = 0
workspace_artifact_scan_error_counts: Counter[str] = Counter()
for item in agent_runs:
    workspace = item.get("workspace") if isinstance(item.get("workspace"), dict) else {}
    inventory = workspace.get("artifact_inventory") if isinstance(workspace.get("artifact_inventory"), dict) else {}
    file_count = count_metric(inventory.get("file_count"))
    total_bytes = count_metric(inventory.get("total_bytes"))
    workspace_artifact_file_counts.append(file_count)
    workspace_artifact_byte_counts.append(total_bytes)
    if inventory.get("truncated") is True:
        workspace_artifact_truncated_runs += 1
    scan_error = str(inventory.get("scan_error") or "").strip().lower()
    if scan_error:
        workspace_artifact_scan_error_counts[scan_error] += 1

delivery_provider_counts: Counter[str] = Counter()
pull_requests_created = 0
pull_requests_merged = 0
pull_requests_reverted = 0
merge_time_seconds: list[float] = []
review_finding_severity_counts: Counter[str] = Counter()
review_finding_category_counts: Counter[str] = Counter()
review_findings_total = 0
for item in delivery_signals:
    provider = str(item.get("provider") or "unknown").strip().lower()
    delivery_provider_counts[provider or "unknown"] += 1
    pull_requests = item.get("pull_requests") or item.get("pull_request") or {}
    if isinstance(pull_requests, dict):
        pull_requests_created += count_metric(pull_requests.get("created"))
        pull_requests_merged += count_metric(pull_requests.get("merged"))
        pull_requests_reverted += count_metric(pull_requests.get("reverted"))
        merge_time_seconds.extend(numeric_values(pull_requests.get("merge_time_seconds")))
    review_findings = item.get("review_findings") or item.get("review") or {}
    if isinstance(review_findings, dict):
        severity_total = add_counts(review_finding_severity_counts, review_findings.get("severity_counts"))
        category_total = add_counts(review_finding_category_counts, review_findings.get("category_counts"))
        explicit_total = count_metric(review_findings.get("total", review_findings.get("count")))
        review_findings_total += explicit_total if explicit_total > 0 else max(severity_total, category_total)

deploy_drill_kind_counts: Counter[str] = Counter()
deploy_drill_status_counts: Counter[str] = Counter()
deploy_drill_service_counts: Counter[str] = Counter()
deploy_drill_smoke_counts: Counter[str] = Counter()
deploy_drill_failure_stage_counts: Counter[str] = Counter()
for item in deploy_drills:
    kind = str(item.get("kind") or "unknown").strip().lower()
    status = str(item.get("status") or "unknown").strip().lower()
    smoke = str(item.get("smoke") or "unknown").strip().lower()
    service = str(item.get("service") or "unknown").strip().lower()
    failure_stage = str(item.get("failure_stage") or "").strip().lower()
    deploy_drill_kind_counts[kind or "unknown"] += 1
    deploy_drill_status_counts[status or "unknown"] += 1
    deploy_drill_service_counts[service or "unknown"] += 1
    deploy_drill_smoke_counts[smoke or "unknown"] += 1
    if failure_stage:
        deploy_drill_failure_stage_counts[failure_stage] += 1

draft_failure_category_counts: Counter[str] = Counter()
draft_risk_level_counts: Counter[str] = Counter()
draft_manual_review_required = 0
for item in agent_eval_drafts:
    category = str(item.get("expected_failure_category") or "unknown").strip().lower()
    risk = str(item.get("risk_level") or "unknown").strip().lower()
    manual_review_required = str(item.get("manual_review_required") or "").strip().lower()
    draft_failure_category_counts[category or "unknown"] += 1
    draft_risk_level_counts[risk or "unknown"] += 1
    if manual_review_required == "true":
        draft_manual_review_required += 1

snapshot = {
    "schema_version": 1,
    "summary_type": "harness_metrics_snapshot",
    "generated_at": datetime.now(timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z"),
    "sources": {
        "evals_dir": str(evals_dir),
        "agent_runs_dir": str(agent_runs_dir),
        "eval_summaries": str(evals_dir / "**/summary.json"),
        "nested_eval_agent_runs": str(evals_dir / "**/agent-runs"),
        "delivery_signals_dir": str(delivery_signals_dir),
        "delivery_signals": str(delivery_signals_dir / "**/*.json"),
        "deploy_drills_dir": str(deploy_drills_dir),
        "deploy_drills": str(deploy_drills_dir / "*.md"),
        "agent_eval_drafts_dir": str(agent_eval_drafts_dir),
        "agent_eval_drafts": str(agent_eval_drafts_dir / "*.yml"),
    },
    "evals": {
        "total": len(evals),
        "passed": eval_passed,
        "failed": len(evals) - eval_passed,
        "pass_rate": pct(eval_passed, len(evals)),
        "status_counts": status_counts(evals),
        "total_tokens": sum(eval_tokens),
        "average_tokens": avg(eval_tokens),
    },
    "eval_suites": {
        "total": len(eval_suites),
        "passed": suite_passed,
        "failed": len(eval_suites) - suite_passed,
        "pass_rate": pct(suite_passed, len(eval_suites)),
        "status_counts": status_counts(eval_suites),
        "case_total": suite_case_total,
        "case_passed": suite_case_passed,
        "case_failed": suite_case_failed,
    },
    "agent_runs": {
        "total": len(agent_runs),
        "completed": run_completed,
        "success_rate": pct(run_completed, len(agent_runs)),
        "retried": run_retried,
        "retry_rate": pct(run_retried, len(agent_runs)),
        "status_counts": status_counts(agent_runs),
        "average_duration_seconds": avg(run_durations),
        "average_turns": avg(run_turn_counts),
        "total_tokens": sum(run_tokens),
        "average_tokens": avg(run_tokens),
        "failure_categories": dict(sorted(failure_categories.items())),
        "codex_events": {
            "total": codex_event_total,
            "event_counts": dict(sorted(codex_event_counts.items())),
        },
        "external_effects": external_effects,
        "external_effect_events": {
            "total": len(external_effect_events),
            "status_counts": dict(sorted(external_effect_status_counts.items())),
            "type_counts": dict(sorted(external_effect_type_counts.items())),
            "type_status_counts": dict(sorted(external_effect_type_status_counts.items())),
            "error_code_counts": dict(sorted(external_effect_error_code_counts.items())),
        },
        "environment": {
            "java_major_counts": dict(sorted(environment_java_major_counts.items())),
            "os_name_counts": dict(sorted(environment_os_name_counts.items())),
            "os_arch_counts": dict(sorted(environment_os_arch_counts.items())),
            "spring_profile_counts": dict(sorted(environment_spring_profile_counts.items())),
        },
        "permission_posture": {
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
        },
        "workspace_artifacts": {
            "total_files": sum(workspace_artifact_file_counts),
            "average_files": avg(workspace_artifact_file_counts),
            "total_bytes": sum(workspace_artifact_byte_counts),
            "average_bytes": avg(workspace_artifact_byte_counts),
            "truncated_runs": workspace_artifact_truncated_runs,
            "scan_error_counts": dict(sorted(workspace_artifact_scan_error_counts.items())),
        },
    },
    "delivery": {
        "total_documents": len(delivery_signals),
        "provider_counts": dict(sorted(delivery_provider_counts.items())),
        "pull_requests": {
            "created": pull_requests_created,
            "merged": pull_requests_merged,
            "reverted": pull_requests_reverted,
            "average_merge_time_seconds": avg(merge_time_seconds),
        },
        "review_findings": {
            "total": review_findings_total,
            "severity_counts": dict(sorted(review_finding_severity_counts.items())),
            "category_counts": dict(sorted(review_finding_category_counts.items())),
        },
    },
    "deploy_drills": {
        "total_reports": len(deploy_drills),
        "kind_counts": dict(sorted(deploy_drill_kind_counts.items())),
        "status_counts": dict(sorted(deploy_drill_status_counts.items())),
        "service_counts": dict(sorted(deploy_drill_service_counts.items())),
        "smoke_counts": dict(sorted(deploy_drill_smoke_counts.items())),
        "failure_stage_counts": dict(sorted(deploy_drill_failure_stage_counts.items())),
    },
    "agent_eval_drafts": {
        "total": len(agent_eval_drafts),
        "manual_review_required": draft_manual_review_required,
        "failure_category_counts": dict(sorted(draft_failure_category_counts.items())),
        "risk_level_counts": dict(sorted(draft_risk_level_counts.items())),
    },
}

json_path = output_dir / "latest.json"
md_path = output_dir / "latest.md"
json_path.write_text(json.dumps(snapshot, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

md_path.write_text(
    "\n".join(
        [
            "# Harness Metrics Snapshot",
            "",
            "Status: generated",
            f"Last Reviewed: {datetime.now(timezone.utc).date().isoformat()}",
            "Review Cadence: 30 days",
            "",
            "## 数据来源",
            "",
            f"- Eval artifacts: `{evals_dir}`",
            f"- Agent run summaries: `{agent_runs_dir}`",
            f"- Eval summaries: `{evals_dir}/**/summary.json`",
            f"- Eval nested agent run summaries: `{evals_dir}/**/agent-runs/`",
            f"- Delivery signals: `{delivery_signals_dir}/**/*.json`",
            f"- Deploy drill reports: `{deploy_drills_dir}/*.md`",
            f"- Agent eval drafts: `{agent_eval_drafts_dir}/*.yml`",
            "",
            "## Agent Eval 指标",
            "",
            "| 指标 | 值 |",
            "|------|----|",
            f"| eval 总数 | {snapshot['evals']['total']} |",
            f"| eval 通过数 | {snapshot['evals']['passed']} |",
            f"| eval 通过率 | {snapshot['evals']['pass_rate']}% |",
            f"| eval token 总量 | {snapshot['evals']['total_tokens']} |",
            f"| eval 平均 token | {snapshot['evals']['average_tokens']} |",
            "",
            "## Agent Eval Suite 指标",
            "",
            "| 指标 | 值 |",
            "|------|----|",
            f"| suite 总数 | {snapshot['eval_suites']['total']} |",
            f"| suite 通过数 | {snapshot['eval_suites']['passed']} |",
            f"| suite 通过率 | {snapshot['eval_suites']['pass_rate']}% |",
            f"| suite case 总数 | {snapshot['eval_suites']['case_total']} |",
            f"| suite case 通过数 | {snapshot['eval_suites']['case_passed']} |",
            f"| suite case 失败数 | {snapshot['eval_suites']['case_failed']} |",
            "",
            "## Agent Run 指标",
            "",
            "| 指标 | 值 |",
            "|------|----|",
            f"| run 总数 | {snapshot['agent_runs']['total']} |",
            f"| completed run | {snapshot['agent_runs']['completed']} |",
            f"| 成功率 | {snapshot['agent_runs']['success_rate']}% |",
            f"| 重试数 | {snapshot['agent_runs']['retried']} |",
            f"| 重试率 | {snapshot['agent_runs']['retry_rate']}% |",
            f"| 平均耗时（秒） | {snapshot['agent_runs']['average_duration_seconds']} |",
            f"| 平均 turn | {snapshot['agent_runs']['average_turns']} |",
            f"| token 总量 | {snapshot['agent_runs']['total_tokens']} |",
            f"| 平均 token | {snapshot['agent_runs']['average_tokens']} |",
            "",
            "## 状态分布",
            "",
            f"- Eval: `{json.dumps(snapshot['evals']['status_counts'], ensure_ascii=False, sort_keys=True)}`",
            f"- Eval suite: `{json.dumps(snapshot['eval_suites']['status_counts'], ensure_ascii=False, sort_keys=True)}`",
            f"- Agent run: `{json.dumps(snapshot['agent_runs']['status_counts'], ensure_ascii=False, sort_keys=True)}`",
            f"- Failure category: `{json.dumps(snapshot['agent_runs']['failure_categories'], ensure_ascii=False, sort_keys=True)}`",
            "",
            "## Codex 事件",
            "",
            f"- Codex event total: {snapshot['agent_runs']['codex_events']['total']}",
            f"- Codex event counts: `{json.dumps(snapshot['agent_runs']['codex_events']['event_counts'], ensure_ascii=False, sort_keys=True)}`",
            "",
            "## 外部副作用",
            "",
            f"- tracker state claimed: {snapshot['agent_runs']['external_effects']['tracker_state_claimed']}",
            f"- Linear comment attempted: {snapshot['agent_runs']['external_effects']['linear_comment_attempted']}",
            f"- external effect events: {snapshot['agent_runs']['external_effect_events']['total']}",
            f"- external effect status: `{json.dumps(snapshot['agent_runs']['external_effect_events']['status_counts'], ensure_ascii=False, sort_keys=True)}`",
            f"- external effect type: `{json.dumps(snapshot['agent_runs']['external_effect_events']['type_counts'], ensure_ascii=False, sort_keys=True)}`",
            f"- external effect type/status: `{json.dumps(snapshot['agent_runs']['external_effect_events']['type_status_counts'], ensure_ascii=False, sort_keys=True)}`",
            f"- external effect error code: `{json.dumps(snapshot['agent_runs']['external_effect_events']['error_code_counts'], ensure_ascii=False, sort_keys=True)}`",
            "",
            "## 运行环境",
            "",
            f"- Java major: `{json.dumps(snapshot['agent_runs']['environment']['java_major_counts'], ensure_ascii=False, sort_keys=True)}`",
            f"- OS name: `{json.dumps(snapshot['agent_runs']['environment']['os_name_counts'], ensure_ascii=False, sort_keys=True)}`",
            f"- OS arch: `{json.dumps(snapshot['agent_runs']['environment']['os_arch_counts'], ensure_ascii=False, sort_keys=True)}`",
            f"- Spring profile: `{json.dumps(snapshot['agent_runs']['environment']['spring_profile_counts'], ensure_ascii=False, sort_keys=True)}`",
            "",
            "## 权限姿态",
            "",
            f"- summaries with permissions: {snapshot['agent_runs']['permission_posture']['summaries_with_permissions']}",
            f"- remote worker runs: {snapshot['agent_runs']['permission_posture']['remote_worker_runs']}",
            f"- network access runs: {snapshot['agent_runs']['permission_posture']['network_access_runs']}",
            f"- danger full access allowed runs: {snapshot['agent_runs']['permission_posture']['danger_full_access_allowed_runs']}",
            f"- approval policy: `{json.dumps(snapshot['agent_runs']['permission_posture']['approval_policy_counts'], ensure_ascii=False, sort_keys=True)}`",
            f"- thread sandbox: `{json.dumps(snapshot['agent_runs']['permission_posture']['thread_sandbox_counts'], ensure_ascii=False, sort_keys=True)}`",
            f"- turn sandbox type: `{json.dumps(snapshot['agent_runs']['permission_posture']['turn_sandbox_type_counts'], ensure_ascii=False, sort_keys=True)}`",
            f"- writable root counts: `{json.dumps(snapshot['agent_runs']['permission_posture']['writable_root_count_distribution'], ensure_ascii=False, sort_keys=True)}`",
            f"- allowed writable root counts: `{json.dumps(snapshot['agent_runs']['permission_posture']['allowed_writable_root_count_distribution'], ensure_ascii=False, sort_keys=True)}`",
            "",
            "## Workspace Artifact Inventory",
            "",
            f"- workspace artifact files: {snapshot['agent_runs']['workspace_artifacts']['total_files']}",
            f"- average files per run: {snapshot['agent_runs']['workspace_artifacts']['average_files']}",
            f"- workspace artifact bytes: {snapshot['agent_runs']['workspace_artifacts']['total_bytes']}",
            f"- average bytes per run: {snapshot['agent_runs']['workspace_artifacts']['average_bytes']}",
            f"- truncated runs: {snapshot['agent_runs']['workspace_artifacts']['truncated_runs']}",
            f"- scan errors: `{json.dumps(snapshot['agent_runs']['workspace_artifacts']['scan_error_counts'], ensure_ascii=False, sort_keys=True)}`",
            "",
            "## Delivery 信号",
            "",
            f"- delivery signal documents: {snapshot['delivery']['total_documents']}",
            f"- provider: `{json.dumps(snapshot['delivery']['provider_counts'], ensure_ascii=False, sort_keys=True)}`",
            f"- PR created: {snapshot['delivery']['pull_requests']['created']}",
            f"- PR merged: {snapshot['delivery']['pull_requests']['merged']}",
            f"- PR reverted: {snapshot['delivery']['pull_requests']['reverted']}",
            f"- average merge time seconds: {snapshot['delivery']['pull_requests']['average_merge_time_seconds']}",
            f"- review findings: {snapshot['delivery']['review_findings']['total']}",
            f"- review finding severity: `{json.dumps(snapshot['delivery']['review_findings']['severity_counts'], ensure_ascii=False, sort_keys=True)}`",
            f"- review finding category: `{json.dumps(snapshot['delivery']['review_findings']['category_counts'], ensure_ascii=False, sort_keys=True)}`",
            "",
            "## 部署 / 回滚演练",
            "",
            f"- deploy drill reports: {snapshot['deploy_drills']['total_reports']}",
            f"- drill kind: `{json.dumps(snapshot['deploy_drills']['kind_counts'], ensure_ascii=False, sort_keys=True)}`",
            f"- drill status: `{json.dumps(snapshot['deploy_drills']['status_counts'], ensure_ascii=False, sort_keys=True)}`",
            f"- drill service: `{json.dumps(snapshot['deploy_drills']['service_counts'], ensure_ascii=False, sort_keys=True)}`",
            f"- drill smoke: `{json.dumps(snapshot['deploy_drills']['smoke_counts'], ensure_ascii=False, sort_keys=True)}`",
            f"- drill failure stage: `{json.dumps(snapshot['deploy_drills']['failure_stage_counts'], ensure_ascii=False, sort_keys=True)}`",
            "",
            "## Agent Eval 草稿",
            "",
            f"- draft total: {snapshot['agent_eval_drafts']['total']}",
            f"- manual review required: {snapshot['agent_eval_drafts']['manual_review_required']}",
            f"- draft failure category: `{json.dumps(snapshot['agent_eval_drafts']['failure_category_counts'], ensure_ascii=False, sort_keys=True)}`",
            f"- draft risk level: `{json.dumps(snapshot['agent_eval_drafts']['risk_level_counts'], ensure_ascii=False, sort_keys=True)}`",
            "",
            "## 边界",
            "",
            "- 本报告只聚合本地低敏 artifacts 和报告摘要，不读取完整 prompt、聊天记录、工具输出、review 正文或外部系统响应。",
            "- `latest.*` 是可再生成快照，长期趋势应另行沉淀为日期化报告或接入 CI artifacts。",
            "",
        ]
    ),
    encoding="utf-8",
)

print(f"Wrote {json_path}")
print(f"Wrote {md_path}")
PY
