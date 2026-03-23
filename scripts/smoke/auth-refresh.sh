#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

base_url="${1:-http://127.0.0.1:9200}"
refresh_url="$base_url/auth/refresh"

print_step "Smoke checking auth refresh endpoint"
scripts/dev/wait-http.sh "$refresh_url" "200,401,403" 20 1 "POST"

print_step "Auth refresh smoke completed"
