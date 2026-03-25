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
    if [[ "$service" == "symphony" ]]; then
      echo "Missing required config for symphony: $file" >&2
      echo "Copy artemis-symphony/WORKFLOW.md.example to ${file} and fill tracker.project_slug before rerunning." >&2
      exit 1
    fi
    echo "Missing required config for $service: $file" >&2
    exit 1
  fi
  echo "config ok: $file"
done

if [[ "$service" == "symphony" ]]; then
  if ! grep -Eq '^[[:space:]]*project_slug:[[:space:]]*[^[:space:]#]+' "$workflow_path"; then
    echo "warning: tracker.project_slug is blank in $workflow_path; Symphony can start, but it cannot dispatch Linear work yet." >&2
  fi

  workflow_api_key_var="$(sed -nE 's/^[[:space:]]*api_key:[[:space:]]*\$([A-Z_][A-Z0-9_]*)[[:space:]]*$/\1/p' "$workflow_path" | head -n 1)"
  if [[ -n "$workflow_api_key_var" && -z "${!workflow_api_key_var:-}" ]]; then
    echo "warning: $workflow_path references \$$workflow_api_key_var, but the environment variable is not set." >&2
  fi

  workflow_assignee_var="$(sed -nE 's/^[[:space:]]*assignee:[[:space:]]*\$([A-Z_][A-Z0-9_]*)[[:space:]]*$/\1/p' "$workflow_path" | head -n 1)"
  if [[ -n "$workflow_assignee_var" && -z "${!workflow_assignee_var:-}" ]]; then
    echo "warning: $workflow_path references \$$workflow_assignee_var for tracker.assignee, but the environment variable is not set." >&2
  fi

  workflow_api_key_literal="$(
    sed -nE '/^[[:space:]]*api_key:[[:space:]]*\$/d; s/^[[:space:]]*api_key:[[:space:]]*([^[:space:]#].*)$/\1/p' "$workflow_path" \
      | head -n 1
  )"
  if [[ -n "$workflow_api_key_literal" ]]; then
    echo "warning: tracker.api_key is written directly in $workflow_path; prefer \$LINEAR_API_KEY (or another env var) to avoid long-lived plaintext secrets." >&2
  fi

  ssh_host_count="$(
    awk '
      /^[[:space:]]*ssh_hosts:[[:space:]]*$/ { in_block=1; next }
      in_block && /^[[:space:]]*-[[:space:]]*/ { count++; next }
      in_block && /^[[:space:]]*[A-Za-z0-9_]+:[[:space:]]*/ { in_block=0 }
      END { print count + 0 }
    ' "$workflow_path"
  )"
  max_per_host="$(sed -nE 's/^[[:space:]]*max_concurrent_agents_per_host:[[:space:]]*([0-9]+)[[:space:]]*$/\1/p' "$workflow_path" | head -n 1)"
  if [[ -n "$max_per_host" && "$ssh_host_count" == "0" ]]; then
    echo "warning: worker.max_concurrent_agents_per_host is set in $workflow_path, but worker.ssh_hosts is empty; the per-host limit will never take effect." >&2
  fi

  if grep -Eq '^[[:space:]]*ssh_hosts:[[:space:]]*$' "$workflow_path"; then
    if [[ -z "${SYMPHONY_SSH_EXECUTABLE:-}" ]] && ! command -v ssh >/dev/null 2>&1; then
      echo "warning: worker.ssh_hosts is configured in $workflow_path, but no ssh executable is available (set SYMPHONY_SSH_EXECUTABLE or install ssh)." >&2
    fi
  fi
fi

print_step "Config check completed for $service"
