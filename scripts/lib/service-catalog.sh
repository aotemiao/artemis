#!/usr/bin/env bash

# 服务目录：用于统一脚本、治理检查与运行时入口。
# 字段顺序：
# name|kind|start_module|port|config_files|readiness_mode|readiness_url|readiness_expected|readiness_method|smoke_script|log_file|dockerfile|api_bridge

SERVICE_RECORDS=(
  "gateway|platform|artemis-gateway|8080|config/nacos/application-common.yml,config/nacos/artemis-gateway.yml|http|http://127.0.0.1:8080/auth/refresh|200,401,403|POST|scripts/smoke/gateway-auth-refresh.sh|logs/artemis-gateway.log|docker/Dockerfile.gateway|"
  "auth|platform|artemis-auth|9200|config/nacos/application-common.yml,config/nacos/artemis-auth.yml|http|http://127.0.0.1:9200/auth/refresh|200,401,403|POST|scripts/smoke/auth-refresh.sh|logs/artemis-auth.log|docker/Dockerfile.auth|"
  "system|domain|artemis-modules/artemis-system/artemis-system-start|9300|config/nacos/application-common.yml,config/nacos/datasource.yml,config/nacos/artemis-system.yml|http|http://127.0.0.1:9300/api/lookup-types?page=0&size=1|200|GET|scripts/smoke/system-lookup.sh|logs/artemis-system.log|docker/Dockerfile.system|artemis-api-system"
  "symphony|platform|artemis-symphony/artemis-symphony-start|9500|WORKFLOW.md|http|http://127.0.0.1:9500/api/v1/state|200|GET|scripts/smoke/symphony-state.sh|||"
  # -- generated service records --
  "resource|domain|artemis-modules/artemis-resource/artemis-resource-start|9400|config/nacos/application-common.yml,config/nacos/datasource.yml,config/nacos/artemis-resource.yml|smoke||||scripts/smoke/resource-ping.sh|logs/artemis-resource.log|docker/Dockerfile.resource|artemis-api-resource"
  # -- end generated service records --
)

service_catalog_record() {
  local name="${1:-}"
  local record
  for record in "${SERVICE_RECORDS[@]}"; do
    [[ "${record%%|*}" == "$name" ]] && {
      printf '%s\n' "$record"
      return 0
    }
  done
  return 1
}

service_catalog_field() {
  local name="${1:-}"
  local field="${2:-}"
  local record
  record="$(service_catalog_record "$name")" || return 1

  IFS='|' read -r service_name service_kind start_module port config_files readiness_mode readiness_url readiness_expected readiness_method smoke_script log_file dockerfile api_bridge <<< "$record"

  case "$field" in
    name) printf '%s\n' "$service_name" ;;
    kind) printf '%s\n' "$service_kind" ;;
    start_module) printf '%s\n' "$start_module" ;;
    port) printf '%s\n' "$port" ;;
    config_files) printf '%s\n' "$config_files" ;;
    readiness_mode) printf '%s\n' "$readiness_mode" ;;
    readiness_url) printf '%s\n' "$readiness_url" ;;
    readiness_expected) printf '%s\n' "$readiness_expected" ;;
    readiness_method) printf '%s\n' "$readiness_method" ;;
    smoke_script) printf '%s\n' "$smoke_script" ;;
    log_file) printf '%s\n' "$log_file" ;;
    dockerfile) printf '%s\n' "$dockerfile" ;;
    api_bridge) printf '%s\n' "$api_bridge" ;;
    *)
      return 1
      ;;
  esac
}

service_catalog_has() {
  local name="${1:-}"
  service_catalog_record "$name" >/dev/null 2>&1
}

service_catalog_names() {
  local record
  for record in "${SERVICE_RECORDS[@]}"; do
    [[ "$record" == \#* ]] && continue
    printf '%s\n' "${record%%|*}"
  done
}
