#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd python3

print_step "Checking agent run summaries"

# 该检查只负责一件不可替代的事：扫描会进入仓库的 agent run 报告，阻止把密钥、令牌、
# 生产连接串等敏感内容沉淀到 docs/reports/agent-runs/。
#
# JSON 摘要的结构契约由 AgentRunSummaryWriter 及其单测（AgentRunSummaryWriterTest）作为
# 唯一事实源保证，不在此处用第二种语言重写 schema，避免双重维护与漂移。
python3 - "$@" <<'PY'
from __future__ import annotations

from pathlib import Path
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
        # JSON 文件也要被纳入敏感信息扫描，而不仅是 Markdown。
        json_dir = root / "artifacts/agent-runs"
        json_dir.mkdir(parents=True)
        (json_dir / "safe.json").write_text(
            '{"summary_type": "symphony_agent_run", "status": "completed"}\n',
            encoding="utf-8",
        )
        json_safe_errors = scan_path(json_dir, root)
        if json_safe_errors:
            raise SystemExit(f"safe JSON fixture was incorrectly rejected: {json_safe_errors}")
        (json_dir / "unsafe.json").write_text(
            '{"note": "token=sk-test-secret-value"}\n',
            encoding="utf-8",
        )
        json_unsafe_errors = scan_path(json_dir, root)
        if not json_unsafe_errors:
            raise SystemExit("unsafe JSON fixture was not detected")
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
