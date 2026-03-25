# Symphony Phase A 收口计划

## Phase 目标与完成标准

目标：
把 `PROJECT_PROGRESS_REPORT.md` 中定义的 Phase A 落成可执行的仓库计划，统一推进 Symphony 启用链路加固与 Linear 进度评论回写，避免“路线已写、执行仍分散”。

完成标准：

- `symphony-activation-hardening` 已完成并归档，`symphony-linear-progress-comments` 完成后同步归档
- Symphony 可在默认本地 workflow 下稳定启动，dispatch 预检失败时仍可降级暴露 HTTP 状态
- Linear 评论回写可按 workflow 显式开启，失败仅降级记录，不影响主调度链路
- Symphony 相关 README、workflow 示例、troubleshooting、脚本与 smoke 保持同步

## Checklist 拆分

- `[x]` 收口启动脚本、服务状态与降级启动链路
- `[x]` 收口 Linear 评论回写配置、模板、标题过滤与失败降级逻辑
- `[x]` 将模块测试、配置检查和 smoke 串成 Phase A 的固定验证回路
- `[x]` Phase A 完成后归档两项子计划，并同步更新项目进度汇报中的阶段状态

## 对应产物

代码：

- `scripts/dev/`
- `scripts/lib/`
- `artemis-symphony/artemis-symphony-start`
- `artemis-symphony/artemis-symphony-orchestrator`
- `artemis-symphony/artemis-symphony-tracker`
- `artemis-symphony/artemis-symphony-config`

脚本：

- `scripts/dev/check-service-config.sh symphony`
- `scripts/dev/run-symphony.sh`
- `scripts/dev/service-status.sh symphony`
- `scripts/smoke/symphony-state.sh`

文档：

- `docs/exec-plans/completed/2026-03-25-symphony-activation-hardening.md`
- `docs/exec-plans/completed/2026-03-25-symphony-linear-progress-comments.md`
- `docs/exec-plans/completed/2026-03-25-symphony-reference-alignment.md`
- `docs/exec-plans/completed/2026-03-25-symphony-live-e2e-alignment.md`
- `docs/harness-engineering/PROJECT_PROGRESS_REPORT.md`
- `artemis-symphony/README.md`
- `artemis-symphony/WORKFLOW.md.example`
- `docs/harness-engineering/SYMPHONY_TROUBLESHOOTING.md`

验证入口：

- `scripts/dev/check-service-config.sh symphony`
- `scripts/dev/run-symphony.sh`
- `scripts/dev/service-status.sh symphony`
- `scripts/smoke/symphony-state.sh`
- `source scripts/lib/common.sh && run_mvn test -pl artemis-symphony/artemis-symphony-start,artemis-symphony/artemis-symphony-orchestrator,artemis-symphony/artemis-symphony-tracker,artemis-symphony/artemis-symphony-config -am`
- `scripts/e2e/run-symphony-live-e2e.sh`
- `scripts/harness/verify-changed.sh`

## 关联计划

- `docs/exec-plans/completed/2026-03-25-symphony-activation-hardening.md`
- `docs/exec-plans/completed/2026-03-25-symphony-linear-progress-comments.md`
- `docs/exec-plans/completed/2026-03-25-symphony-reference-alignment.md`
- `docs/exec-plans/completed/2026-03-25-symphony-live-e2e-alignment.md`

说明：
本计划负责统筹 Phase A 的优先级、验证顺序与完成后回写，不替代上述两个子计划里的实现细节。

当前状态：
`symphony-activation-hardening`、`symphony-linear-progress-comments`、`symphony-reference-alignment` 与 `symphony-live-e2e-alignment` 已全部完成并归档，Phase A 已收口。

## 风险与回滚

风险：

- 启动脚本和降级启动路径若处理不一致，容易出现“服务已起但 dispatch 不可用”的误判
- 评论回写属于外部副作用，若模板或 mutation 失败处理不当，可能把 Symphony 主流程拖入异常
- 两项子计划若各自推进但不统一回写，项目进度汇报会再次落后于仓库事实

回滚方式：

- 启动链路与评论回写能力分别按子计划粒度回退，不做跨计划的整体硬回滚
- 若 Phase A 状态判断与仓库事实不一致，先回退进度汇报中的阶段口径，再重新核对 active / completed plan

## 完成后需要更新的仓库事实

- 将 Phase A 相关子计划迁移到 `docs/exec-plans/completed/`
- 更新 `docs/harness-engineering/PROJECT_PROGRESS_REPORT.md` 中 Phase A 的状态与完成度判断
- 若评论回写规则或默认 workflow 约束发生稳定变化，同步更新相关 OpenSpec 与 runbook
