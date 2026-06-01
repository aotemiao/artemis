#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

usage() {
  cat >&2 <<'EOF'
Usage: scripts/dev/stop-all.sh [options]

Options:
  --volumes   Remove Compose volumes together with containers
  -h, --help  Show this help
EOF
}

run_in_repo_root

remove_volumes=0

while [[ "$#" -gt 0 ]]; do
  case "$1" in
    --volumes)
      remove_volumes=1
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
down_args=(down)
if [[ "$remove_volumes" -eq 1 ]]; then
  down_args+=(-v)
fi

print_step "Stopping Artemis Docker Compose services"
$cmd "${compose_args[@]}" "${down_args[@]}"
