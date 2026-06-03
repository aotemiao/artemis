#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

usage() {
  echo "Usage: $0 <gateway|auth|system|all|<domain>> [image_tag_suffix]" >&2
}

run_in_repo_root
require_cmd docker

target="${1:-}"
tag_suffix="${2:-local}"
normalized_target="$(normalize_service_name "$target")"

if ! docker info >/dev/null 2>&1; then
  echo "Docker daemon is not available. Start Docker Desktop / OrbStack and rerun: scripts/dev/build-image.sh ${target:-all} ${tag_suffix}" >&2
  exit 1
fi

build_one() {
  local service="$1"
  local dockerfile="$2"
  local image="artemis-${service}:${tag_suffix}"
  print_step "Building image: ${image}"
  local attempt
  for attempt in 1 2 3; do
    if docker build -f "$dockerfile" -t "$image" .; then
      return 0
    fi
    if [[ "$attempt" == "3" ]]; then
      echo "Failed to build image after ${attempt} attempts: ${image}" >&2
      return 1
    fi
    echo "Docker build failed for ${image}; retrying (${attempt}/3)..." >&2
    sleep 10
  done
}

case "$normalized_target" in
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
    dockerfile="docker/Dockerfile.${normalized_target}"
    if [[ ! -f "$dockerfile" ]]; then
      usage
      exit 1
    fi
    build_one "$normalized_target" "$dockerfile"
    ;;
esac

print_step "Image build completed for: $normalized_target"
