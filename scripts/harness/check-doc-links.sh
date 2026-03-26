#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd python3

python3 - <<'PY'
from pathlib import Path
import re
import sys

repo = Path.cwd()
pattern = re.compile(r'\[[^\]]+\]\(([^)]+)\)')
errors = []


def exact_exists(path: Path) -> bool:
    try:
        rel = path.relative_to(repo)
    except ValueError:
        return path.exists()

    current = repo
    for part in rel.parts:
        try:
            names = {child.name for child in current.iterdir()}
        except FileNotFoundError:
            return False
        if part not in names:
            return False
        current = current / part
    return True

for md in repo.rglob("*.md"):
    if ".git" in md.parts:
        continue
    text = md.read_text(encoding="utf-8")
    for target in pattern.findall(text):
        if target.startswith(("http://", "https://", "mailto:")):
            continue
        if target.startswith("#"):
            continue
        clean = target.split("#", 1)[0]
        if not clean:
            continue
        resolved = (md.parent / clean).resolve()
        if not exact_exists(resolved):
            errors.append(f"{md.relative_to(repo)} -> {target}")

if errors:
    print("Broken markdown links detected:", file=sys.stderr)
    for item in errors:
        print(f"  - {item}", file=sys.stderr)
    sys.exit(1)

print("Markdown links check passed.")
PY
