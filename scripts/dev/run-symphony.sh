#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

split_run_script_args "$@"

workflow_override=""
has_server_port=0
if [[ "${#RUN_SCRIPT_APP_ARGS[@]}" -gt 0 ]]; then
  for arg in "${RUN_SCRIPT_APP_ARGS[@]}"; do
    if [[ "$arg" == --server.port=* ]]; then
      has_server_port=1
    fi
    if [[ "$arg" == --symphony.workflow-path=* ]]; then
      workflow_override="${arg#--symphony.workflow-path=}"
    fi
  done
fi

if [[ -n "$workflow_override" ]]; then
  export SYMPHONY_WORKFLOW_PATH="$workflow_override"
fi

scripts/dev/check-service-config.sh symphony

if [[ -n "${SYMPHONY_WORKFLOW_PATH:-}" && -z "$workflow_override" ]]; then
  symphony_args=(--symphony.workflow-path="${SYMPHONY_WORKFLOW_PATH}")
  if [[ "${#RUN_SCRIPT_APP_ARGS[@]}" -gt 0 ]]; then
    symphony_args+=("${RUN_SCRIPT_APP_ARGS[@]}")
  fi
  RUN_SCRIPT_APP_ARGS=("${symphony_args[@]}")
fi

if [[ "$has_server_port" -eq 0 ]]; then
  symphony_args=(--server.port=9500)
  if [[ "${#RUN_SCRIPT_APP_ARGS[@]}" -gt 0 ]]; then
    symphony_args+=("${RUN_SCRIPT_APP_ARGS[@]}")
  fi
  RUN_SCRIPT_APP_ARGS=("${symphony_args[@]}")
  print_step "Starting artemis-symphony with default HTTP port 9500"
else
  print_step "Starting artemis-symphony with custom application arguments"
fi

run_args=()
if [[ "${#RUN_SCRIPT_MVN_ARGS[@]}" -gt 0 ]]; then
  run_args+=("${RUN_SCRIPT_MVN_ARGS[@]}")
fi
if [[ "${#RUN_SCRIPT_APP_ARGS[@]}" -gt 0 ]]; then
  run_args+=("${RUN_SCRIPT_APP_ARGS[@]}")
fi

run_packaged_service_module artemis-symphony/artemis-symphony-start "${run_args[@]}"
