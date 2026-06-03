#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

print_step "Checking capability package structure"

violations=0

report_violation() {
  local file="$1"
  local reason="$2"
  echo "Capability package violation: ${file} (${reason})" >&2
  violations=1
}

while IFS= read -r file; do
  [[ -z "$file" ]] && continue

  path="${file#./}"
  name="${path##*/}"

  [[ "$name" == "package-info.java" ]] && continue
  [[ "$path" =~ ^artemis-modules/.+/src/(main|test)/java/ ]] || continue

  if [[ "$path" =~ /(adapter/(web|dubbo)|adapter/web/dto|client/(api|dto))/([^/]+\.java)$ ]]; then
    report_violation "$path" "adapter/client 分类根包不得平铺具体业务类"
  fi

  if [[ "$path" =~ /app/(command|query)/([^/]+\.java)$ ]]; then
    report_violation "$path" "app command/query 具体类必须进入业务能力子包"
  fi

  if [[ "$path" =~ /(domain/(model|gateway|service|event)|infra/(gateway|repository|dataobject|converter|mapper))/([^/]+\.java)$ ]]; then
    report_violation "$path" "domain/infra 分类根包不得平铺具体业务类"
  fi
done < <(find artemis-modules -path "*/target/*" -prune -o -path "*/src/*/java/*" -name "*.java" -print)

if [[ "$violations" -ne 0 ]]; then
  exit 1
fi

print_step "Capability package structure check passed"
