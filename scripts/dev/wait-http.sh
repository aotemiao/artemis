#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

usage() {
  echo "Usage: $0 <url> [expected_csv] [attempts] [sleep_seconds] [method]" >&2
}

run_in_repo_root
require_cmd curl

url="${1:-}"
expected_csv="${2:-200}"
attempts="${3:-30}"
sleep_seconds="${4:-2}"
method="${5:-GET}"

if [[ -z "$url" ]]; then
  usage
  exit 1
fi

tmp_body="$(mktemp)"
trap 'rm -f "$tmp_body"' EXIT

for ((attempt = 1; attempt <= attempts; attempt++)); do
  status="$(curl -sS -X "$method" -o "$tmp_body" -w "%{http_code}" "$url" || true)"
  IFS=',' read -r -a expected_codes <<< "$expected_csv"
  for code in "${expected_codes[@]}"; do
    if [[ "$status" == "$code" ]]; then
      echo "HTTP ready: $url ($status) on attempt $attempt/$attempts"
      exit 0
    fi
  done
  if (( attempt < attempts )); then
    sleep "$sleep_seconds"
  fi
done

echo "HTTP wait failed for $url, expected one of [$expected_csv], got ${status:-<none>}" >&2
if [[ -s "$tmp_body" ]]; then
  cat "$tmp_body" >&2
fi
exit 1
