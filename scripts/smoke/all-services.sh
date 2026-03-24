#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

print_step "Running system smoke"
scripts/smoke/system-lookup.sh

print_step "Running auth smoke"
scripts/smoke/auth-refresh.sh

print_step "Running gateway smoke"
scripts/smoke/gateway-auth-refresh.sh

print_step "Running symphony smoke"
scripts/smoke/symphony-state.sh

print_step "All service smoke completed"
