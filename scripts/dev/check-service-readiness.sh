#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

usage() {
  echo "Usage: $0 <system|auth|gateway|symphony> [attempts] [sleep_seconds]" >&2
}

run_in_repo_root

service="${1:-}"
attempts="${2:-30}"
sleep_seconds="${3:-2}"

case "$service" in
  system)
    url="http://127.0.0.1:9300/api/lookup-types?page=0&size=1"
    expected="200"
    method="GET"
    log_file="logs/artemis-system.log"
    log_hint="scripts/dev/tail-log.sh system"
    ;;
  auth)
    url="http://127.0.0.1:9200/auth/refresh"
    expected="200,401,403"
    method="POST"
    log_file="logs/artemis-auth.log"
    log_hint="scripts/dev/tail-log.sh auth"
    ;;
  gateway)
    url="http://127.0.0.1:8080/auth/refresh"
    expected="200,401,403"
    method="POST"
    log_file="logs/artemis-gateway.log"
    log_hint="scripts/dev/tail-log.sh gateway"
    ;;
  symphony)
    url="http://127.0.0.1:9500/api/v1/state"
    expected="200"
    method="GET"
    log_file=""
    log_hint="重新执行 scripts/dev/run-symphony.sh，并观察当前终端输出"
    ;;
  *)
    usage
    exit 1
    ;;
esac

scripts/dev/check-service-config.sh "$service"

print_step "Waiting for $service readiness"
if scripts/dev/wait-http.sh "$url" "$expected" "$attempts" "$sleep_seconds" "$method"; then
  print_step "Service readiness check completed for $service"
  exit 0
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
