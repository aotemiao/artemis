#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
cmd="$(compose_cmd)"

print_step "Stopping local infrastructure"
cd docker
$cmd down

print_step "Local infrastructure stopped"
