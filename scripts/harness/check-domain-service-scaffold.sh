#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

temp_dir="$(mktemp -d)"
trap 'rm -rf "$temp_dir"' EXIT

print_step "Checking domain service scaffold preview output"
scripts/dev/new-domain-service.sh phase1-demo --port 9410 --output-root "$temp_dir" --skip-register >/dev/null

required_files=(
  "artemis-api/artemis-api-phase1-demo/pom.xml"
  "artemis-api/artemis-api-phase1-demo/src/main/java/com/aotemiao/artemis/api/phase1/demo/package-info.java"
  "artemis-modules/artemis-phase1-demo/pom.xml"
  "artemis-modules/artemis-phase1-demo/artemis-phase1-demo-client/CLIENT_CONTRACT.md"
  "artemis-modules/artemis-phase1-demo/SERVICE_API.md"
  "artemis-modules/artemis-phase1-demo/artemis-phase1-demo-app/src/test/java/com/aotemiao/artemis/phase1/demo/app/query/GetPhase1DemoPingQryExeTest.java"
  "artemis-modules/artemis-phase1-demo/artemis-phase1-demo-start/src/test/java/com/aotemiao/artemis/phase1/demo/arch/Phase1DemoLayerDependencyRulesTest.java"
  "config/nacos/artemis-phase1-demo.yml"
  "scripts/dev/run-phase1-demo.sh"
  "scripts/dev/check-phase1-demo-readiness.sh"
  "scripts/smoke/phase1-demo-ping.sh"
  "docker/Dockerfile.phase1-demo"
)

for file in "${required_files[@]}"; do
  if [[ ! -f "${temp_dir}/${file}" ]]; then
    echo "Scaffold output is missing required file: ${file}" >&2
    exit 1
  fi
done

grep -Fq "jacoco-maven-plugin" \
  "${temp_dir}/artemis-modules/artemis-phase1-demo/artemis-phase1-demo-app/pom.xml"
grep -Fq "jacoco-maven-plugin" \
  "${temp_dir}/artemis-modules/artemis-phase1-demo/artemis-phase1-demo-adapter/pom.xml"
grep -Fq "jacoco-maven-plugin" \
  "${temp_dir}/artemis-modules/artemis-phase1-demo/artemis-phase1-demo-infra/pom.xml"
grep -Fq 'ROUTE: GET /api/phase1-demo/ping' \
  "${temp_dir}/artemis-modules/artemis-phase1-demo/SERVICE_API.md"
grep -Fq 'INTERFACE: com.aotemiao.artemis.phase1.demo.client.api.Phase1DemoPingService' \
  "${temp_dir}/artemis-modules/artemis-phase1-demo/artemis-phase1-demo-client/CLIENT_CONTRACT.md"

bash -n "${temp_dir}/scripts/dev/run-phase1-demo.sh"
bash -n "${temp_dir}/scripts/dev/check-phase1-demo-readiness.sh"
bash -n "${temp_dir}/scripts/smoke/phase1-demo-ping.sh"

print_step "Domain service scaffold check passed"
