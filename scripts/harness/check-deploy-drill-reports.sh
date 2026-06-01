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
import sys

repo = Path.cwd()
base = repo / "docs/reports/deploy-drills"
required_sections = [
    "## 演练范围",
    "## 执行命令",
    "## 验证结果",
    "## 结论",
]

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

if failures:
    print("Deploy drill report check failed:", file=sys.stderr)
    for item in failures:
        print(f"  - {item}", file=sys.stderr)
    raise SystemExit(1)

print("Deploy drill report check passed.")
PY
