#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd python3

print_step "Checking feature specs"

required_paths=(
  "docs/feature-specs/README.md"
  "docs/feature-specs/templates/feature-spec-template.md"
  "docs/feature-specs/examples/menu-permission-feature-spec.md"
  "docs/patterns/README.md"
  "docs/patterns/spec-to-validation-mapping.md"
  "docs/patterns/agent-delivery-handoff.md"
)

for path in "${required_paths[@]}"; do
  require_repo_path_exact "$path"
done

python3 - <<'PY'
from pathlib import Path
import re
import sys

repo = Path.cwd()
base = repo / "docs/feature-specs"
required_sections = [
    "## 背景",
    "## 目标",
    "## 非目标",
    "## 用户故事",
    "## 业务规则",
    "## 数据与接口影响",
    "## 验收标准",
    "## 验证映射",
    "## 关联资产",
]
modern_required_sections = [
    "## 异常与风险场景",
    "## 工程风险评估",
]

missing = []
ac_pattern = re.compile(r"\|\s*(AC-\d+)\s*\|")
for path in sorted(base.rglob("*.md")):
    rel = path.relative_to(repo)
    text = path.read_text(encoding="utf-8")
    is_template = "templates/" in rel.as_posix()

    if not re.search(r"^Status:\s+\S.*$", text[:500], re.MULTILINE):
        missing.append(f"{rel}: missing Status metadata")
    if is_template:
        if "Last Reviewed: YYYY-MM-DD" not in text[:500]:
            missing.append(f"{rel}: template must keep Last Reviewed placeholder")
        if "Review Cadence: 90 days" not in text[:500]:
            missing.append(f"{rel}: template must declare Review Cadence placeholder")
    else:
        if not re.search(r"^Last Reviewed:\s+\d{4}-\d{2}-\d{2}$", text[:500], re.MULTILINE):
            missing.append(f"{rel}: missing Last Reviewed metadata")
        if not re.search(r"^Review Cadence:\s+\d+\s+days$", text[:500], re.MULTILINE):
            missing.append(f"{rel}: missing Review Cadence metadata")

    if is_template:
        continue
    if path.name == "README.md":
        continue
    for section in required_sections:
        if section not in text:
            missing.append(f"{rel}: missing section {section}")
    is_completed = "completed/" in rel.as_posix()
    if not is_completed:
        for section in modern_required_sections:
            if section not in text:
                missing.append(f"{rel}: missing section {section}")
    if "| 验收编号 | 验证入口 |" not in text:
        missing.append(f"{rel}: missing validation mapping table")
    acceptance = text.split("## 验收标准", 1)[1].split("## 验证映射", 1)[0]
    validation = text.split("## 验证映射", 1)[1].split("## 关联资产", 1)[0]
    acceptance_ids = sorted(set(ac_pattern.findall(acceptance)))
    validation_ids = sorted(set(ac_pattern.findall(validation)))
    if not acceptance_ids:
        missing.append(f"{rel}: no AC-* acceptance criteria found")
    for ac_id in acceptance_ids:
        if ac_id not in validation_ids:
            missing.append(f"{rel}: acceptance criterion {ac_id} is not mapped in ## 验证映射")
    for line in validation.splitlines():
        if not line.strip().startswith("| AC-"):
            continue
        cells = [cell.strip() for cell in line.strip().strip("|").split("|")]
        if len(cells) < 3:
            missing.append(f"{rel}: malformed validation mapping row: {line.strip()}")
            continue
        if not cells[1] or cells[1] in {"-", "待定", "TBD", "<command-or-test>"}:
            missing.append(f"{rel}: validation entry missing for {cells[0]}")
        if not cells[2] or cells[2] in {"-", "待定", "TBD", "<期望输出或断言>"}:
            missing.append(f"{rel}: pass criteria missing for {cells[0]}")

if missing:
    print("Feature spec check failed:", file=sys.stderr)
    for item in missing:
        print(f"  - {item}", file=sys.stderr)
    raise SystemExit(1)

print("Feature spec check passed.")
PY
