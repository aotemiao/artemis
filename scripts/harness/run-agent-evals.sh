#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd python3

print_step "Running agent workflow eval fixtures"

python3 - <<'PY'
from pathlib import Path
import re
import sys

repo = Path.cwd()
fixtures = sorted((repo / "docs/agent-evals/fixtures").glob("*.yml"))
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
errors: list[str] = []

if not fixtures:
    errors.append("docs/agent-evals/fixtures: no eval fixtures found")

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
        if stripped.startswith("- "):
            existing = data.setdefault(current_key, [])
            if not isinstance(existing, list):
                errors.append(f"{path.relative_to(repo)}: key {current_key} mixes scalar and list values")
                continue
            existing.append(stripped[2:].strip())
        elif isinstance(data.get(current_key), str):
            data[current_key] = f"{data[current_key]}\n{stripped}".strip()
    return data

for path in fixtures:
    rel = path.relative_to(repo)
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

if errors:
    print("Agent eval fixture check failed:", file=sys.stderr)
    for error in errors:
        print(f"  - {error}", file=sys.stderr)
    raise SystemExit(1)

print(f"Agent eval fixture check passed ({len(fixtures)} fixtures).")
PY
