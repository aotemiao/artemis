#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root
require_cmd curl

base_url="${1:-http://127.0.0.1:8080}"
login_url="$base_url/auth/login"
user_url="$base_url/api/system/users/1"
internal_url="$base_url/api/system/internal/auth/users/1/authorization"

admin_response_file="$(mktemp)"
internal_response_file="$(mktemp)"
trap 'rm -f "$admin_response_file" "$internal_response_file"' EXIT

print_step "Smoke checking gateway admin RBAC"
scripts/dev/wait-http.sh "$login_url" "200,401,403" 20 1 "POST"

login_response="$(curl -sS -X POST "$login_url" \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456"}')"
token="$(printf '%s' "$login_response" | grep -o '"token":"[^"]*"' | head -n 1 | cut -d':' -f2- | tr -d '"')"

if [[ -z "$token" ]]; then
  echo "Failed to parse admin token from login response:" >&2
  echo "$login_response" >&2
  exit 1
fi

admin_status="$(curl -sS -o "$admin_response_file" -w '%{http_code}' \
  -H "Authorization: $token" \
  "$user_url")"

if [[ "$admin_status" != "200" ]]; then
  echo "Expected admin system route to return 200, got $admin_status" >&2
  cat "$admin_response_file" >&2
  exit 1
fi

internal_status="$(curl -sS -o "$internal_response_file" -w '%{http_code}' \
  -H "Authorization: $token" \
  "$internal_url")"

if [[ "$internal_status" != "403" ]]; then
  echo "Expected internal system route to be blocked by gateway with 403, got $internal_status" >&2
  cat "$internal_response_file" >&2
  exit 1
fi

print_step "Gateway RBAC smoke completed"
