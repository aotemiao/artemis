#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

usage() {
  cat >&2 <<'EOF'
Usage: scripts/dev/upload-nacos-configs.sh [options]

Options:
  --nacos-server <url>     Nacos HTTP address, default: http://127.0.0.1:8848
  --namespace-id <id>      Nacos namespace ID, default: public namespace
  --group <group>          Nacos config group, default: DEFAULT_GROUP
  --redis-host <host>      Redis host rendered into application-common.yml, default: 127.0.0.1
  --postgres-host <host>   PostgreSQL host rendered into datasource.yml, default: localhost
  --postgres-port <port>   PostgreSQL port rendered into datasource.yml, default: 5432
  -h, --help               Show this help
EOF
}

run_in_repo_root
require_cmd curl
require_cmd grep
require_cmd mktemp
require_cmd sed

nacos_server="http://127.0.0.1:8848"
namespace_id=""
group="DEFAULT_GROUP"
redis_host="127.0.0.1"
postgres_host="localhost"
postgres_port="5432"

while [[ "$#" -gt 0 ]]; do
  case "$1" in
    --nacos-server)
      nacos_server="${2:-}"
      shift 2
      ;;
    --namespace-id)
      namespace_id="${2:-}"
      shift 2
      ;;
    --group)
      group="${2:-}"
      shift 2
      ;;
    --redis-host)
      redis_host="${2:-}"
      shift 2
      ;;
    --postgres-host)
      postgres_host="${2:-}"
      shift 2
      ;;
    --postgres-port)
      postgres_port="${2:-}"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      usage
      exit 1
      ;;
  esac
done

if [[ -z "$nacos_server" || -z "$group" || -z "$redis_host" || -z "$postgres_host" || -z "$postgres_port" ]]; then
  usage
  exit 1
fi

config_dir="config/nacos"
order=(
  application-common.yml
  datasource.yml
  artemis-system.yml
  artemis-resource.yml
  artemis-workflow.yml
  artemis-auth.yml
  artemis-gateway.yml
)

wait_for_nacos() {
  local readiness_url="${nacos_server%/}/nacos/v1/console/health/readiness"
  local attempt
  for attempt in $(seq 1 60); do
    if curl -fsS "$readiness_url" >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done

  echo "Nacos is not ready: $readiness_url" >&2
  return 1
}

render_config() {
  local data_id="$1"
  local source_file="$2"
  local target_file="$3"

  case "$data_id" in
    application-common.yml)
      sed -e "s/host: 127.0.0.1/host: ${redis_host}/" "$source_file" > "$target_file"
      ;;
    datasource.yml)
      sed -e "s#jdbc:postgresql://localhost:5432/#jdbc:postgresql://${postgres_host}:${postgres_port}/#" \
        "$source_file" > "$target_file"
      ;;
    *)
      cp "$source_file" "$target_file"
      ;;
  esac
}

publish_config() {
  local data_id="$1"
  local source_file="${config_dir}/${data_id}"
  local rendered_file
  local response
  local curl_args=()

  [[ -f "$source_file" ]] || return 0

  rendered_file="$(mktemp)"
  render_config "$data_id" "$source_file" "$rendered_file"

  curl_args=(
    -fsS
    -X POST
    "${nacos_server%/}/nacos/v2/cs/config"
    --data-urlencode "dataId=${data_id}"
    --data-urlencode "group=${group}"
    --data-urlencode "content@${rendered_file}"
  )
  if [[ -n "$namespace_id" ]]; then
    curl_args+=(--data-urlencode "namespaceId=${namespace_id}")
  fi

  if ! response="$(curl "${curl_args[@]}")"; then
    rm -f "$rendered_file"
    echo "  ERROR ${data_id}: request failed" >&2
    return 1
  fi
  rm -f "$rendered_file"

  if printf '%s' "$response" | grep -Eq '"code"[[:space:]]*:[[:space:]]*0|^true$'; then
    echo "  OK  ${data_id}"
    return 0
  fi

  echo "  FAIL ${data_id}: ${response}" >&2
  return 1
}

print_step "Waiting for Nacos at ${nacos_server}"
wait_for_nacos

print_step "Uploading Nacos configs"
echo "Group: ${group}"
if [[ -n "$namespace_id" ]]; then
  echo "NamespaceId: ${namespace_id}"
else
  echo "NamespaceId: public"
fi

found=0
ok=0
for data_id in "${order[@]}"; do
  if [[ ! -f "${config_dir}/${data_id}" ]]; then
    continue
  fi
  found=$((found + 1))
  if publish_config "$data_id"; then
    ok=$((ok + 1))
  fi
done

if [[ "$found" -eq 0 ]]; then
  echo "No config files found under ${config_dir}" >&2
  exit 1
fi

if [[ "$ok" -ne "$found" ]]; then
  echo "Published ${ok}/${found} config(s)." >&2
  exit 1
fi

print_step "Nacos config upload completed: ${ok}/${found}"
