# Symphony Linear 进度评论回写

## 背景

当前 `artemis-symphony` 已能从 Linear 拉取议题、创建工作区并驱动 Codex 执行，但默认只读不写。用户若在 Linear 中创建“进度汇报”类 issue，现阶段只能通过本地状态页、日志或工作区间接观察执行过程，无法直接在 Linear 里看到 Symphony 的处理进度。

## 目标

- 为 Symphony 增加可选的 Linear issue 评论回写能力
- 让 `WORKFLOW.md` 可以控制是否启用评论回写以及评论模板
- 让 `WORKFLOW.md` 可以按 issue 标题过滤评论回写范围，避免全项目评论噪音
- 确保评论写回失败时仅降级记录，不影响主调度、重试与工作区生命周期

## 非目标

- 本计划不新增自动改状态、自动改描述或自动关闭 issue 能力
- 本计划不引入除 Linear 之外的新 tracker 写回实现
- 本计划不把评论回写做成默认强制开启

## 范围

- `artemis-symphony-config` 中与 workflow 配置、模板渲染相关的能力
- `artemis-symphony-tracker` 中的 Linear GraphQL mutation 封装
- `artemis-symphony-orchestrator` 中 worker 生命周期结束时的进度评论逻辑
- `artemis-symphony/README.md`、`WORKFLOW.md.example`、根 README 与必要 runbook
- 相关单元测试与本地运行验证

## 风险

- 若每次 attempt 都评论，可能造成 issue 评论噪音；需要把能力设计为显式开启
- 评论模板若渲染失败，不应拖垮主流程，但要有可解释日志
- 写回 Linear 属于外部副作用，测试应优先覆盖 mutation 结构与降级行为，避免把真实工作区评论刷屏

## 分步任务

1. 明确评论回写的配置结构、默认值与模板上下文
2. 补充执行计划与 OpenSpec，固定“可选回写、失败降级”的规则
3. 在 LinearTrackerClient 中新增 issue comment mutation 封装
4. 在编排器 worker 退出路径挂接评论回写，并补必要模板渲染
5. 补测试、更新文档，并用本地服务与脚本验证运行状态

## 验证

- `source scripts/lib/common.sh && run_mvn test -pl artemis-symphony/artemis-symphony-orchestrator,artemis-symphony/artemis-symphony-tracker,artemis-symphony/artemis-symphony-config -am`
- `scripts/dev/check-service-config.sh symphony`
- `scripts/dev/run-symphony.sh`
- `scripts/smoke/symphony-state.sh`
- 手动触发 `POST /api/v1/refresh`，确认服务继续可用且无写回异常导致的主流程中断

通过标准：

- workflow 可显式开启 Linear 评论回写，且有默认模板
- LinearTrackerClient 能正确发出 `commentCreate` mutation
- 评论渲染或写回失败只打日志，不导致 worker/调度异常退出
- 相关测试与本地 smoke 通过

## 决策记录

- `2026-03-25`：评论回写按“每次 worker 尝试结束”触发，而不是按 token/turn 级别实时推送，避免对 Linear 造成过高评论频率
- `2026-03-25`：评论回写默认关闭，需在 `WORKFLOW.md` 中显式开启，避免现有项目无意中引入评论噪音

## 遗留问题

- 若后续需要更细粒度范围控制，可继续扩展到按 label 过滤
