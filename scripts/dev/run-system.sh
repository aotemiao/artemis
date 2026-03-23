#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

print_step "Starting artemis-system"
run_mvn -pl artemis-modules/artemis-system/artemis-system-start -am spring-boot:run "$@"
