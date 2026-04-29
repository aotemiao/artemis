#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

scripts/dev/check-service-config.sh "workflow"
scripts/smoke/workflow-ping.sh "${1:-http://127.0.0.1:9410}"
