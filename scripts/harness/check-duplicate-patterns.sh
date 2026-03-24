#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd python3

python3 - <<'PY'
from collections import defaultdict
from pathlib import Path
import hashlib
import re
import sys

REPO = Path.cwd()
JAVA_SUFFIXES = ("DTO.java", "Request.java", "Response.java", "ExceptionHandler.java")
SHELL_SUFFIX = ".sh"


def should_skip(path: Path) -> bool:
    return any(part in {".git", "target"} for part in path.parts)


def normalize_java(text: str) -> str:
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.S)
    text = re.sub(r"^\s*//.*$", "", text, flags=re.M)
    text = re.sub(r"^\s*package\s+.*?;$", "", text, flags=re.M)
    text = re.sub(r"^\s*import\s+.*?;$", "", text, flags=re.M)
    text = re.sub(r"\b(class|record|interface)\s+\w+", r"\1 TYPE_NAME", text)
    return " ".join(text.split())


def normalize_shell(text: str) -> str:
    lines = []
    for line in text.splitlines():
        stripped = line.strip()
        if not stripped or stripped.startswith("#!"):
            continue
        if stripped.startswith("#"):
            continue
        lines.append(stripped)
    return " ".join(lines)


groups: dict[str, list[Path]] = defaultdict(list)

for path in REPO.rglob("*"):
    if not path.is_file() or should_skip(path):
        continue
    rel = path.relative_to(REPO)
    if path.name.endswith(JAVA_SUFFIXES):
        normalized = normalize_java(path.read_text(encoding="utf-8"))
    elif "scripts" in rel.parts and path.suffix == SHELL_SUFFIX:
        normalized = normalize_shell(path.read_text(encoding="utf-8"))
    else:
        continue
    if not normalized:
        continue
    digest = hashlib.sha256(normalized.encode("utf-8")).hexdigest()
    groups[digest].append(rel)

duplicates = [sorted(paths) for paths in groups.values() if len(paths) > 1]

if duplicates:
    print("Duplicate implementation patterns detected:", file=sys.stderr)
    for group in sorted(duplicates):
        print("  - " + ", ".join(str(path) for path in group), file=sys.stderr)
    sys.exit(1)

print("Duplicate pattern scan passed.")
PY
