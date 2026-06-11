#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd git
require_cmd python3

python3 - <<'PY'
from pathlib import Path
import re
import subprocess
import sys

REPO = Path.cwd()
CHANGES_DIR = REPO / "openspec/changes"
ARCHIVE_DIR = CHANGES_DIR / "archive"


def exact_exists(path: Path) -> bool:
    try:
        rel = path.resolve().relative_to(REPO.resolve())
    except ValueError:
        return path.exists()

    current = REPO.resolve()
    for part in rel.parts:
        try:
            names = {child.name for child in current.iterdir()}
        except FileNotFoundError:
            return False
        if part not in names:
            return False
        current = current / part
    return True


def rel(path: Path) -> str:
    return str(path.relative_to(REPO))


def task_lines(tasks_text: str) -> list[str]:
    return [
        line
        for line in tasks_text.splitlines()
        if re.search(r"^\s*-\s+\[[ xX]\]\s+", line)
    ]


def unchecked_task_lines(tasks_text: str) -> list[str]:
    return [
        line
        for line in task_lines(tasks_text)
        if re.search(r"^\s*-\s+\[\s\]\s+", line)
    ]


errors: list[str] = []

if CHANGES_DIR.exists():
    active_changes = [
        path
        for path in sorted(CHANGES_DIR.iterdir())
        if path.is_dir() and path.name != "archive"
    ]
else:
    active_changes = []

for change in active_changes:
    proposal = change / "proposal.md"
    tasks = change / "tasks.md"
    if not exact_exists(proposal):
        errors.append(f"{rel(change)}: missing required proposal.md")
    if not exact_exists(tasks):
        errors.append(f"{rel(change)}: missing required tasks.md")
        continue

    tasks_text = tasks.read_text(encoding="utf-8")
    if not task_lines(tasks_text):
        errors.append(f"{rel(tasks)}: missing checkbox tasks")
        continue
    if not unchecked_task_lines(tasks_text):
        archive_target = ARCHIVE_DIR / f"YYYY-MM-DD-{change.name}"
        errors.append(
            f"{rel(change)}: all tasks are checked; archive it under "
            f"{rel(archive_target)}"
        )

if ARCHIVE_DIR.exists():
    output = subprocess.check_output(
        ["git", "ls-files", "--others", "--exclude-standard", "--", rel(ARCHIVE_DIR)],
        cwd=REPO,
        text=True,
    )
    untracked_archive_files = [line for line in output.splitlines() if line.strip()]
    deleted_output = subprocess.check_output(
        ["git", "ls-files", "--deleted", "--", rel(CHANGES_DIR)],
        cwd=REPO,
        text=True,
    )
    deleted_files = {line for line in deleted_output.splitlines() if line.strip()}
    for item in untracked_archive_files:
        archive_rel = Path(item).relative_to(rel(ARCHIVE_DIR))
        archive_change = archive_rel.parts[0]
        source_change = re.sub(r"^\d{4}-\d{2}-\d{2}-", "", archive_change)
        source_path = Path(rel(CHANGES_DIR)) / source_change / Path(*archive_rel.parts[1:])
        if str(source_path) in deleted_files:
            continue
        errors.append(f"{item}: archive file is untracked")

if errors:
    print("OpenSpec change state check failed:", file=sys.stderr)
    for error in errors:
        print(f"  - {error}", file=sys.stderr)
    sys.exit(1)

if active_changes:
    print(f"OpenSpec change state check passed ({len(active_changes)} active change(s)).")
else:
    print("OpenSpec change state check passed (no active changes).")
PY
