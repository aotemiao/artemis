#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

usage() {
  cat <<'USAGE'
Usage: scripts/harness/generate-ci-harness-metrics.sh [output_dir] [evals_dir] [agent_runs_dir] [delivery_signals_dir] [deploy_drills_dir] [agent_eval_drafts_dir]

Generates a low-sensitivity Harness metrics snapshot for CI artifact upload.

Environment overrides:
  HARNESS_METRICS_OUTPUT_DIR
  HARNESS_METRICS_EVALS_DIR
  HARNESS_METRICS_AGENT_RUNS_DIR
  HARNESS_METRICS_DELIVERY_SIGNALS_DIR
  HARNESS_METRICS_DEPLOY_DRILLS_DIR
  HARNESS_METRICS_AGENT_EVAL_DRAFTS_DIR
USAGE
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

if [[ "$#" -gt 6 ]]; then
  usage >&2
  exit 1
fi

run_in_repo_root

output_dir="${1:-${HARNESS_METRICS_OUTPUT_DIR:-artifacts/harness-metrics}}"
evals_dir="${2:-${HARNESS_METRICS_EVALS_DIR:-artifacts/agent-evals}}"
agent_runs_dir="${3:-${HARNESS_METRICS_AGENT_RUNS_DIR:-artifacts/agent-runs}}"
delivery_signals_dir="${4:-${HARNESS_METRICS_DELIVERY_SIGNALS_DIR:-artifacts/harness-delivery-signals}}"
deploy_drills_dir="${5:-${HARNESS_METRICS_DEPLOY_DRILLS_DIR:-docs/reports/deploy-drills}}"
agent_eval_drafts_dir="${6:-${HARNESS_METRICS_AGENT_EVAL_DRAFTS_DIR:-artifacts/agent-eval-drafts}}"

print_step "Generating CI harness metrics snapshot"
scripts/harness/generate-harness-metrics-report.sh \
  "$evals_dir" \
  "$agent_runs_dir" \
  "$output_dir" \
  "$delivery_signals_dir" \
  "$deploy_drills_dir" \
  "$agent_eval_drafts_dir"

if [[ -n "${GITHUB_STEP_SUMMARY:-}" && -f "$output_dir/latest.md" ]]; then
  {
    echo
    echo "## Harness Metrics Snapshot"
    echo
    echo "Generated artifact directory: \`$output_dir\`"
    echo
    cat "$output_dir/latest.md"
  } >> "$GITHUB_STEP_SUMMARY"
fi

print_step "CI harness metrics snapshot ready"
echo "JSON: $output_dir/latest.json"
echo "Markdown: $output_dir/latest.md"
echo "Delivery signals: $delivery_signals_dir"
echo "Deploy drills: $deploy_drills_dir"
echo "Agent eval drafts: $agent_eval_drafts_dir"
