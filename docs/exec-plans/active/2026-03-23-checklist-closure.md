# Checklist Closure Hardening

## 背景

`docs/harness-engineering/CHECKLIST.md` 中仍保留一批 `[~]` 与 `[ ]` 项。它们虽然不影响上一轮封板结论，但已经成为后续扩展增强的主要缺口：约束还没有完全系统化为测试与脚本，agent 工作流资产还不够细，周期性治理与重复模式控制也还没有成为仓库默认回路。

## 目标

- 将本轮 checklist 中剩余未完成项逐项补成仓库事实
- 让新增能力至少具备“文档入口 + 可执行脚本/测试 + 验证方式”
- 把结果同步回写到质量看板、路线图与 checklist

## 非目标

- 本计划不引入新的外部部署平台或 SaaS 依赖
- 本计划不一次性把所有业务模块都补到同等深度的测试覆盖
- 本计划不重写现有微服务业务逻辑

## 范围

- `docs/harness-engineering/`
- `docs/exec-plans/`
- `scripts/dev/`
- `scripts/harness/`
- `scripts/smoke/`
- `artemis-symphony/`
- `artemis-auth/`
- `artemis-modules/artemis-system/`
- `openspec/specs/`
- `.github/workflows/`
- 根 `pom.xml` 与相关模块 `pom.xml`

## 风险

- 新守门如果阈值定得过高，可能让本地与 CI 出现噪音失败
- 重复模式扫描若实现过宽，可能带来误报
- 将更多检查接入 `verify` 后，构建时长会上升，需要保持可解释性
- 文档与脚本入口增多后，若索引不同步会形成新的漂移点

## 分步任务

1. 建立本计划并补充 OpenSpec，明确新增治理能力的边界
2. 为 `artemis-system` 补系统化层间约束测试，并扩展关键路径测试基线
3. 新增契约变更 / API 文档同步检查与覆盖率基线守门
4. 新增重复模式扫描与周期性治理任务
5. 补齐常见任务 runbook、细粒度 prompts/skills、自评与 reviewer 回路
6. 新增启动失败 / 慢启动 / 配置缺失的自动检查脚本与聚合 smoke
7. 回写 checklist、roadmap、quality score、README/AGENTS/docs 索引并完成验证

## 验证

- `bash -n $(find scripts -type f -name '*.sh' | sort)`
- `scripts/harness/verify-changed.sh working-tree`
- `scripts/harness/full-verify.sh`
- 针对新增 ArchUnit / 文档守门 / 覆盖率守门的 Maven 测试与 `verify`

## 决策记录

- `2026-03-23`：本轮按“先规范、再守门、后回写”推进，避免 checklist 更新先于能力落仓
- `2026-03-23`：对未完成项优先选择仓库内可重复执行的脚本、测试、runbook，而不是只写说明文字

## 遗留问题

- 若未来新增更多业务微服务，需要把本轮守门与 runbook 模式继续复制到新增服务
