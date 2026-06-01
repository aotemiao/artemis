#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

usage() {
  cat >&2 <<'EOF'
Usage: scripts/dev/start-all.sh [options]

Options:
  --full      Include resource/workflow services through the Compose full profile
  --no-build  Reuse existing images instead of building before start
  --skip-readiness
              Start containers without running host-side readiness checks
  -h, --help  Show this help

Default: start PostgreSQL, Redis, Nacos, Nacos config loader, system, auth and gateway.
EOF
}

run_in_repo_root

full=0
build=1
skip_readiness=0

while [[ "$#" -gt 0 ]]; do
  case "$1" in
    --full)
      full=1
      shift
      ;;
    --no-build)
      build=0
      shift
      ;;
    --skip-readiness)
      skip_readiness=1
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      usage
      exit 1
      ;;
  esac
done

cmd="$(compose_cmd)"
compose_args=(-f docker/docker-compose.yml)
if [[ "$full" -eq 1 ]]; then
  compose_args+=(--profile full)
fi

up_args=(up -d)
if [[ "$build" -eq 1 ]]; then
  up_args+=(--build)
fi

print_step "Starting Artemis with Docker Compose"
$cmd "${compose_args[@]}" rm -f -s nacos-config-loader >/dev/null 2>&1 || true
$cmd "${compose_args[@]}" "${up_args[@]}"

print_step "Artemis Docker Compose services"
$cmd "${compose_args[@]}" ps

if [[ "$skip_readiness" -eq 0 ]]; then
  print_step "Checking Artemis service readiness"
  scripts/dev/check-service-readiness.sh system 60 2
  if [[ "$full" -eq 1 ]]; then
    scripts/dev/check-service-readiness.sh resource 60 2
    scripts/dev/check-service-readiness.sh workflow 60 2
  fi
  scripts/dev/check-service-readiness.sh auth 60 2
  scripts/dev/check-service-readiness.sh gateway 60 2
fi
