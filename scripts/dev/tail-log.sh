#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

service="${1:-}"

case "$service" in
  gateway) log_file="logs/artemis-gateway.log" ;;
  auth) log_file="logs/artemis-auth.log" ;;
  system) log_file="logs/artemis-system.log" ;;
  *)
    echo "Usage: $0 <gateway|auth|system>" >&2
    exit 1
    ;;
esac

if [[ ! -f "$log_file" ]]; then
  echo "Log file not found: $log_file" >&2
  exit 1
fi

tail -f "$log_file"
