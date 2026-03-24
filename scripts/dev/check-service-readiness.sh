#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

usage() {
  echo "Usage: $0 <system|auth|gateway|symphony|<domain>> [attempts] [sleep_seconds]" >&2
}

run_in_repo_root

service="$(normalize_service_name "${1:-}")"
attempts="${2:-30}"
sleep_seconds="${3:-2}"
smoke_script=""
log_file=""
log_hint=""
url=""
expected=""
method=""

if service_catalog_has "$service"; then
  smoke_script="$(service_catalog_field "$service" smoke_script)"
  log_file="$(service_catalog_field "$service" log_file)"
  log_hint="scripts/dev/tail-log.sh ${service}"
  if [[ "$(service_catalog_field "$service" readiness_mode)" == "http" ]]; then
    url="$(service_catalog_field "$service" readiness_url)"
    expected="$(service_catalog_field "$service" readiness_expected)"
    method="$(service_catalog_field "$service" readiness_method)"
    smoke_script=""
  fi
  if [[ "$service" == "symphony" ]]; then
    log_hint="重新执行 scripts/dev/run-symphony.sh，并观察当前终端输出"
  fi
else
  smoke_script="scripts/smoke/${service}-ping.sh"
  if [[ ! -f "$smoke_script" ]]; then
    usage
    exit 1
  fi
  log_file="logs/artemis-${service}.log"
  log_hint="scripts/dev/tail-log.sh ${service}"
fi

scripts/dev/check-service-config.sh "$service"

if [[ -n "$smoke_script" ]]; then
  print_step "Running readiness smoke for $service"
  if "$smoke_script"; then
    print_step "Service readiness check completed for $service"
    exit 0
  fi
else
print_step "Waiting for $service readiness"
if scripts/dev/wait-http.sh "$url" "$expected" "$attempts" "$sleep_seconds" "$method"; then
  print_step "Service readiness check completed for $service"
  exit 0
fi
fi

echo "Service readiness failed for $service." >&2
if [[ -n "$log_file" && -f "$log_file" ]]; then
  echo "--- recent log tail: $log_file ---" >&2
  tail -n 40 "$log_file" >&2 || true
  echo "--- end log tail ---" >&2
else
  echo "Log file is unavailable. Use: $log_hint" >&2
fi
exit 1
