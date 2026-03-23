#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

check_tcp() {
  local host="$1"
  local port="$2"
  local name="$3"
  if ! nc -z "$host" "$port" >/dev/null 2>&1; then
    echo "TCP check failed for $name at $host:$port" >&2
    exit 1
  fi
  echo "$name: ok ($host:$port)"
}

check_http_exact() {
  local url="$1"
  local expected="$2"
  local name="$3"
  local status
  status="$(curl -sS -o /dev/null -w "%{http_code}" "$url")"
  if [[ "$status" != "$expected" ]]; then
    echo "HTTP check failed for $name at $url, expected $expected, got $status" >&2
    exit 1
  fi
  echo "$name: ok ($status)"
}

check_http_any() {
  local method="$1"
  local url="$2"
  local expected_csv="$3"
  local name="$4"
  local status
  status="$(curl -sS -X "$method" -o /dev/null -w "%{http_code}" "$url")"
  IFS=',' read -r -a expected <<< "$expected_csv"
  for code in "${expected[@]}"; do
    if [[ "$status" == "$code" ]]; then
      echo "$name: ok ($status)"
      return 0
    fi
  done
  echo "HTTP check failed for $name at $url, expected one of [$expected_csv], got $status" >&2
  exit 1
}

run_in_repo_root
require_cmd curl
require_cmd nc

print_step "Checking local infrastructure"
check_tcp 127.0.0.1 5432 "PostgreSQL"
check_tcp 127.0.0.1 6379 "Redis"
check_http_exact "http://127.0.0.1:8848/nacos/v1/console/health/readiness" "200" "Nacos readiness"

print_step "Checking local services"
check_http_exact "http://127.0.0.1:9300/api/lookup-types?page=0&size=1" "200" "System lookup page"
check_http_any "POST" "http://127.0.0.1:9200/auth/refresh" "200,401,403" "Auth refresh"
check_tcp 127.0.0.1 8080 "Gateway port"

print_step "Health check completed"
