#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/lib/common.sh"

run_in_repo_root

contains_fixed_string() {
  local pattern="$1"
  local file="$2"
  if command -v rg >/dev/null 2>&1 && rg --version >/dev/null 2>&1; then
    rg -q --fixed-strings -- "$pattern" "$file"
    return
  fi
  grep -Fq -- "$pattern" "$file"
}

require_file_contains() {
  local file="$1"
  local pattern="$2"
  require_repo_path_exact "$file"
  if ! contains_fixed_string "$pattern" "$file"; then
    echo "Missing required content in $file: $pattern" >&2
    exit 1
  fi
}

require_file_not_contains() {
  local file="$1"
  local pattern="$2"
  require_repo_path_exact "$file"
  if contains_fixed_string "$pattern" "$file"; then
    echo "Outdated content found in $file: $pattern" >&2
    exit 1
  fi
}

print_step "Checking docs index and core entry docs"
require_repo_path_exact "docs/asset-manifest.yml"
require_file_contains "docs/README.md" "SERVICE_SMOKE_RUNBOOK.md"
require_file_contains "docs/README.md" "ADD_DOMAIN_SERVICE_RUNBOOK.md"
require_file_contains "docs/README.md" "ADD_DUBBO_CLIENT_RUNBOOK.md"
require_file_contains "docs/README.md" "ADD_ARCHUNIT_RULE_RUNBOOK.md"
require_file_contains "docs/README.md" "AGENT_REVIEW_LOOP.md"
require_file_contains "docs/README.md" "AGENT_DEVELOPMENT_WORKFLOW.md"
require_file_contains "docs/README.md" "DOC_FRESHNESS_POLICY.md"
require_file_contains "docs/README.md" "DEPLOY_AND_ROLLBACK_RUNBOOK.md"
require_file_contains "docs/README.md" "PROJECT_PROGRESS_REPORT.md"
require_file_contains "docs/README.md" "SYMPHONY_TROUBLESHOOTING.md"
require_file_contains "docs/README.md" "QUALITY_ISSUE_STANDARD.md"
require_file_contains "docs/README.md" "quality-issues/"
require_file_contains "docs/README.md" "deploy-drills/"
require_file_contains "docs/README.md" "feature-specs/"
require_file_contains "docs/README.md" "agent-evals/"
require_file_contains "docs/README.md" "patterns/"
require_file_contains "docs/README.md" "spec-to-validation-mapping.md"
require_file_contains "docs/README.md" "security-review-checklist.md"
require_file_contains "docs/README.md" "AGENT_PERMISSION_RUNBOOK.md"
require_file_contains "docs/README.md" "RISK_BASED_VERIFICATION_RUNBOOK.md"
require_file_contains "docs/README.md" "agent-runs/"
require_file_contains "docs/README.md" "security/"
require_file_contains "docs/README.md" "asset-manifest.yml"

print_step "Checking README consistency"
require_file_contains "README.md" "如何最快了解项目"
require_file_contains "README.md" "分层收敛方向"
require_file_contains "README.md" "文档与目录职责"
require_file_contains "README.md" "事实源"
require_file_contains "README.md" "执行入口"
require_file_contains "README.md" "验证守门"
require_file_contains "README.md" "任务过程"
require_file_contains "README.md" "编排资产"
require_file_contains "README.md" "运行与交付资产"
require_file_contains "README.md" "openspec/specs/"
require_file_contains "README.md" "docs/runbooks/"
require_file_contains "README.md" "docs/governance/"
require_file_contains "README.md" "docs/reports/"
require_file_contains "README.md" "docs/agent-workflow/"
require_file_contains "README.md" "docs/feature-specs/"
require_file_contains "README.md" "docs/patterns/"
require_file_contains "README.md" "docs/agent-evals/"
require_file_contains "README.md" "scripts/dev/"
require_file_contains "README.md" "scripts/harness/"
require_file_contains "README.md" "scripts/smoke/"
require_file_contains "README.md" "artemis-symphony/skills/"
require_file_contains "README.md" "artemis-symphony/prompts/"
require_file_contains "README.md" "docs/exec-plans/active/"
require_file_contains "README.md" "PROJECT_PROGRESS_REPORT.md"
require_file_contains "README.md" "QUALITY_SCORE.md"
require_file_contains "README.md" "AGENT_DEVELOPMENT_WORKFLOW.md"
require_file_contains "README.md" "DEPLOY_AND_ROLLBACK_RUNBOOK.md"
require_file_contains "README.md" ".github/workflows/governance.yml"
require_file_contains "README.md" ".github/workflows/verify.yml"

print_step "Checking Maven test runtime consistency"
require_file_contains "pom.xml" "<mockito.version>5.17.0</mockito.version>"
require_file_contains "pom.xml" "<argLine></argLine>"
require_file_contains "pom.xml" "-javaagent:\${settings.localRepository}/org/mockito/mockito-core/\${mockito.version}/mockito-core-\${mockito.version}.jar"
require_file_contains "pom.xml" "<artifactId>maven-surefire-plugin</artifactId>"
require_file_contains "pom.xml" "<artifactId>maven-failsafe-plugin</artifactId>"
require_file_contains "openspec/specs/engineering-constraints/spec.md" "测试 JVM 参数可重复"
require_file_contains "openspec/specs/engineering-constraints/spec.md" "测试环境隔离"
require_file_contains "docs/runbooks/RISK_BASED_VERIFICATION_RUNBOOK.md" "Surefire、Failsafe、JaCoCo、Mockito"
require_file_contains "docs/runbooks/RISK_BASED_VERIFICATION_RUNBOOK.md" "测试 fake、临时目录、网络、SSH、HOME 展开或外部 transport"

print_step "Checking AGENTS consistency"
require_file_contains "AGENTS.md" "scripts/dev/check-service-config.sh"
require_file_contains "AGENTS.md" "scripts/dev/check-service-readiness.sh"
require_file_contains "AGENTS.md" "scripts/dev/new-domain-service.sh"
require_file_contains "AGENTS.md" "scripts/dev/package-service.sh"
require_file_contains "AGENTS.md" "scripts/dev/build-image.sh"
require_file_contains "AGENTS.md" "scripts/dev/service-status.sh"
require_file_contains "AGENTS.md" "scripts/dev/deploy-drill.sh"
require_file_contains "AGENTS.md" "scripts/dev/rollback-drill.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-api-doc-sync.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-client-contracts.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-capability-package-structure.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-domain-service-scaffold.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-service-catalog.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-symphony-assets.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-feature-specs.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-deploy-drill-reports.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-spec-driven-delivery-chain.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-agentic-harness-assets.sh"
require_file_contains "AGENTS.md" "scripts/harness/run-agent-evals.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-critical-path-tests.sh"
require_file_contains "AGENTS.md" "scripts/harness/check-duplicate-patterns.sh"
require_file_contains "AGENTS.md" "scripts/harness/run-governance-checks.sh"
require_file_contains "AGENTS.md" "scripts/smoke/all-services.sh"
require_file_contains "AGENTS.md" "docs/runbooks/ADD_DOMAIN_SERVICE_RUNBOOK.md"
require_file_contains "AGENTS.md" "docs/runbooks/ADD_DUBBO_CLIENT_RUNBOOK.md"
require_file_contains "AGENTS.md" "docs/runbooks/ADD_ARCHUNIT_RULE_RUNBOOK.md"
require_file_contains "AGENTS.md" "docs/agent-workflow/AGENT_REVIEW_LOOP.md"
require_file_contains "AGENTS.md" "docs/runbooks/AGENT_PERMISSION_RUNBOOK.md"
require_file_contains "AGENTS.md" "docs/runbooks/RISK_BASED_VERIFICATION_RUNBOOK.md"
require_file_contains "AGENTS.md" "docs/patterns/security-review-checklist.md"
require_file_contains "AGENTS.md" "docs/security/THREAT_MODEL.md"
require_file_contains "AGENTS.md" "docs/reports/PROJECT_PROGRESS_REPORT.md"
require_file_contains "AGENTS.md" "docs/governance/QUALITY_ISSUE_STANDARD.md"
require_file_contains "AGENTS.md" "docs/runbooks/DEPLOY_AND_ROLLBACK_RUNBOOK.md"
require_file_contains "AGENTS.md" "docs/runbooks/SYMPHONY_TROUBLESHOOTING.md"

print_step "Checking ARCHITECTURE drift"
require_file_contains "ARCHITECTURE.md" "scripts/dev/package-service.sh"
require_file_contains "ARCHITECTURE.md" "scripts/dev/build-image.sh"
require_file_contains "ARCHITECTURE.md" "scripts/dev/check-service-config.sh"
require_file_contains "ARCHITECTURE.md" "scripts/dev/check-service-readiness.sh"
require_file_contains "ARCHITECTURE.md" "scripts/dev/new-domain-service.sh"
require_file_contains "ARCHITECTURE.md" "scripts/dev/service-status.sh"
require_file_contains "ARCHITECTURE.md" "scripts/dev/deploy-drill.sh"
require_file_contains "ARCHITECTURE.md" "scripts/dev/rollback-drill.sh"
require_file_contains "ARCHITECTURE.md" "scripts/harness/run-governance-checks.sh"
require_file_contains "ARCHITECTURE.md" "scripts/harness/check-service-catalog.sh"
require_file_contains "ARCHITECTURE.md" "scripts/harness/check-symphony-assets.sh"
require_file_contains "ARCHITECTURE.md" "PROJECT_PROGRESS_REPORT.md"
require_file_not_contains 'ARCHITECTURE.md' 'mvn verify` 级别的统一守门尚未完全固化到 Maven lifecycle'

print_step "Checking asset directory docs consistency"
require_file_contains "docs/runbooks/SERVICE_SMOKE_RUNBOOK.md" "scripts/dev/check-service-readiness.sh"
require_file_contains "docs/runbooks/SERVICE_SMOKE_RUNBOOK.md" "scripts/smoke/symphony-state.sh"
require_file_contains "docs/runbooks/SERVICE_SMOKE_RUNBOOK.md" "scripts/smoke/all-services.sh"
require_file_contains "docs/governance/DOC_FRESHNESS_POLICY.md" "Last Reviewed"
require_file_contains "docs/governance/DOC_FRESHNESS_POLICY.md" "PROJECT_PROGRESS_REPORT.md"
require_file_contains "docs/runbooks/DEPLOY_AND_ROLLBACK_RUNBOOK.md" "scripts/dev/build-image.sh"
require_file_contains "docs/runbooks/DEPLOY_AND_ROLLBACK_RUNBOOK.md" "scripts/dev/deploy-drill.sh"
require_file_contains "docs/runbooks/DEPLOY_AND_ROLLBACK_RUNBOOK.md" "scripts/dev/rollback-drill.sh"
require_file_contains "docs/reports/PROJECT_PROGRESS_REPORT.md" "## 汇总结论"
require_file_contains "docs/reports/PROJECT_PROGRESS_REPORT.md" "## 下一阶段演进路线"
require_file_contains "docs/runbooks/SYMPHONY_TROUBLESHOOTING.md" "scripts/smoke/symphony-state.sh"
require_file_contains "docs/governance/QUALITY_ISSUE_STANDARD.md" "quality-issues/archive/"
require_file_contains "docs/feature-specs/README.md" "## 验收标准"
require_file_contains "docs/patterns/spec-to-validation-mapping.md" "## 映射原则"
require_file_contains "docs/patterns/security-review-checklist.md" "## 审查清单"
require_file_contains "docs/security/THREAT_MODEL.md" "## 信任边界"
require_file_contains "docs/runbooks/AGENT_PERMISSION_RUNBOOK.md" "## 权限矩阵"
require_file_contains "docs/runbooks/RISK_BASED_VERIFICATION_RUNBOOK.md" "## 变更分类"
require_file_contains "docs/agent-evals/README.md" "## 评测目标"
require_file_contains "docs/reports/agent-runs/README.md" "## 摘要用途"
require_file_contains "docs/exec-plans/templates/execution-plan-template.md" "## 验收映射"
require_file_contains "docs/exec-plans/templates/execution-plan-template.md" "## 风险分类"
require_file_contains "docs/feature-specs/templates/feature-spec-template.md" "## 异常与风险场景"
require_file_contains "docs/reports/deploy-drills/2026-06-01-sample-report-template.md" "## 验证结果"

print_step "Checking asset manifest"
python3 - <<'PY'
from pathlib import Path
import sys

repo = Path.cwd()
manifest = repo / "docs/asset-manifest.yml"
lines = manifest.read_text(encoding="utf-8").splitlines()
errors: list[str] = []
current: dict[str, str] | None = None
assets: list[dict[str, str]] = []

for raw in lines:
    line = raw.rstrip()
    stripped = line.strip()
    if stripped.startswith("- path: "):
        if current is not None:
            assets.append(current)
        current = {"path": stripped.removeprefix("- path: ").strip()}
        continue
    if current is not None and stripped.startswith(("type: ", "owner: ")):
        key, value = stripped.split(":", 1)
        current[key.strip()] = value.strip()

if current is not None:
    assets.append(current)

if not assets:
    errors.append("docs/asset-manifest.yml: no assets found")

seen: set[str] = set()
for asset in assets:
    path = asset.get("path", "")
    if not path:
        errors.append("docs/asset-manifest.yml: asset missing path")
        continue
    if path in seen:
        errors.append(f"docs/asset-manifest.yml: duplicate path {path}")
    seen.add(path)
    if not asset.get("type"):
        errors.append(f"docs/asset-manifest.yml: {path} missing type")
    if not asset.get("owner"):
        errors.append(f"docs/asset-manifest.yml: {path} missing owner")
    if not (repo / path).exists():
        errors.append(f"docs/asset-manifest.yml: referenced path does not exist: {path}")

if errors:
    print("Asset manifest check failed:", file=sys.stderr)
    for error in errors:
        print(f"  - {error}", file=sys.stderr)
    raise SystemExit(1)
PY

print_step "Docs consistency check passed"
