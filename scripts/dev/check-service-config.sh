#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

usage() {
  echo "Usage: $0 <system|auth|gateway|symphony|<domain>>" >&2
}

run_in_repo_root

service="$(normalize_service_name "${1:-}")"
if service_catalog_has "$service"; then
  IFS=',' read -r -a required_files <<< "$(service_catalog_field "$service" config_files)"
else
  service_config="config/nacos/artemis-${service}.yml"
  if [[ ! -f "$service_config" ]]; then
    usage
    exit 1
  fi
  required_files=(
    "config/nacos/application-common.yml"
    "config/nacos/datasource.yml"
    "$service_config"
  )
fi

if [[ "$service" == "symphony" ]]; then
  workflow_path="${SYMPHONY_WORKFLOW_PATH:-WORKFLOW.md}"
  required_files=("$workflow_path")
fi

print_step "Checking required config for $service"
for file in "${required_files[@]}"; do
  if [[ ! -f "$file" ]]; then
    echo "Missing required config for $service: $file" >&2
    exit 1
  fi
  echo "config ok: $file"
done

print_step "Config check completed for $service"
