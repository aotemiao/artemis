#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

scripts/dev/check-service-config.sh "system"
scripts/dev/check-service-readiness.sh "system" "${1:-30}" "${2:-2}"
