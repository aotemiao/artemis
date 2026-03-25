#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/service-catalog.sh"

repo_root() {
  git rev-parse --show-toplevel
}

run_in_repo_root() {
  cd "$(repo_root)"
}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

prefer_java_21() {
  if [[ "$(uname -s)" != "Darwin" ]]; then
    return 0
  fi
  if ! command -v /usr/libexec/java_home >/dev/null 2>&1; then
    return 0
  fi

  local java21_home
  java21_home="$(/usr/libexec/java_home -v 21 2>/dev/null || true)"
  if [[ -z "$java21_home" ]]; then
    return 0
  fi

  export JAVA_HOME="$java21_home"
  export PATH="$JAVA_HOME/bin:$PATH"
}

require_java_21() {
  prefer_java_21
  require_cmd java

  local version_line
  version_line="$(java -version 2>&1 | head -n 1)"
  if [[ "$version_line" != *\"21* ]]; then
    echo "Java 21 is required. Current runtime: $version_line" >&2
    echo "If you are on macOS and Java 21 is installed, use the repository scripts so JAVA_HOME is selected automatically." >&2
    exit 1
  fi
}

run_mvn() {
  require_java_21
  require_cmd mvn
  mvn "$@"
}

RUN_SCRIPT_MVN_ARGS=()
RUN_SCRIPT_APP_ARGS=()

split_run_script_args() {
  RUN_SCRIPT_MVN_ARGS=()
  RUN_SCRIPT_APP_ARGS=()

  local arg
  local value
  local parsed_args=()
  for arg in "$@"; do
    case "$arg" in
      -Dspring-boot.run.arguments=*)
        value="${arg#-Dspring-boot.run.arguments=}"
        if [[ -n "$value" ]]; then
          parsed_args=()
          read -r -a parsed_args <<< "$value"
          if [[ "${#parsed_args[@]}" -eq 0 ]]; then
            RUN_SCRIPT_APP_ARGS+=("$value")
          else
            RUN_SCRIPT_APP_ARGS+=("${parsed_args[@]}")
          fi
        fi
        ;;
      --*)
        RUN_SCRIPT_APP_ARGS+=("$arg")
        ;;
      *)
        RUN_SCRIPT_MVN_ARGS+=("$arg")
        ;;
    esac
  done
}

resolve_boot_jar() {
  local module="${1:-}"
  local target_dir="${module%/}/target"
  local jars=()
  local jar

  if [[ ! -d "$target_dir" ]]; then
    echo "Missing target directory for module: $module" >&2
    return 1
  fi

  while IFS= read -r jar; do
    [[ -z "$jar" ]] && continue
    jars+=("$jar")
  done < <(find "$target_dir" -maxdepth 1 -type f -name '*.jar' \
    ! -name '*.jar.original' \
    ! -name 'original-*.jar' \
    ! -name '*-sources.jar' \
    ! -name '*-javadoc.jar' \
    ! -name '*-plain.jar' | sort)

  if [[ "${#jars[@]}" -eq 0 ]]; then
    echo "Unable to locate a runnable jar under: $target_dir" >&2
    return 1
  fi

  printf '%s\n' "${jars[0]}"
}

run_packaged_service_module() {
  local module="${1:-}"
  shift || true

  if [[ -z "$module" ]]; then
    echo "Missing service start module." >&2
    exit 1
  fi

  split_run_script_args "$@"

  local mvn_args=(-B -pl "$module" -am package -Dmaven.test.skip=true)
  if [[ "${#RUN_SCRIPT_MVN_ARGS[@]}" -gt 0 ]]; then
    mvn_args+=("${RUN_SCRIPT_MVN_ARGS[@]}")
  fi
  run_mvn "${mvn_args[@]}"

  local jar
  jar="$(resolve_boot_jar "$module")" || exit 1

  print_step "Running boot jar: ${jar}"
  local java_args=(java -jar "$jar")
  if [[ "${#RUN_SCRIPT_APP_ARGS[@]}" -gt 0 ]]; then
    java_args+=("${RUN_SCRIPT_APP_ARGS[@]}")
  fi
  exec "${java_args[@]}"
}

compose_cmd() {
  if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
    echo "docker compose"
    return 0
  fi
  if command -v docker-compose >/dev/null 2>&1; then
    echo "docker-compose"
    return 0
  fi
  echo "Neither 'docker compose' nor 'docker-compose' is available." >&2
  exit 1
}

print_step() {
  echo
  echo "==> $1"
}

normalize_service_name() {
  local raw="${1:-}"
  raw="${raw#artemis-}"
  echo "$raw"
}

resolve_service_start_module() {
  local normalized
  normalized="$(normalize_service_name "${1:-}")"

  if service_catalog_has "$normalized"; then
    service_catalog_field "$normalized" start_module
    return 0
  fi

  case "$normalized" in
    *)
      local module="artemis-modules/artemis-${normalized}/artemis-${normalized}-start"
      if [[ -d "$module" ]]; then
        echo "$module"
        return 0
      fi
      return 1
      ;;
  esac
}
