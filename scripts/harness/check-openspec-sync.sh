#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd git

usage() {
  echo "Usage: $0 [staged|working-tree|range <base-ref> [head-ref]]" >&2
}

strict="${OPENSPEC_STRICT:-0}"
mode="${1:-staged}"

if [[ "$mode" == "staged" ]]; then
  changed_files="$(git diff --cached --name-only)"
elif [[ "$mode" == "working-tree" ]]; then
  changed_files="$(git diff --name-only)"
elif [[ "$mode" == "range" ]]; then
  base_ref="${2:-}"
  head_ref="${3:-HEAD}"
  if [[ -z "$base_ref" ]]; then
    usage
    exit 1
  fi
  changed_files="$(git diff --name-only "$base_ref" "$head_ref")"
else
  usage
  exit 1
fi

openspec_changes_dir="openspec/changes"
if [[ ! -d "$openspec_changes_dir" ]]; then
  echo "No openspec/changes directory; skipping."
  exit 0
fi

active_changes=()
while IFS= read -r dir; do
  [[ -z "$dir" ]] && continue
  active_changes+=("$dir")
done < <(find "$openspec_changes_dir" -mindepth 1 -maxdepth 1 -type d ! -name archive | sort)

if [[ "${#active_changes[@]}" -eq 0 ]]; then
  echo "No active OpenSpec changes."
  exit 0
fi

if [[ -z "$changed_files" ]]; then
  echo "No changed files detected for mode: $mode"
  exit 0
fi

touched_openspec=0
while IFS= read -r file; do
  [[ -z "$file" ]] && continue
  for dir in "${active_changes[@]}"; do
    case "$file" in
      "$dir"/*)
        touched_openspec=1
        break
        ;;
    esac
  done
  [[ "$touched_openspec" -eq 1 ]] && break
done <<< "$changed_files"

if [[ "$touched_openspec" -eq 1 ]]; then
  echo "OpenSpec is touched together with the current changes."
  exit 0
fi

echo "---"
echo "OpenSpec: 存在进行中变更，但当前改动未包含其目录下文件。"
echo "请确认是否需要同步更新对应的 design/tasks/spec。"
echo "规则见 openspec/docs/pre-commit-openspec-sync-rule.md"
echo "---"

if [[ "$strict" == "1" ]]; then
  exit 1
fi
