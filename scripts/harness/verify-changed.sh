#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd git

mode="${1:-staged}"
shift_args=()

if [[ "$mode" == "staged" ]]; then
  changed_files="$(git diff --cached --name-only)"
elif [[ "$mode" == "working-tree" ]]; then
  changed_files="$(
    {
      git diff --name-only
      git ls-files --others --exclude-standard
    } | sort -u
  )"
elif [[ "$mode" == "range" ]]; then
  base_ref="${2:-}"
  head_ref="${3:-HEAD}"
  if [[ -z "$base_ref" ]]; then
    echo "Usage: $0 [staged|working-tree|range <base-ref> [head-ref]]" >&2
    exit 1
  fi
  changed_files="$(git diff --name-only "$base_ref" "$head_ref")"
  shift_args=("$base_ref" "$head_ref")
else
  echo "Usage: $0 [staged|working-tree|range <base-ref> [head-ref]]" >&2
  exit 1
fi

if [[ -z "$changed_files" ]]; then
  print_step "No changed files detected"
  echo "Nothing to verify in mode: $mode"
  exit 0
fi

print_step "Checking OpenSpec sync"
if ((${#shift_args[@]} > 0)); then
  scripts/harness/check-openspec-sync.sh "$mode" "${shift_args[@]}"
else
  scripts/harness/check-openspec-sync.sh "$mode"
fi

print_step "Running governance checks"
# 按改动文件类型推导治理 scope：只动代码 → code，只动文档/规范 → docs，混合或出现
# 其它类型（脚本、配置等）→ all。任何无法明确归类的改动都回退到 all，确保不漏检。
gov_scope="all"
has_code=0
has_docs=0
has_other=0
while IFS= read -r file; do
  [[ -z "$file" ]] && continue
  case "$file" in
    *.java | */pom.xml | pom.xml) has_code=1 ;;
    *.md | openspec/* | docs/*) has_docs=1 ;;
    *) has_other=1 ;;
  esac
done <<< "$changed_files"
if ((has_other == 0)); then
  if ((has_code == 1 && has_docs == 0)); then
    gov_scope="code"
  elif ((has_docs == 1 && has_code == 0)); then
    gov_scope="docs"
  fi
fi
scripts/harness/run-governance-checks.sh "$gov_scope"

changed_java="$(printf "%s\n" "$changed_files" | grep -E '\.java$' || true)"
changed_poms="$(printf "%s\n" "$changed_files" | grep -E '(^|/)pom\.xml$' || true)"

modules=""
if [[ -n "$changed_java" ]]; then
  while IFS= read -r file; do
    [[ -z "$file" ]] && continue
    [[ ! -f "$file" ]] && continue
    case "$file" in
      *"/src/"*) modules="$modules ${file%%/src/*}" ;;
      *) modules="$modules ${file%%/*}" ;;
    esac
  done <<< "$changed_java"
fi

if [[ -n "$changed_poms" ]]; then
  while IFS= read -r file; do
    [[ -z "$file" ]] && continue
    if [[ "$file" == "pom.xml" ]]; then
      modules="$modules ."
    elif [[ ! -f "$file" ]]; then
      continue
    else
      modules="$modules ${file%/pom.xml}"
    fi
  done <<< "$changed_poms"
fi

modules="$(echo "$modules" | tr ' ' '\n' | sed '/^$/d' | sort -u | tr '\n' ',' | sed 's/,$//')"

if [[ -z "$modules" ]]; then
  print_step "No changed Java or pom.xml files detected"
  echo "Governance checks completed for mode: $mode"
  exit 0
fi

print_step "Running module-scoped verify for: $modules"
run_mvn -B -pl "$modules" -am verify

print_step "Changed-scope verify completed"
