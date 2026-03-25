#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

if [[ -z "${LINEAR_API_KEY:-}" ]]; then
  echo "LINEAR_API_KEY is required for Symphony live e2e." >&2
  exit 1
fi

require_cmd codex

if [[ -z "${SYMPHONY_LIVE_SSH_WORKER_HOSTS:-}" ]]; then
  compose_cmd >/dev/null
  if ! docker info >/dev/null 2>&1; then
    echo "Docker daemon is not running, but SSH docker fallback is required." >&2
    exit 1
  fi
  require_cmd ssh-keygen
  if [[ ! -s "${HOME}/.codex/auth.json" ]]; then
    echo "SSH docker fallback requires ~/.codex/auth.json to exist." >&2
    exit 1
  fi
fi

export SYMPHONY_RUN_LIVE_E2E=1

print_step "Running Symphony live e2e (local worker + ssh worker)"
run_mvn \
  -pl artemis-symphony/artemis-symphony-start \
  -am \
  -DfailIfNoTests=false \
  -Dfailsafe.failIfNoSpecifiedTests=false \
  -Dit.test=SymphonyLiveE2EIT \
  clean \
  test-compile \
  org.apache.maven.plugins:maven-failsafe-plugin:3.5.4:integration-test \
  org.apache.maven.plugins:maven-failsafe-plugin:3.5.4:verify \
  "$@"
