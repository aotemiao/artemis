#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

usage() {
  echo "Usage: $0 <service> <image-tag|jar-path>" >&2
}

run_in_repo_root
require_cmd date

service="${1:-}"
rollback_target="${2:-}"

if [[ -z "$service" || -z "$rollback_target" ]]; then
  usage
  exit 1
fi

service="$(normalize_service_name "$service")"
if ! service_catalog_has "$service"; then
  usage
  exit 1
fi

target_kind=""
if docker image inspect "$rollback_target" >/dev/null 2>&1; then
  target_kind="docker-image"
elif [[ -f "$rollback_target" ]]; then
  target_kind="jar-file"
else
  echo "Rollback target not found as local image or jar file: ${rollback_target}" >&2
  exit 1
fi

timestamp="$(date '+%Y-%m-%d-%H%M%S')"
report_dir="docs/harness-engineering/deploy-drills"
mkdir -p "$report_dir"
report_path="${report_dir}/${timestamp}-${service}-rollback.md"

{
  echo "# Rollback Drill Report"
  echo
  echo "- 时间：${timestamp}"
  echo "- 服务：${service}"
  echo "- 回滚目标：${rollback_target}"
  echo "- 目标类型：${target_kind}"
  echo
  echo "## 回滚后建议动作"
  echo
  echo "- 重新部署回滚目标"
  echo "- 执行 smoke：$(service_catalog_field "$service" smoke_script)"
  echo "- 查看日志：$(service_catalog_field "$service" log_file 2>/dev/null || true)"
} >"$report_path"

print_step "Rollback drill completed"
echo "Report written to ${report_path}"
