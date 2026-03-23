#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

print_step "Checking OpenSpec sync against working tree"
scripts/harness/check-openspec-sync.sh working-tree

print_step "Checking markdown links"
scripts/harness/check-doc-links.sh

print_step "Checking docs consistency"
scripts/harness/check-doc-consistency.sh

print_step "Checking docs freshness"
scripts/harness/check-doc-freshness.sh

print_step "Running mvn verify"
run_mvn -B verify

print_step "Full verify completed"
