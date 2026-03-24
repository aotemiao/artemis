#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

print_step "Checking OpenSpec sync against working tree"
scripts/harness/check-openspec-sync.sh working-tree

print_step "Running governance checks"
scripts/harness/run-governance-checks.sh

print_step "Running mvn verify"
run_mvn -B verify

print_step "Full verify completed"
