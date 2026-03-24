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

  [[ -d "$service_root" ]] || {
    echo "Missing service root for ${service}: ${service_root}" >&2
    exit 1
  }
  [[ -d "artemis-api/${api_bridge}" ]] || {
    echo "Missing API bridge for ${service}: artemis-api/${api_bridge}" >&2
    exit 1
  }
  [[ -f "$smoke_script" ]] || {
    echo "Missing smoke script for ${service}: ${smoke_script}" >&2
    exit 1
  }
  [[ -f "scripts/dev/run-${service}.sh" ]] || {
    echo "Missing run script for ${service}: scripts/dev/run-${service}.sh" >&2
    exit 1
  }
  [[ -f "scripts/dev/check-${service}-readiness.sh" ]] || {
    echo "Missing readiness script for ${service}: scripts/dev/check-${service}-readiness.sh" >&2
    exit 1
  }
  [[ -f "$dockerfile" ]] || {
    echo "Missing dockerfile for ${service}: ${dockerfile}" >&2
    exit 1
  }
  [[ -f "${service_root}/${service_artifact}-client/CLIENT_CONTRACT.md" ]] || {
    echo "Missing client contract doc for ${service}" >&2
    exit 1
  }

  if ! find "$service_root" -maxdepth 1 -type f \( -name "*_API.md" -o -name "SERVICE_API.md" \) | grep -q .; then
    echo "Missing API doc entry for ${service}: expected *_API.md or SERVICE_API.md under ${service_root}" >&2
    exit 1
  fi
done < <(service_catalog_names)

print_step "Service asset catalog check passed"
