# Harness Freeze Plan

Status: completed
Last Reviewed: 2026-03-23
Review Cadence: 90 days

## 背景

Artemis 已经具备 Harness Engineering 主骨架，但还没有进入“封板状态”：文档存在漂移苗头，仓库缺少 docs 总索引，服务打包和镜像构建还没有统一入口，部署/回滚与常见故障 runbook 不完整，CI 也还没有覆盖镜像构建层面的回归。

## 封板定义

本计划中的“封板状态”指的是：

- 核心入口文档、执行脚本、验证脚本、runbook、CI 守门之间没有明显漂移
- 人与 agent 都可以通过仓库内入口完成“读文档 -> 启动服务 -> smoke -> verify -> 构建镜像”闭环
- 关键工程事实能够通过脚本和 CI 被重复校验，而不是只靠人工记忆

## 目标

- 建立 docs 索引和文档防漂移守门
- 建立统一的服务打包和镜像构建入口
- 建立部署/回滚和 Symphony 故障处理 runbook
- 将新的守门接入仓库级 `full-verify` 与 CI

## 非目标

- 不在本轮引入外部部署平台实现
- 不在本轮覆盖所有业务服务的全部集成场景

## 分步任务

- `[x]` 建立封板定义与执行计划文件
- `[x]` 新增 docs 总索引与文档相关守门脚本
- `[x]` 修正文档漂移并统一核心入口文档
- `[x]` 新增统一服务打包脚本
- `[x]` 新增统一镜像构建脚本
- `[x]` 新增部署 / 回滚 runbook
- `[x]` 新增 Symphony 故障 runbook
- `[x]` 将文档守门与镜像构建接入 `full-verify` / CI
- `[x]` 完成脚本语法检查、仓库级验证，并回写文档状态

## 验证

- `bash -n $(find scripts -type f -name '*.sh' | sort)` 通过
- `scripts/harness/check-doc-links.sh` 通过
- `scripts/harness/check-doc-consistency.sh` 通过
- `scripts/harness/check-doc-freshness.sh` 通过
- `scripts/harness/full-verify.sh` 通过
- CI 工作流包含 docs 守门与镜像构建步骤
- CI 工作流已增加周期性巡检触发
- `scripts/dev/build-image.sh` 已补 Docker daemon 前置检查
- 本计划内全部任务回写为完成
