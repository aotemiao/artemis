#!/bin/sh

set -eu

NACOS_SERVER="${NACOS_SERVER:-http://nacos:8848}"
NACOS_GROUP="${NACOS_GROUP:-DEFAULT_GROUP}"
NACOS_NAMESPACE_ID="${NACOS_NAMESPACE_ID:-}"
CONFIG_DIR="${CONFIG_DIR:-/config/nacos}"
REDIS_HOST="${REDIS_HOST:-redis}"
POSTGRES_HOST="${POSTGRES_HOST:-postgres}"
POSTGRES_PORT="${POSTGRES_PORT:-5432}"

wait_for_nacos() {
  readiness_url="${NACOS_SERVER%/}/nacos/v1/console/health/readiness"
  for _ in $(seq 1 60); do
    if curl -fsS "$readiness_url" >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done

  echo "Nacos is not ready: $readiness_url" >&2
  return 1
}

render_config() {
  data_id="$1"
  source_file="$2"
  target_file="$3"

  case "$data_id" in
    application-common.yml)
      sed "s/host: 127.0.0.1/host: ${REDIS_HOST}/" "$source_file" > "$target_file"
      ;;
    datasource.yml)
      sed "s#jdbc:postgresql://localhost:5432/#jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/#" \
        "$source_file" > "$target_file"
      ;;
    *)
      cp "$source_file" "$target_file"
      ;;
  esac
}

publish_config() {
  data_id="$1"
  source_file="${CONFIG_DIR}/${data_id}"
  rendered_file="${tmp_dir}/${data_id}"

  if [ ! -f "$source_file" ]; then
    return 0
  fi

  render_config "$data_id" "$source_file" "$rendered_file"

  if [ -n "$NACOS_NAMESPACE_ID" ]; then
    response="$(curl -fsS -X POST "${NACOS_SERVER%/}/nacos/v2/cs/config" \
      --data-urlencode "dataId=${data_id}" \
      --data-urlencode "group=${NACOS_GROUP}" \
      --data-urlencode "namespaceId=${NACOS_NAMESPACE_ID}" \
      --data-urlencode "content@${rendered_file}")"
  else
    response="$(curl -fsS -X POST "${NACOS_SERVER%/}/nacos/v2/cs/config" \
      --data-urlencode "dataId=${data_id}" \
      --data-urlencode "group=${NACOS_GROUP}" \
      --data-urlencode "content@${rendered_file}")"
  fi

  if printf '%s' "$response" | grep -Eq '"code"[[:space:]]*:[[:space:]]*0|^true$'; then
    echo "  OK  ${data_id}"
    return 0
  fi

  echo "  FAIL ${data_id}: ${response}" >&2
  return 1
}

wait_for_nacos

tmp_dir="$(mktemp -d)"
trap 'rm -rf "$tmp_dir"' EXIT

echo "Uploading Nacos configs to ${NACOS_SERVER%/}"
found=0
ok=0

for data_id in \
  application-common.yml \
  datasource.yml \
  artemis-system.yml \
  artemis-resource.yml \
  artemis-workflow.yml \
  artemis-auth.yml \
  artemis-gateway.yml
do
  if [ ! -f "${CONFIG_DIR}/${data_id}" ]; then
    continue
  fi
  found=$((found + 1))
  if publish_config "$data_id"; then
    ok=$((ok + 1))
  fi
done

if [ "$found" -eq 0 ]; then
  echo "No config files found under ${CONFIG_DIR}" >&2
  exit 1
fi

if [ "$ok" -ne "$found" ]; then
  echo "Published ${ok}/${found} config(s)." >&2
  exit 1
fi

echo "Nacos config upload completed: ${ok}/${found}"
