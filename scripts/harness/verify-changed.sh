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
  changed_files="$(git diff --name-only)"
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

print_step "Checking OpenSpec sync"
scripts/harness/check-openspec-sync.sh "$mode" "${shift_args[@]}"

changed_java="$(printf "%s\n" "$changed_files" | grep -E '\.java$' || true)"
changed_poms="$(printf "%s\n" "$changed_files" | grep -E '(^|/)pom\.xml$' || true)"

modules=""
if [[ -n "$changed_java" ]]; then
  while IFS= read -r file; do
    [[ -z "$file" ]] && continue
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
    else
      modules="$modules ${file%/pom.xml}"
    fi
  done <<< "$changed_poms"
fi

modules="$(echo "$modules" | tr ' ' '\n' | sed '/^$/d' | sort -u | tr '\n' ',' | sed 's/,$//')"

if [[ -z "$modules" ]]; then
  print_step "No changed Java or pom.xml files detected"
  echo "Nothing to verify in mode: $mode"
  exit 0
fi

print_step "Running module-scoped verify for: $modules"
run_mvn -B -pl "$modules" -am verify

print_step "Changed-scope verify completed"
