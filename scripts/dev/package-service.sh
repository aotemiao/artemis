#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

usage() {
  echo "Usage: $0 <gateway|auth|system|symphony|all> [extra mvn args...]" >&2
}

run_in_repo_root

service="${1:-}"
shift || true

case "$service" in
  gateway)
    modules="artemis-gateway"
    ;;
  auth)
    modules="artemis-auth"
    ;;
  system)
    modules="artemis-modules/artemis-system/artemis-system-start"
    ;;
  symphony)
    modules="artemis-symphony/artemis-symphony-start"
    ;;
  all)
    modules="."
    ;;
  *)
    usage
    exit 1
    ;;
esac

print_step "Packaging service target: $service"
if [[ "$modules" == "." ]]; then
  run_mvn -B package "$@"
else
  run_mvn -B -pl "$modules" -am package "$@"
fi

print_step "Package completed for: $service"
