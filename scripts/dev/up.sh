#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd git
cmd="$(compose_cmd)"

print_step "Starting local infrastructure from docker/docker-compose.yml"
cd docker
$cmd up -d

print_step "Local infrastructure started"
echo "Next:"
echo "  1. Ensure Nacos configs under config/nacos are loaded"
echo "  2. Start services with scripts/dev/run-system.sh, scripts/dev/run-auth.sh, scripts/dev/run-gateway.sh"
