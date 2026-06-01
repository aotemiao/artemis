# Spec Driven Delivery Skill

适用场景：处理带业务语义的需求，尤其是 PRD、复杂 issue、跨模块功能和需要明确验收标准的任务。

## 先读

- `docs/feature-specs/README.md`
- `docs/feature-specs/templates/feature-spec-template.md`
- `docs/patterns/spec-to-validation-mapping.md`
- `docs/patterns/agent-delivery-handoff.md`
- `docs/agent-workflow/AGENT_DEVELOPMENT_WORKFLOW.md`
- 相关 `openspec/specs/`

## 执行步骤

1. 判断需求是否需要 Feature Spec。
2. 如果需要，先建立或更新 `docs/feature-specs/active/` 下的 Spec。
3. 把验收标准写成可测试条目，并在 `## 验证映射` 中绑定验证入口。
4. 若任务跨模块或多阶段，建立或更新 `docs/exec-plans/active/`。
5. 实现代码、测试、文档和脚本。
6. 执行 `scripts/harness/check-feature-specs.sh` 与相关验证入口。
7. 最终交付时按 `docs/patterns/agent-delivery-handoff.md` 汇报。

## 常见风险

- 把 OpenSpec 当成业务需求文档。
- 验收标准只写“功能正常”，没有可测试断言。
- 执行计划没有引用 Feature Spec，导致实现和需求脱节。
- handoff 只列出改动，没有说明验收标准如何被验证。
