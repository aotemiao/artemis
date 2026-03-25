#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

print_step "Starting artemis-resource"
run_packaged_service_module artemis-modules/artemis-resource/artemis-resource-start "$@"
