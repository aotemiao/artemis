#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

usage() {
  echo "Usage: $0 <gateway|auth|system|all> [image_tag_suffix]" >&2
}

run_in_repo_root
require_cmd docker

target="${1:-}"
tag_suffix="${2:-local}"

if ! docker info >/dev/null 2>&1; then
  echo "Docker daemon is not available. Start Docker Desktop / OrbStack and rerun: scripts/dev/build-image.sh ${target:-all} ${tag_suffix}" >&2
  exit 1
fi

build_one() {
  local service="$1"
  local dockerfile="$2"
  local image="artemis-${service}:${tag_suffix}"
  print_step "Building image: ${image}"
  docker build -f "$dockerfile" -t "$image" .
}

case "$target" in
  gateway)
    build_one "gateway" "docker/Dockerfile.gateway"
    ;;
  auth)
    build_one "auth" "docker/Dockerfile.auth"
    ;;
  system)
    build_one "system" "docker/Dockerfile.system"
    ;;
  all)
    build_one "gateway" "docker/Dockerfile.gateway"
    build_one "auth" "docker/Dockerfile.auth"
    build_one "system" "docker/Dockerfile.system"
    ;;
  *)
    usage
    exit 1
    ;;
esac

print_step "Image build completed for: $target"
