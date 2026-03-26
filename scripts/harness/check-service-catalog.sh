#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

print_step "Checking registered service assets"

while IFS= read -r service; do
  [[ -z "$service" ]] && continue
  kind="$(service_catalog_field "$service" kind)"
  [[ "$kind" != "domain" ]] && continue

  start_module="$(service_catalog_field "$service" start_module)"
  api_bridge="$(service_catalog_field "$service" api_bridge)"
  smoke_script="$(service_catalog_field "$service" smoke_script)"
  dockerfile="$(service_catalog_field "$service" dockerfile)"

  service_root="${start_module%/*}"
  service_artifact="$(basename "$service_root")"

  require_repo_path_exact "$service_root"
  require_repo_path_exact "artemis-api/${api_bridge}"
  require_repo_path_exact "$smoke_script"
  require_repo_path_exact "scripts/dev/run-${service}.sh"
  require_repo_path_exact "scripts/dev/check-${service}-readiness.sh"
  require_repo_path_exact "$dockerfile"
  require_repo_path_exact "${service_root}/${service_artifact}-client/CLIENT_CONTRACT.md"

  if ! find "$service_root" -maxdepth 1 -type f \( -name "*_API.md" -o -name "SERVICE_API.md" \) | grep -q .; then
    echo "Missing API doc entry for ${service}: expected *_API.md or SERVICE_API.md under ${service_root}" >&2
    exit 1
  fi
done < <(service_catalog_names)

print_step "Service asset catalog check passed"
