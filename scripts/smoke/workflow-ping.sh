#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

base_url="${1:-http://127.0.0.1:9410}"
ping_url="${base_url}/api/workflow/ping"

print_step "Smoke checking artemis-workflow ping endpoint"
scripts/dev/wait-http.sh "${ping_url}" "200" 20 1 "GET"

print_step "artemis-workflow ping smoke completed"
