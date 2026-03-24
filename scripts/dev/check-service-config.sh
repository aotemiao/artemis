#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

usage() {
  echo "Usage: $0 <system|auth|gateway|symphony>" >&2
}

run_in_repo_root

service="${1:-}"
case "$service" in
  system)
    required_files=(
      "config/nacos/application-common.yml"
      "config/nacos/datasource.yml"
      "config/nacos/artemis-system.yml"
    )
    ;;
  auth)
    required_files=(
      "config/nacos/application-common.yml"
      "config/nacos/artemis-auth.yml"
    )
    ;;
  gateway)
    required_files=(
      "config/nacos/application-common.yml"
      "config/nacos/artemis-gateway.yml"
    )
    ;;
  symphony)
    workflow_path="${SYMPHONY_WORKFLOW_PATH:-WORKFLOW.md}"
    required_files=("$workflow_path")
    ;;
  *)
    usage
    exit 1
    ;;
esac

print_step "Checking required config for $service"
for file in "${required_files[@]}"; do
  if [[ ! -f "$file" ]]; then
    echo "Missing required config for $service: $file" >&2
    exit 1
  fi
  echo "config ok: $file"
done

print_step "Config check completed for $service"
