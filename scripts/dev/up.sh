#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd git
cmd="$(compose_cmd)"

print_step "Starting local infrastructure from docker/docker-compose.yml"
cd docker
$cmd up -d postgres redis nacos

print_step "Local infrastructure started"
echo "Next:"
echo "  1. Load Nacos configs with scripts/dev/upload-nacos-configs.sh"
echo "  2. Start services with scripts/dev/run-system.sh, scripts/dev/run-auth.sh, scripts/dev/run-gateway.sh"
