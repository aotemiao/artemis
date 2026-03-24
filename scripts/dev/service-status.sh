#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

usage() {
  echo "Usage: $0 [all|<service>]" >&2
}

run_in_repo_root
require_cmd python3

target="${1:-all}"

collect_status() {
  local service="$1"
  local port
  port="$(service_catalog_field "$service" port 2>/dev/null || true)"
  local config_files
  config_files="$(service_catalog_field "$service" config_files 2>/dev/null || true)"
  local smoke_script
  smoke_script="$(service_catalog_field "$service" smoke_script 2>/dev/null || true)"
  local log_file
  log_file="$(service_catalog_field "$service" log_file 2>/dev/null || true)"
  local readiness_mode
  readiness_mode="$(service_catalog_field "$service" readiness_mode 2>/dev/null || true)"

  local runtime_state="unknown"
  if [[ -n "$port" ]] && nc -z 127.0.0.1 "$port" >/dev/null 2>&1; then
    runtime_state="port-open"
  else
    runtime_state="stopped"
  fi

  printf '%s\t%s\t%s\t%s\t%s\t%s\n' \
    "$service" \
    "$runtime_state" \
    "$port" \
    "$readiness_mode" \
    "$smoke_script" \
    "$log_file"
}

print_step "Collecting service status"
if [[ "$target" == "all" ]]; then
  while IFS= read -r service; do
    [[ -z "$service" ]] && continue
    collect_status "$service"
  done < <(service_catalog_names)
else
  target="$(normalize_service_name "$target")"
  if ! service_catalog_has "$target"; then
    usage
    exit 1
  fi
  collect_status "$target"
fi | python3 - <<'PY'
import sys

rows = [line.rstrip("\n").split("\t") for line in sys.stdin if line.strip()]
headers = ["service", "runtime", "port", "readiness", "smoke", "log"]
widths = [len(h) for h in headers]
for row in rows:
    for i, value in enumerate(row):
        widths[i] = max(widths[i], len(value))

def fmt(row):
    return "  ".join(value.ljust(widths[i]) for i, value in enumerate(row))

print(fmt(headers))
print("  ".join("-" * widths[i] for i in range(len(headers))))
for row in rows:
    print(fmt(row))
PY
