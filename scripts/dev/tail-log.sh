#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

service="$(normalize_service_name "${1:-}")"

if [[ -z "$service" ]]; then
  echo "Usage: $0 <gateway|auth|system|<domain>>" >&2
  exit 1
fi

if service_catalog_has "$service"; then
  log_file="$(service_catalog_field "$service" log_file)"
else
  log_file="logs/artemis-${service}.log"
fi

if [[ -z "$log_file" ]]; then
  echo "No file log is configured for service: $service" >&2
  exit 1
fi

if [[ ! -f "$log_file" ]]; then
  echo "Log file not found: $log_file" >&2
  exit 1
fi

tail -f "$log_file"
