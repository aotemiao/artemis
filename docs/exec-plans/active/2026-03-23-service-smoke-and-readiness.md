# Service Smoke And Readiness Hardening

## 背景

仓库已经具备根入口文档、`scripts/harness` 验证入口和仓库级 `full-verify`，但服务层的“启动成功断言 + smoke 入口 + 常见执行顺序”还不够完整。为了让 agent 和开发者能按同一套回路判断服务是否真的可用，需要把这部分也落成仓库事实。

## 目标

- 为 `auth`、`gateway`、`symphony` 补齐可执行 smoke 脚本
- 增加可复用的 HTTP 等待 / 断言入口
- 让 `artemis-symphony` 默认具备一个可观测 HTTP 端口
- 把服务 smoke 的执行顺序和入口沉淀成 runbook

## 非目标

- 本计划不覆盖所有微服务的全部 smoke 场景
- 本计划不引入新的外部依赖或部署平台能力

## 范围

- `scripts/dev/`
- `scripts/smoke/`
- `docs/harness-engineering/`
- `AGENTS.md`
- `README.md`
- `QUALITY_SCORE.md`
- `docs/harness-engineering/CHECKLIST.md`
- `docs/harness-engineering/ROADMAP.md`

## 分步任务

- `[x]` 新增可复用 HTTP 等待脚本 `scripts/dev/wait-http.sh`
- `[x]` 调整 `scripts/dev/run-symphony.sh`，默认启用可观测 HTTP 端口
- `[x]` 新增 `scripts/smoke/auth-refresh.sh`
- `[x]` 新增 `scripts/smoke/gateway-auth-refresh.sh`
- `[x]` 新增 `scripts/smoke/symphony-state.sh`
- `[x]` 新增服务 smoke runbook
- `[x]` 更新仓库入口文档与质量状态
- `[x]` 运行脚本语法检查与仓库级验证

## 验证

- `bash -n $(find scripts -type f -name '*.sh' | sort)` 通过
- 新增 smoke 脚本具备 `--help` 等价的自解释用法或默认参数
- `scripts/harness/full-verify.sh` 通过
- `2026-03-23` 已完成计划内全部步骤并回写状态

## 决策记录

- `2026-03-23`：本轮只围绕一个执行计划文件推进，避免建议和执行脱节
