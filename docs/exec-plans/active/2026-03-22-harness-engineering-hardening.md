# Harness Engineering Hardening

## 背景

Artemis 已具备多模块微服务、OpenSpec、部分测试、Symphony 编排器和 pre-commit，但在仓库级 agent 入口、验证脚本、执行计划沉淀、工作流模板方面仍有明显缺口。当前已补齐第一轮骨架，需要继续把这些入口变成默认工作方式和真实守门。

## 目标

- 把仓库级 Harness Engineering 结构从“有入口”推进到“能稳定驱动改动”
- 逐步把验证脚本上收为 Maven / CI 守门
- 为关键服务补齐 smoke、容器化和可观测性入口

## 非目标

- 本计划不在第一轮直接重写整个构建体系
- 本计划不一次性覆盖全部模块的所有工程治理能力

## 范围

本计划关注：

- 仓库根文档和目录结构
- `scripts/dev`、`scripts/harness`、`scripts/smoke`
- `artemis-symphony/WORKFLOW.md.example`
- Maven / CI / Dockerfile 的后续补强路线

本计划暂不直接覆盖：

- 所有业务模块的领域细节
- 外部部署平台的具体实现

## 风险

- 如果过早把未验证的规则直接绑定到 lifecycle，可能造成构建噪音或误报
- smoke 脚本先从最小闭环开始，覆盖不足会导致“有脚本但信号不够强”
- CI 中的 diff 计算依赖 Git 历史与事件上下文，后续仍需观察首轮运行结果

## 分步任务

1. 固化根入口文档、Harness 清单、执行计划目录与基础脚本
2. 将 `scripts/harness` 变成团队默认验证入口，并补 README / runbook 引导
3. 在 Maven 中逐步接实 `verify` 守门，优先覆盖 Checkstyle、Spotless、测试
4. 为更多服务补 smoke 与健康检查脚本
5. 为关键 `-start` 模块补 Dockerfile 或等价容器化模板
6. 建立文档整理、质量评分更新、重复模式治理机制

## 验证

- 文档存在且互相可发现
- 脚本具备可执行权限并通过 `bash -n`
- `README.md` 与 `WORKFLOW.md.example` 能把人和 agent 导向统一入口
- `mvn verify` 能执行标准质量门
- `scripts/harness/full-verify.sh` 在本地实跑成功
- CI 能对 PR / push 执行 OpenSpec diff 检查
- 后续每完成一个阶段，更新 `QUALITY_SCORE.md` 与 `docs/harness-engineering/CHECKLIST.md`

## 决策记录

- `2026-03-22`：先补仓库级结构和脚本，再推进 Maven lifecycle 与 CI 守门，避免在无 `mvn` 环境下引入高风险构建改动
- `2026-03-22`：在 Maven 可用后优先接通 `verify`、CI、health 与 Docker 模板，让仓库默认沿 Harness 回路工作
- `2026-03-22`：本地 `scripts/harness/full-verify.sh` 已跑通，CI 改为补 OpenSpec diff 检查并复用仓库级验证入口

## 遗留问题

- 需要后续补一个以 CI 为中心的执行计划
- 需要后续补一个以容器化与部署骨架为中心的执行计划
