## 1. 工作流与规范

- [x] 1.1 新增 `docs/harness-engineering/AGENT_DEVELOPMENT_WORKFLOW.md`，说明分工、分流规则、需求模板与执行步骤
- [x] 1.2 新增 `openspec/specs/agent-development-workflow/spec.md`，固化稳定约束
- [x] 1.3 为本次规则变化新增 `openspec/changes/agent-development-workflow-normalization/` 轨迹文件

## 2. Symphony 资产

- [x] 2.1 新增 `artemis-symphony/prompts/agent-requirement-intake.md`，沉淀需求受理 prompt

## 3. 验证

- [x] 3.1 对新增文件执行 `git diff --cached --check`
- [x] 3.2 检查新增 Markdown 文件链接可解析
- [x] 3.3 检查新工作流文档元信息与新 spec 结构
- [x] 3.4 记录 `scripts/harness/verify-changed.sh` 因 CRLF 无法直接执行的现状
