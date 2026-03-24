#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

while IFS= read -r service; do
  [[ -z "$service" ]] && continue
  smoke_script="$(service_catalog_field "$service" smoke_script || true)"
  [[ -z "$smoke_script" ]] && continue
  print_step "Running ${service} smoke"
  "$smoke_script"
done < <(service_catalog_names)

print_step "All service smoke completed"
