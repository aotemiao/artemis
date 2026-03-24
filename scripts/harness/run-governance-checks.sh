#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

print_step "Running governance checks"
scripts/harness/check-doc-links.sh
scripts/harness/check-doc-consistency.sh
scripts/harness/check-doc-freshness.sh
scripts/harness/check-api-doc-sync.sh
scripts/harness/check-client-contracts.sh
scripts/harness/check-critical-path-tests.sh
scripts/harness/check-duplicate-patterns.sh
scripts/harness/check-quality-issue-archive.sh

print_step "Governance checks completed"
