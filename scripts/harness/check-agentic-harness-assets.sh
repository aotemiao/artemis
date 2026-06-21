#!/bin/sh
# 校验关键 agentic harness 资产存在，且模板保留必需的风险/验证小节。
# POSIX sh + grep，可在 Linux / macOS / Windows(Git Bash) 下运行，不依赖 python3。
set -eu

cd "$(git rev-parse --show-toplevel)"

fail() {
  echo "Agentic harness 资产检查失败: $1" >&2
  exit 1
}

echo
echo "==> Checking agentic harness assets"

# 1. 必需存在的核心入口、治理与 Symphony agent 资产
for asset in \
  AGENTS.md \
  ARCHITECTURE.md \
  README.md \
  docs/patterns/security-review-checklist.md \
  docs/security/THREAT_MODEL.md \
  docs/runbooks/AGENT_PERMISSION_RUNBOOK.md \
  docs/runbooks/RISK_BASED_VERIFICATION_RUNBOOK.md \
  docs/feature-specs/templates/feature-spec-template.md \
  docs/exec-plans/templates/execution-plan-template.md \
  artemis-symphony/WORKFLOW.md.example \
  artemis-symphony/tools/README.md \
  artemis-symphony/tools/registry.json \
  artemis-symphony/skills/spec-driven-delivery.md \
  artemis-symphony/skills/adversarial-review.md \
  artemis-symphony/prompts/spec-driven-delivery.md \
  artemis-symphony/prompts/adversarial-review.md
do
  [ -f "$asset" ] || fail "缺少必需资产: $asset"
done

# 2. 外部 agent 指针必须回指 AGENTS.md，避免多套指令漂移
for pointer in CLAUDE.md GEMINI.md .github/copilot-instructions.md; do
  [ -f "$pointer" ] || fail "缺少外部 agent 指针: $pointer"
  grep -Fq "AGENTS.md" "$pointer" || fail "$pointer 未回指 AGENTS.md"
done

# 3. 模板必须保留风险/验证小节，防止退化成空壳模板
grep -Fq "## 异常与风险场景" docs/feature-specs/templates/feature-spec-template.md \
  || fail "feature-spec 模板缺少“## 异常与风险场景”小节"
grep -Fq "## 工程风险评估" docs/feature-specs/templates/feature-spec-template.md \
  || fail "feature-spec 模板缺少“## 工程风险评估”小节"
grep -Fq "## 风险分类" docs/exec-plans/templates/execution-plan-template.md \
  || fail "执行计划模板缺少“## 风险分类”小节"
grep -Fq "## 验证分类" docs/exec-plans/templates/execution-plan-template.md \
  || fail "执行计划模板缺少“## 验证分类”小节"

echo "==> Agentic harness asset check passed"
