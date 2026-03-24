#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

required_files=(
  "artemis-auth/src/test/java/com/aotemiao/artemis/auth/arch/SystemClientDependencyRulesTest.java"
  "artemis-auth/src/test/java/com/aotemiao/artemis/auth/client/SystemUserValidateClientTest.java"
  "artemis-auth/src/test/java/com/aotemiao/artemis/auth/web/AuthControllerTest.java"
  "artemis-auth/src/test/java/com/aotemiao/artemis/auth/web/AuthExceptionHandlerTest.java"
  "artemis-modules/artemis-system/artemis-system-app/src/test/java/com/aotemiao/artemis/system/app/command/ValidateCredentialsCmdExeTest.java"
  "artemis-modules/artemis-system/artemis-system-adapter/src/test/java/com/aotemiao/artemis/system/adapter/web/InternalAuthControllerTest.java"
  "artemis-modules/artemis-system/artemis-system-infra/src/test/java/com/aotemiao/artemis/system/infra/LookupTypeGatewayImplIntegrationTest.java"
  "artemis-modules/artemis-system/artemis-system-infra/src/test/java/com/aotemiao/artemis/system/infra/UserCredentialsGatewayImplTest.java"
  "artemis-modules/artemis-system/artemis-system-start/src/test/java/com/aotemiao/artemis/system/arch/SystemLayerDependencyRulesTest.java"
  "artemis-symphony/artemis-symphony-start/src/test/java/com/aotemiao/artemis/symphony/api/SymphonyStateControllerTest.java"
)

missing=0

print_step "Checking critical path test baseline"
for file in "${required_files[@]}"; do
  if [[ ! -f "$file" ]]; then
    echo "Missing required critical-path test: $file" >&2
    missing=1
  fi
done

if [[ "$missing" -ne 0 ]]; then
  exit 1
fi

print_step "Critical path test baseline check passed"
