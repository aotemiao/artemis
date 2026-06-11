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
review_date="${timestamp:0:10}"
report_dir="docs/reports/deploy-drills"
mkdir -p "$report_dir"
report_path="${report_dir}/${timestamp}-${service}-rollback.md"

{
  echo "# Rollback Drill Report"
  echo
  echo "Status: completed"
  echo "Last Reviewed: ${review_date}"
  echo "Review Cadence: 90 days"
  echo
  echo "## 演练范围"
  echo
  echo "- 时间：${timestamp}"
  echo "- 服务：${service}"
  echo "- 回滚目标：${rollback_target}"
  echo "- 目标类型：${target_kind}"
  echo
  echo "## 执行命令"
  echo
  echo '```bash'
  printf 'scripts/dev/rollback-drill.sh %q %q\n' "$service" "$rollback_target"
  echo '```'
  echo
  echo "## 验证结果"
  echo
  echo "| 检查项 | 命令或证据 | 结果 |"
  echo "|--------|------------|------|"
  echo "| 回滚目标存在 | ${rollback_target} | 通过：${target_kind} |"
  echo "| 建议 smoke | $(service_catalog_field "$service" smoke_script) | 待执行 |"
  echo "| 日志入口 | $(service_catalog_field "$service" log_file 2>/dev/null || true) | 待检查 |"
  echo
  echo "## 指标摘要"
  echo
  echo '```json'
  echo "{"
  echo '  "schema_version": 1,'
  echo '  "summary_type": "deploy_drill_report",'
  echo '  "kind": "rollback",'
  echo "  \"service\": \"${service}\","
  echo "  \"services\": [\"${service}\"],"
  echo '  "status": "completed",'
  echo '  "smoke": "pending",'
  echo '  "rollback": true,'
  echo '  "failure_stage": ""'
  echo "}"
  echo '```'
  echo
  echo "## 回滚后建议动作"
  echo
  echo "- 重新部署回滚目标"
  echo "- 执行 smoke：$(service_catalog_field "$service" smoke_script)"
  echo "- 查看日志：$(service_catalog_field "$service" log_file 2>/dev/null || true)"
  echo
  echo "## 结论"
  echo
  echo "- 回滚演练目标已解析；实际回滚与 smoke 结果需在执行后补充。"
} >"$report_path"

print_step "Rollback drill completed"
echo "Report written to ${report_path}"
