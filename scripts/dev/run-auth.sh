#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

print_step "Starting artemis-auth"
run_mvn -pl artemis-auth -am spring-boot:run "$@"
