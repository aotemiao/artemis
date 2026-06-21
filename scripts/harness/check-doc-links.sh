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
markdown_link_pattern = re.compile(r'\[[^\]]+\]\(([^)]+)\)')
inline_code_pattern = re.compile(r'(?<!`)`([^`\n]+)`(?!`)')
inline_repo_path_pattern = re.compile(r'(?<![\w./-])((?:docs|scripts)/[^\s`),;，。；：]+)')
errors = []

CURRENT_STATE_DOCS = {
    Path("AGENTS.md"),
    Path("ARCHITECTURE.md"),
    Path("README.md"),
    Path("QUALITY_SCORE.md"),
    Path("docs/README.md"),
    Path("docs/exec-plans/README.md"),
    Path("docs/feature-specs/README.md"),
}

CURRENT_STATE_DOC_DIRS = (
    Path("docs/agent-workflow"),
    Path("docs/governance"),
    Path("docs/reports"),
)


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


def is_current_state_doc(path: Path) -> bool:
    rel = path.relative_to(repo)
    if rel in CURRENT_STATE_DOCS:
        return True
    for current_dir in CURRENT_STATE_DOC_DIRS:
        if rel.parent == current_dir:
            return True
    return False


def iter_inline_repo_paths(code: str):
    for match in inline_repo_path_pattern.finditer(code):
        candidate = match.group(1).strip().rstrip("./,;:，。；：")
        candidate = candidate.split("#", 1)[0]
        if not candidate:
            continue
        # 现状文档里的占位符示例不是可验证路径。
        if any(token in candidate for token in ("<", ">", "{", "}", "$", "*", "...")):
            continue
        yield candidate


def iter_inline_code_lines(text: str):
    in_fence = False
    for line_number, line in enumerate(text.splitlines(), start=1):
        stripped = line.lstrip()
        if stripped.startswith(("```", "~~~")):
            in_fence = not in_fence
            continue
        if in_fence:
            continue
        yield line_number, line

for md in repo.rglob("*.md"):
    if ".git" in md.parts:
        continue
    text = md.read_text(encoding="utf-8")
    for target in markdown_link_pattern.findall(text):
        if target.startswith(("http://", "https://", "mailto:")):
            continue
        if target.startswith("#"):
            continue
        if target.startswith("/"):
            errors.append(f"{md.relative_to(repo)} -> {target} (absolute filesystem path is not allowed)")
            continue
        clean = target.split("#", 1)[0]
        if not clean:
            continue
        resolved = (md.parent / clean).resolve()
        if not exact_exists(resolved):
            errors.append(f"{md.relative_to(repo)} -> {target}")

    if is_current_state_doc(md):
        for line_number, line in iter_inline_code_lines(text):
            for code in inline_code_pattern.findall(line):
                for inline_path in iter_inline_repo_paths(code):
                    resolved = (repo / inline_path).resolve()
                    if not exact_exists(resolved):
                        errors.append(f"{md.relative_to(repo)}:{line_number} -> `{inline_path}`")

if errors:
    print("Broken markdown links or inline repository paths detected:", file=sys.stderr)
    for item in errors:
        print(f"  - {item}", file=sys.stderr)
    sys.exit(1)

print("Markdown links and inline repository paths check passed.")
PY
