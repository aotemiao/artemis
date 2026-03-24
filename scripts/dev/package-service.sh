#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

usage() {
  echo "Usage: $0 <gateway|auth|system|symphony|all|<domain>> [extra mvn args...]" >&2
}

run_in_repo_root

service="${1:-}"
shift || true

normalized_service="$(normalize_service_name "$service")"

case "$normalized_service" in
  all)
    modules="."
    ;;
  *)
    if ! modules="$(resolve_service_start_module "$normalized_service")"; then
      usage
      exit 1
    fi
    ;;
esac

print_step "Packaging service target: $normalized_service"
if [[ "$modules" == "." ]]; then
  run_mvn -B package "$@"
else
  run_mvn -B -pl "$modules" -am package "$@"
fi

print_step "Package completed for: $normalized_service"
