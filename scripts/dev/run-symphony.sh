#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

args=("$@")
has_run_arguments=0
for arg in "${args[@]}"; do
  if [[ "$arg" == -Dspring-boot.run.arguments=* ]]; then
    has_run_arguments=1
    break
  fi
done

if [[ "$has_run_arguments" -eq 0 ]]; then
  args=(-Dspring-boot.run.arguments=--server.port=9500 "${args[@]}")
  print_step "Starting artemis-symphony with default HTTP port 9500"
else
  print_step "Starting artemis-symphony with custom spring-boot.run.arguments"
fi

run_mvn -pl artemis-symphony/artemis-symphony-start -am spring-boot:run "${args[@]}"
