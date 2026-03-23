#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

base_url="${1:-http://127.0.0.1:9300}"
page_url="$base_url/api/lookup-types?page=0&size=1"
items_url="$base_url/api/lookup-types/nonexistent/items"

print_step "Smoke checking system lookup page endpoint"
scripts/dev/wait-http.sh "$page_url" "200" 20 1 "GET"

print_step "Smoke checking system lookup items endpoint"
scripts/dev/wait-http.sh "$items_url" "200" 20 1 "GET"

print_step "System lookup smoke completed"
