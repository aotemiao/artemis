#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd python3

python3 - <<'PY'
from pathlib import Path
import sys

repo = Path.cwd()
base = repo / "docs/harness-engineering/quality-issues"
required = [
    repo / "docs/harness-engineering/QUALITY_ISSUE_STANDARD.md",
    base / "README.md",
    base / "active/README.md",
    base / "archive/README.md",
]


def exact_exists(path: Path) -> bool:
    try:
        rel = path.resolve().relative_to(repo.resolve())
    except ValueError:
        return path.exists()

    current = repo.resolve()
    for part in rel.parts:
        try:
            names = {child.name for child in current.iterdir()}
        except FileNotFoundError:
            return False
        if part not in names:
            return False
        current = current / part
    return True

missing = [path for path in required if not exact_exists(path)]
if missing:
    print("Quality issue archive check failed:", file=sys.stderr)
    for path in missing:
        print(f"  - missing required file: {path.relative_to(repo)}", file=sys.stderr)
    raise SystemExit(1)

archive_files = [
    path for path in (base / "archive").glob("*.md") if path.name != "README.md"
]
for path in archive_files:
    text = path.read_text(encoding="utf-8")
    for marker in ("## 关闭条件", "## 验证", "## 关闭日期"):
        if marker not in text:
            print(f"{path.relative_to(repo)}: missing marker {marker}", file=sys.stderr)
            raise SystemExit(1)

print("Quality issue archive check passed.")
PY
