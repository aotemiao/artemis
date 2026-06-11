#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd python3

print_step "Checking deploy drill reports"

require_repo_path_exact "docs/reports/deploy-drills/README.md"
require_repo_path_exact "docs/reports/deploy-drills/2026-06-01-sample-report-template.md"

python3 - <<'PY'
from pathlib import Path
import json
import re
import sys

repo = Path.cwd()
base = repo / "docs/reports/deploy-drills"
required_sections = [
    "## 演练范围",
    "## 执行命令",
    "## 验证结果",
    "## 指标摘要",
    "## 结论",
]
allowed_kinds = {"deploy", "rollback"}
allowed_statuses = {"completed", "failed", "partial", "planned"}
allowed_smoke = {"passed", "failed", "skipped", "pending", "partial"}
required_summary_keys = {
    "schema_version",
    "summary_type",
    "kind",
    "service",
    "services",
    "status",
    "smoke",
    "rollback",
    "failure_stage",
}


def load_summary(path: Path, text: str) -> tuple[dict | None, str | None]:
    for match in re.finditer(r"```json\s*(\{.*?\})\s*```", text, re.DOTALL):
        try:
            body = json.loads(match.group(1))
        except json.JSONDecodeError as exc:
            return None, f"invalid JSON summary ({exc})"
        if body.get("summary_type") == "deploy_drill_report":
            return body, None
    return None, "missing deploy_drill_report JSON summary"


def validate_summary(path: Path, body: dict) -> list[str]:
    errors = []
    missing = sorted(required_summary_keys - set(body.keys()))
    for key in missing:
        errors.append(f"missing summary key {key}")
    if body.get("schema_version") != 1:
        errors.append("summary schema_version must be 1")
    if body.get("summary_type") != "deploy_drill_report":
        errors.append("summary_type must be deploy_drill_report")
    if body.get("kind") not in allowed_kinds:
        errors.append(f"summary kind must be one of {sorted(allowed_kinds)}")
    if body.get("status") not in allowed_statuses:
        errors.append(f"summary status must be one of {sorted(allowed_statuses)}")
    if body.get("smoke") not in allowed_smoke:
        errors.append(f"summary smoke must be one of {sorted(allowed_smoke)}")
    if not isinstance(body.get("service"), str) or not body.get("service").strip():
        errors.append("summary service must be a non-empty string")
    services = body.get("services")
    if not isinstance(services, list) or not services or not all(isinstance(item, str) and item for item in services):
        errors.append("summary services must be a non-empty list of strings")
    if not isinstance(body.get("rollback"), bool):
        errors.append("summary rollback must be boolean")
    if not isinstance(body.get("failure_stage"), str):
        errors.append("summary failure_stage must be string")
    return errors

failures = []
for path in sorted(base.glob("*.md")):
    if path.name == "README.md":
        continue
    text = path.read_text(encoding="utf-8")
    if "# Deploy Drill Report" not in text and "# Rollback Drill Report" not in text:
        failures.append(f"{path.relative_to(repo)}: missing report title")
    for section in required_sections:
        if section not in text:
            failures.append(f"{path.relative_to(repo)}: missing section {section}")
    if "| 检查项 | 命令或证据 | 结果 |" not in text and "| 服务 | 配置检查 | 打包 | 镜像 | smoke | 日志 |" not in text:
        failures.append(f"{path.relative_to(repo)}: missing validation result table")
    summary, error = load_summary(path, text)
    if error:
        failures.append(f"{path.relative_to(repo)}: {error}")
        continue
    if summary is not None:
        for item in validate_summary(path, summary):
            failures.append(f"{path.relative_to(repo)}: {item}")

if failures:
    print("Deploy drill report check failed:", file=sys.stderr)
    for item in failures:
        print(f"  - {item}", file=sys.stderr)
    raise SystemExit(1)

print("Deploy drill report check passed.")
PY
