#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

base_url="${1:-http://127.0.0.1:9500}"
root_url="$base_url/"
state_url="$base_url/api/v1/state"

print_step "Smoke checking symphony root page"
scripts/dev/wait-http.sh "$root_url" "200" 20 1 "GET"

print_step "Smoke checking symphony state endpoint"
scripts/dev/wait-http.sh "$state_url" "200" 20 1 "GET"

print_step "Symphony state smoke completed"
