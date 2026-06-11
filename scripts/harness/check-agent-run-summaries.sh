#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd python3

print_step "Checking agent run summaries"

python3 - "$@" <<'PY'
from __future__ import annotations

from pathlib import Path
import json
import re
import sys
import tempfile

repo = Path.cwd()
args = sys.argv[1:]

patterns: list[tuple[str, re.Pattern[str]]] = [
    ("bearer-token", re.compile(r"(?i)\bbearer\s+[A-Za-z0-9._~+/=-]{12,}")),
    ("jwt", re.compile(r"\beyJ[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\b")),
    ("aws-access-key", re.compile(r"\bAKIA[0-9A-Z]{16}\b")),
    (
        "secret-assignment",
        re.compile(
            r"(?i)\b(?:api[_-]?key|access[_-]?token|secret|password|passwd|pwd|token)\s*[:=]\s*['\"]?[A-Za-z0-9._~+/=-]{8,}"
        ),
    ),
    ("jdbc-password", re.compile(r"(?i)\bjdbc:[^\s`'\"]*(?:password|passwd|pwd)=[^&\s`'\"]+")),
    ("mysql-password-uri", re.compile(r"(?i)\bmysql://[^:\s`'\"]+:[^@\s`'\"]+@")),
    ("postgres-password-uri", re.compile(r"(?i)\bpostgres(?:ql)?://[^:\s`'\"]+:[^@\s`'\"]+@")),
    ("redis-password-uri", re.compile(r"(?i)\bredis://[^:\s`'\"]+:[^@\s`'\"]+@")),
]

allowed_readme_phrases = {
    "token、密码、生产连接串、客户数据和个人敏感数据。",
}

required_top_level_keys = {
    "schema_version",
    "summary_type",
    "run_id",
    "status",
    "failure_reason",
    "failure_category",
    "started_at",
    "finished_at",
    "duration_seconds",
    "issue",
    "attempt",
    "workspace",
    "codex",
    "permissions",
    "environment",
    "retry",
    "external_effects",
}
required_nested_keys = {
    "issue": {"id", "identifier", "title", "state"},
    "attempt": {"number", "retry_attempt", "dispatch_kind", "parent_run_id", "turn_count"},
    "workspace": {"path", "key", "worker_host", "artifact_inventory"},
    "codex": {"session_id", "last_event", "event_counts", "usage"},
    "permissions": {
        "approval_policy",
        "thread_sandbox",
        "turn_sandbox_policy",
        "remote_worker",
        "network_access",
        "writable_roots",
    },
    "environment": {"java", "maven", "os", "process", "spring_profiles"},
    "retry": {"scheduled", "dispatch_kind"},
    "external_effects": {"tracker_state_claimed", "linear_comment_attempted"},
}
required_usage_keys = {"input_tokens", "output_tokens", "total_tokens"}
required_java_environment_keys = {"version", "vendor", "runtime_name", "runtime_version", "vm_name", "vm_version"}
required_maven_environment_keys = {"version"}
required_os_environment_keys = {"name", "arch", "version"}
required_external_effect_event_keys = {
    "type",
    "provider",
    "target",
    "status",
    "error_code",
    "error_message",
    "at",
}
allowed_external_effect_statuses = {"", "succeeded", "failed", "skipped"}
required_workspace_artifact_inventory_keys = {
    "max_files",
    "file_count",
    "total_bytes",
    "truncated",
    "scan_error",
    "files",
}
required_workspace_artifact_file_keys = {"path", "size_bytes"}


def is_unsafe_path_reference(value: object) -> bool:
    if not isinstance(value, str) or not value:
        return False
    path = Path(value)
    return path.is_absolute() or ".." in path.parts


def validate_path_reference_list(value: object, rel: Path, path_label: str) -> list[str]:
    errors: list[str] = []
    if not isinstance(value, list):
        errors.append(f"{rel}: {path_label} must be a list")
        return errors
    for index, item in enumerate(value):
        if not isinstance(item, str):
            errors.append(f"{rel}: {path_label}[{index}] must be string")
            continue
        if is_unsafe_path_reference(item):
            errors.append(f"{rel}: {path_label}[{index}] must be a low-sensitive relative reference")
    return errors


def validate_policy_path_references(value: object, rel: Path, path_label: str) -> list[str]:
    errors: list[str] = []
    if isinstance(value, dict):
        for key, nested in value.items():
            nested_label = f"{path_label}.{key}"
            if key == "writableRoots":
                errors.extend(validate_path_reference_list(nested, rel, nested_label))
            else:
                errors.extend(validate_policy_path_references(nested, rel, nested_label))
    elif isinstance(value, list):
        for index, nested in enumerate(value):
            errors.extend(validate_policy_path_references(nested, rel, f"{path_label}[{index}]"))
    return errors


def scan_path(path: Path, root: Path) -> list[str]:
    errors: list[str] = []
    if not path.exists():
        errors.append(f"{path}: path does not exist")
        return errors

    if path.is_file():
        candidates = [path]
    else:
        candidates = sorted(
            candidate
            for candidate in path.rglob("*")
            if candidate.is_file()
        )

    for candidate in candidates:
        if candidate.name == "README.md" or candidate.suffix.lower() not in {".md", ".json"}:
            continue
        text = candidate.read_text(encoding="utf-8", errors="replace")
        rel = candidate.relative_to(root) if candidate.is_relative_to(root) else candidate
        for line_no, line in enumerate(text.splitlines(), start=1):
            if line.strip() in allowed_readme_phrases:
                continue
            for label, pattern in patterns:
                if pattern.search(line):
                    errors.append(f"{rel}:{line_no}: possible sensitive run summary content ({label})")
        if candidate.suffix.lower() == ".json":
            errors.extend(validate_agent_run_summary(candidate, rel))
    return errors


def validate_agent_run_summary(path: Path, rel: Path) -> list[str]:
    try:
        body = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        return [f"{rel}:{exc.lineno}: invalid JSON summary ({exc.msg})"]
    if not isinstance(body, dict):
        return [f"{rel}: JSON summary must be an object"]
    if body.get("summary_type") != "symphony_agent_run":
        return []

    errors: list[str] = []
    missing = sorted(required_top_level_keys - set(body.keys()))
    for key in missing:
        errors.append(f"{rel}: symphony_agent_run summary missing top-level key {key}")
    if body.get("schema_version") != 1:
        errors.append(f"{rel}: symphony_agent_run summary schema_version must be 1")
    for key in ("run_id", "status", "failure_category", "finished_at"):
        if not isinstance(body.get(key), str) or not body.get(key):
            errors.append(f"{rel}: symphony_agent_run summary key {key} must be a non-empty string")
    if body.get("status") != "completed" and not body.get("failure_category"):
        errors.append(f"{rel}: non-completed summary must include failure_category")
    if not isinstance(body.get("duration_seconds"), int):
        errors.append(f"{rel}: duration_seconds must be an integer")

    for key, nested_keys in required_nested_keys.items():
        value = body.get(key)
        if not isinstance(value, dict):
            errors.append(f"{rel}: {key} must be an object")
            continue
        nested_missing = sorted(nested_keys - set(value.keys()))
        for nested_key in nested_missing:
            errors.append(f"{rel}: {key} missing key {nested_key}")

    usage = body.get("codex", {}).get("usage") if isinstance(body.get("codex"), dict) else None
    if not isinstance(usage, dict):
        errors.append(f"{rel}: codex.usage must be an object")
    else:
        for key in sorted(required_usage_keys):
            if not isinstance(usage.get(key), int):
                errors.append(f"{rel}: codex.usage.{key} must be an integer")

    event_counts = body.get("codex", {}).get("event_counts") if isinstance(body.get("codex"), dict) else None
    if not isinstance(event_counts, dict):
        errors.append(f"{rel}: codex.event_counts must be an object")
    else:
        for key, value in sorted(event_counts.items()):
            if not isinstance(key, str) or not key.strip():
                errors.append(f"{rel}: codex.event_counts keys must be non-empty strings")
            if not isinstance(value, int) or isinstance(value, bool) or value < 0:
                errors.append(f"{rel}: codex.event_counts.{key} must be a non-negative integer")

    permissions = body.get("permissions")
    attempt = body.get("attempt")
    if isinstance(attempt, dict):
        for key in ("dispatch_kind", "parent_run_id"):
            if not isinstance(attempt.get(key), str):
                errors.append(f"{rel}: attempt.{key} must be string")
        for key in ("number", "retry_attempt", "turn_count"):
            if not isinstance(attempt.get(key), int):
                errors.append(f"{rel}: attempt.{key} must be an integer")

    workspace = body.get("workspace")
    if isinstance(workspace, dict):
        workspace_path = workspace.get("path")
        if not isinstance(workspace_path, str):
            errors.append(f"{rel}: workspace.path must be string")
        elif is_unsafe_path_reference(workspace_path):
            errors.append(f"{rel}: workspace.path must be a low-sensitive relative reference")
        inventory = workspace.get("artifact_inventory")
        if not isinstance(inventory, dict):
            errors.append(f"{rel}: workspace.artifact_inventory must be an object")
        else:
            missing = sorted(required_workspace_artifact_inventory_keys - set(inventory.keys()))
            for key in missing:
                errors.append(f"{rel}: workspace.artifact_inventory missing key {key}")
            for key in ("max_files", "file_count", "total_bytes"):
                if not isinstance(inventory.get(key), int):
                    errors.append(f"{rel}: workspace.artifact_inventory.{key} must be an integer")
            if not isinstance(inventory.get("truncated"), bool):
                errors.append(f"{rel}: workspace.artifact_inventory.truncated must be boolean")
            if not isinstance(inventory.get("scan_error"), str):
                errors.append(f"{rel}: workspace.artifact_inventory.scan_error must be string")
            files = inventory.get("files")
            if not isinstance(files, list):
                errors.append(f"{rel}: workspace.artifact_inventory.files must be a list")
            else:
                for index, item in enumerate(files):
                    if not isinstance(item, dict):
                        errors.append(f"{rel}: workspace.artifact_inventory.files[{index}] must be an object")
                        continue
                    missing = sorted(required_workspace_artifact_file_keys - set(item.keys()))
                    for key in missing:
                        errors.append(f"{rel}: workspace.artifact_inventory.files[{index}] missing key {key}")
                    path = item.get("path")
                    if not isinstance(path, str) or not path:
                        errors.append(f"{rel}: workspace.artifact_inventory.files[{index}].path must be a non-empty string")
                    elif Path(path).is_absolute() or ".." in Path(path).parts:
                        errors.append(
                            f"{rel}: workspace.artifact_inventory.files[{index}].path must be relative to workspace"
                        )
                    if not isinstance(item.get("size_bytes"), int):
                        errors.append(f"{rel}: workspace.artifact_inventory.files[{index}].size_bytes must be an integer")

    permissions = body.get("permissions")
    if isinstance(permissions, dict):
        if not isinstance(permissions.get("remote_worker"), bool):
            errors.append(f"{rel}: permissions.remote_worker must be boolean")
        errors.extend(validate_path_reference_list(permissions.get("writable_roots"), rel, "permissions.writable_roots"))
        if "allowed_writable_roots" in permissions:
            errors.extend(validate_path_reference_list(
                permissions.get("allowed_writable_roots"), rel, "permissions.allowed_writable_roots"
            ))
        turn_sandbox_policy = permissions.get("turn_sandbox_policy")
        if isinstance(turn_sandbox_policy, dict):
            errors.extend(validate_policy_path_references(
                turn_sandbox_policy, rel, "permissions.turn_sandbox_policy"
            ))
    environment = body.get("environment")
    if isinstance(environment, dict):
        java = environment.get("java")
        if not isinstance(java, dict):
            errors.append(f"{rel}: environment.java must be an object")
        else:
            for key in sorted(required_java_environment_keys):
                if not isinstance(java.get(key), str):
                    errors.append(f"{rel}: environment.java.{key} must be string")
            if not java.get("version"):
                errors.append(f"{rel}: environment.java.version must be a non-empty string")
        maven = environment.get("maven")
        if not isinstance(maven, dict):
            errors.append(f"{rel}: environment.maven must be an object")
        else:
            for key in sorted(required_maven_environment_keys):
                if not isinstance(maven.get(key), str):
                    errors.append(f"{rel}: environment.maven.{key} must be string")
        os = environment.get("os")
        if not isinstance(os, dict):
            errors.append(f"{rel}: environment.os must be an object")
        else:
            for key in sorted(required_os_environment_keys):
                if not isinstance(os.get(key), str):
                    errors.append(f"{rel}: environment.os.{key} must be string")
            if not os.get("name"):
                errors.append(f"{rel}: environment.os.name must be a non-empty string")
        process = environment.get("process")
        if not isinstance(process, dict):
            errors.append(f"{rel}: environment.process must be an object")
        elif not isinstance(process.get("available_processors"), int):
            errors.append(f"{rel}: environment.process.available_processors must be an integer")
        spring_profiles = environment.get("spring_profiles")
        if not isinstance(spring_profiles, list) or not all(isinstance(item, str) for item in spring_profiles):
            errors.append(f"{rel}: environment.spring_profiles must be a list of strings")
    retry = body.get("retry")
    if isinstance(retry, dict):
        if not isinstance(retry.get("scheduled"), bool):
            errors.append(f"{rel}: retry.scheduled must be boolean")
        if not isinstance(retry.get("dispatch_kind"), str):
            errors.append(f"{rel}: retry.dispatch_kind must be string")
    effects = body.get("external_effects")
    if isinstance(effects, dict):
        for key in ("tracker_state_claimed", "linear_comment_attempted"):
            if not isinstance(effects.get(key), bool):
                errors.append(f"{rel}: external_effects.{key} must be boolean")
        events = effects.get("events")
        if not isinstance(events, list):
            errors.append(f"{rel}: external_effects.events must be a list")
        else:
            for index, event in enumerate(events):
                if not isinstance(event, dict):
                    errors.append(f"{rel}: external_effects.events[{index}] must be an object")
                    continue
                missing = sorted(required_external_effect_event_keys - set(event.keys()))
                for key in missing:
                    errors.append(f"{rel}: external_effects.events[{index}] missing key {key}")
                for key in sorted(required_external_effect_event_keys):
                    if key in event and not isinstance(event.get(key), str):
                        errors.append(f"{rel}: external_effects.events[{index}].{key} must be string")
                status = event.get("status")
                if isinstance(status, str) and status not in allowed_external_effect_statuses:
                    errors.append(
                        f"{rel}: external_effects.events[{index}].status must be one of {sorted(allowed_external_effect_statuses)}"
                    )
    return errors


def scan_reports(root: Path) -> list[str]:
    return scan_path(root / "docs/reports/agent-runs", root)


def write_fixture(path: Path, unsafe: bool) -> None:
    secret_line = "- token: sk-test-secret-value" if unsafe else "- 外部副作用：无。"
    path.write_text(
        "\n".join(
            [
                "# Agent Run Fixture",
                "",
                "Status: completed",
                "Last Reviewed: 2026-06-10",
                "Review Cadence: 90 days",
                "",
                "## 执行摘要",
                "",
                secret_line,
                "",
            ]
        ),
        encoding="utf-8",
    )


def write_json_fixture(path: Path, invalid_mode: str = "") -> None:
    body = {
        "schema_version": 1,
        "summary_type": "symphony_agent_run",
        "run_id": "run-1",
        "status": "completed",
        "failure_reason": "",
        "failure_category": "none",
        "started_at": "2026-06-10T00:00:00Z",
        "finished_at": "2026-06-10T00:00:01Z",
        "duration_seconds": 1,
        "issue": {
            "id": "issue-1",
            "identifier": "ART-1",
            "title": "Fixture",
            "state": "Done",
        },
        "attempt": {
            "number": 1,
            "retry_attempt": 0,
            "dispatch_kind": "implementation",
            "parent_run_id": "",
            "turn_count": 1,
        },
        "workspace": {
            "path": "workspace/ART-1",
            "key": "ART-1",
            "worker_host": "",
            "artifact_inventory": {
                "max_files": 200,
                "file_count": 1,
                "total_bytes": 12,
                "truncated": False,
                "scan_error": "",
                "files": [
                    {
                        "path": "EVAL_RESULT.md",
                        "size_bytes": 12,
                    }
                ],
            },
        },
        "codex": {
            "session_id": "session-1",
            "last_event": "turn_completed",
            "event_counts": {
                "session_started": 1,
                "thread/tokenUsage/updated": 1,
                "turn_completed": 1,
            },
            "usage": {
                "input_tokens": 1,
                "output_tokens": 2,
                "total_tokens": 3,
            },
        },
        "permissions": {
            "approval_policy": {},
            "thread_sandbox": "workspace-write",
            "turn_sandbox_policy": {
                "type": "workspaceWrite",
                "writableRoots": ["ART-1/1-workspace"],
            },
            "remote_worker": False,
            "network_access": False,
            "writable_roots": ["ART-1/1-workspace"],
            "allowed_writable_roots": ["configured-writable-root/1-cache"],
        },
        "environment": {
            "java": {
                "version": "21.0.8",
                "vendor": "Fixture JDK",
                "runtime_name": "Fixture Runtime",
                "runtime_version": "21.0.8+9",
                "vm_name": "Fixture VM",
                "vm_version": "21.0.8+9",
            },
            "maven": {
                "version": "",
            },
            "os": {
                "name": "FixtureOS",
                "arch": "arm64",
                "version": "1.0",
            },
            "process": {
                "available_processors": 8,
            },
            "spring_profiles": [],
        },
        "retry": {
            "scheduled": False,
            "dispatch_kind": "",
        },
        "external_effects": {
            "tracker_state_claimed": True,
            "linear_comment_attempted": False,
            "events": [
                {
                    "type": "tracker_state_update",
                    "provider": "linear",
                    "target": "issue-1",
                    "status": "succeeded",
                    "error_code": "",
                    "error_message": "",
                    "at": "2026-06-10T00:00:00Z",
                }
            ],
        },
    }
    if invalid_mode == "missing-writable-roots":
        del body["permissions"]["writable_roots"]
    elif invalid_mode == "absolute-writable-roots":
        body["permissions"]["writable_roots"] = ["/tmp/workspace"]
        body["permissions"]["turn_sandbox_policy"]["writableRoots"] = ["/tmp/workspace"]
    path.write_text(json.dumps(body, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


if args == ["--self-test"]:
    with tempfile.TemporaryDirectory() as tmp:
        root = Path(tmp)
        reports_dir = root / "docs/reports/agent-runs"
        reports_dir.mkdir(parents=True)
        write_fixture(reports_dir / "2026-06-10-safe.md", unsafe=False)
        safe_errors = scan_reports(root)
        if safe_errors:
            raise SystemExit(f"safe fixture was incorrectly rejected: {safe_errors}")
        write_fixture(reports_dir / "2026-06-10-unsafe.md", unsafe=True)
        unsafe_errors = scan_reports(root)
        if not unsafe_errors:
            raise SystemExit("unsafe fixture was not detected")
        artifact_dir = root / "artifacts/agent-evals/eval-1/agent-runs"
        artifact_dir.mkdir(parents=True)
        write_fixture(artifact_dir / "safe.md", unsafe=False)
        artifact_safe_errors = scan_path(artifact_dir, root)
        if artifact_safe_errors:
            raise SystemExit(f"safe artifact fixture was incorrectly rejected: {artifact_safe_errors}")
        write_fixture(artifact_dir / "unsafe.md", unsafe=True)
        artifact_unsafe_errors = scan_path(artifact_dir, root)
        if not artifact_unsafe_errors:
            raise SystemExit("unsafe artifact fixture was not detected")
        artifact_json_dir = root / "artifacts/agent-runs"
        artifact_json_dir.mkdir(parents=True)
        write_json_fixture(artifact_json_dir / "valid.json")
        valid_json_errors = scan_path(artifact_json_dir, root)
        if valid_json_errors:
            raise SystemExit(f"valid JSON fixture was incorrectly rejected: {valid_json_errors}")
        write_json_fixture(artifact_json_dir / "invalid.json", "missing-writable-roots")
        invalid_json_errors = scan_path(artifact_json_dir, root)
        if not any("permissions missing key writable_roots" in error for error in invalid_json_errors):
            raise SystemExit(f"invalid JSON fixture did not fail schema-lite validation: {invalid_json_errors}")
        write_json_fixture(artifact_json_dir / "absolute-path.json", "absolute-writable-roots")
        absolute_path_errors = scan_path(artifact_json_dir, root)
        if not any("permissions.writable_roots[0] must be a low-sensitive relative reference" in error
                   for error in absolute_path_errors):
            raise SystemExit(
                f"absolute path JSON fixture did not fail low-sensitive path validation: {absolute_path_errors}"
            )
    print("Agent run summary self-test passed.")
    raise SystemExit(0)

if any(arg == "--self-test" for arg in args):
    raise SystemExit("--self-test cannot be combined with scan paths")

if args:
    errors: list[str] = []
    for arg in args:
        errors.extend(scan_path((repo / arg).resolve() if not Path(arg).is_absolute() else Path(arg), repo))
else:
    errors = scan_reports(repo)
if errors:
    print("Agent run summary check failed:", file=sys.stderr)
    for error in errors:
        print(f"  - {error}", file=sys.stderr)
    raise SystemExit(1)

print("Agent run summary check passed.")
PY
