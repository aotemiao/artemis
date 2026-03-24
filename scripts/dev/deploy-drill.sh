#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

usage() {
  echo "Usage: $0 <all|service> [tag_suffix] [--skip-smoke]" >&2
}

run_in_repo_root
require_cmd date
require_cmd mkdir

target="${1:-all}"
tag_suffix="${2:-drill}"
skip_smoke="0"

if [[ "${3:-}" == "--skip-smoke" || "${2:-}" == "--skip-smoke" ]]; then
  skip_smoke="1"
  [[ "${2:-}" == "--skip-smoke" ]] && tag_suffix="drill"
fi

services=()
if [[ "$target" == "all" ]]; then
  while IFS= read -r service; do
    [[ -z "$service" ]] && continue
    services+=("$service")
  done < <(service_catalog_names)
else
  target="$(normalize_service_name "$target")"
  if ! service_catalog_has "$target"; then
    usage
    exit 1
  fi
  services+=("$target")
fi

timestamp="$(date '+%Y-%m-%d-%H%M%S')"
report_dir="docs/harness-engineering/deploy-drills"
mkdir -p "$report_dir"
report_path="${report_dir}/${timestamp}-${target}.md"

{
  echo "# Deploy Drill Report"
  echo
  echo "- 时间：${timestamp}"
  echo "- 目标：${target}"
  echo "- 镜像标签后缀：${tag_suffix}"
  echo "- smoke：$([[ "$skip_smoke" == "1" ]] && echo "跳过" || echo "若本地服务在线则执行")"
  echo
  echo "## 结果"
} >"$report_path"

for service in "${services[@]}"; do
  print_step "Deploy drill for ${service}"
  scripts/dev/check-service-config.sh "$service"
  scripts/dev/package-service.sh "$service" -DskipTests

  dockerfile="$(service_catalog_field "$service" dockerfile 2>/dev/null || true)"
  image_result="跳过（无 Dockerfile）"
  if [[ -n "$dockerfile" ]]; then
    scripts/dev/build-image.sh "$service" "$tag_suffix"
    image_result="已构建 ${service_catalog_field "$service" name 2>/dev/null || echo "$service"}:${tag_suffix}"
  fi

  smoke_result="跳过"
  if [[ "$skip_smoke" != "1" ]]; then
    port="$(service_catalog_field "$service" port 2>/dev/null || true)"
    if [[ -n "$port" ]] && nc -z 127.0.0.1 "$port" >/dev/null 2>&1; then
      if scripts/dev/check-service-readiness.sh "$service" 3 1; then
        smoke_result="通过"
      else
        smoke_result="失败"
      fi
    else
      smoke_result="跳过（本地服务未运行）"
    fi
  fi

  {
    echo "### ${service}"
    echo
    echo "- start 模块：$(service_catalog_field "$service" start_module)"
    echo "- 配置检查：通过"
    echo "- 镜像：${image_result}"
    echo "- smoke：${smoke_result}"
    echo "- 日志：$(service_catalog_field "$service" log_file 2>/dev/null || true)"
    echo
  } >>"$report_path"
done

print_step "Deploy drill completed"
echo "Report written to ${report_path}"
