#!/bin/sh
# 仓库级治理检查编排器。POSIX sh，可跨平台运行；子检查各自保留原 shebang。
#
# 可选参数 scope：all（默认）/ code / docs。
#   - verify-changed 会按改动文件类型传入 scope，让增量验证只跑相关子检查。
#   - full-verify 与 CI 不传参，默认 all，始终跑全量。
# 每个子检查可在第三个参数标注所属 scope（空格分隔可多选）。未标注的子检查在任何
# scope 下都会运行——这是 fail-safe 默认：新增检查若忘记标注，会被全量执行，绝不会
# 因为 scope 收窄而被静默跳过。只有明确标注、且当前 scope 不在其列表内的检查才跳过。
set -eu

cd "$(git rev-parse --show-toplevel)"

scope="${1:-all}"
case "$scope" in
  all | code | docs) ;;
  *)
    echo "Usage: $0 [all|code|docs]" >&2
    exit 1
    ;;
esac

run_governance_check() {
  name="$1"
  script="$2"
  categories="${3:-}"
  if [ "$scope" != "all" ] && [ -n "$categories" ]; then
    case " $categories " in
      *" $scope "*) ;;
      *)
        echo
        echo "==> Governance: ${name} (skipped for scope=${scope})"
        return 0
        ;;
    esac
  fi
  echo
  echo "==> Governance: ${name}"
  if ! bash "$script"; then
    echo "Governance sub-check failed: ${name} (${script})" >&2
    exit 1
  fi
}

echo
echo "==> Running governance checks (scope=${scope})"
run_governance_check "Markdown Links" scripts/harness/check-doc-links.sh
run_governance_check "OpenSpec Change State" scripts/harness/check-openspec-change-state.sh "docs"
run_governance_check "Feature Specs" scripts/harness/check-feature-specs.sh "docs"
run_governance_check "Spec Driven Delivery Chain" scripts/harness/check-spec-driven-delivery-chain.sh
run_governance_check "Agentic Harness Assets" scripts/harness/check-agentic-harness-assets.sh
run_governance_check "Agent Run Summaries" scripts/harness/check-agent-run-summaries.sh "docs"
run_governance_check "API Doc Sync" scripts/harness/check-api-doc-sync.sh
run_governance_check "Client Contracts" scripts/harness/check-client-contracts.sh
run_governance_check "Capability Package Structure" scripts/harness/check-capability-package-structure.sh "code"
run_governance_check "Domain Service Scaffold" scripts/harness/check-domain-service-scaffold.sh "code"
run_governance_check "Service Catalog" scripts/harness/check-service-catalog.sh
run_governance_check "Deploy Drill Reports" scripts/harness/check-deploy-drill-reports.sh "docs"
run_governance_check "Critical Path Tests" scripts/harness/check-critical-path-tests.sh "code"
run_governance_check "Duplicate Patterns" scripts/harness/check-duplicate-patterns.sh
run_governance_check "Quality Issue Archive" scripts/harness/check-quality-issue-archive.sh "docs"

echo
echo "==> Governance checks completed (scope=${scope})"
