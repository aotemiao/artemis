# Harness Engineering Checklist

Status: maintained
Last Reviewed: 2026-03-23
Review Cadence: 90 days

本清单强调“可直接落地”。每一项都应尽量对应仓库中的文档、脚本、测试或自动化入口，而不是抽象口号。

封板说明：

- 当前仓库已达到本轮 Harness Engineering 封板范围
- 本清单中仍保留的 `[ ]` 项属于后续扩展增强，不影响当前封板结论

状态说明：

- `[x]` 已落仓
- `[~]` 已有基础，需继续补强
- `[ ]` 尚未开始

## A. 仓库知识成为系统事实来源

- `[x]` 提供根入口文档 `AGENTS.md`
- `[x]` 提供全局架构地图 `ARCHITECTURE.md`
- `[x]` 提供质量看板 `QUALITY_SCORE.md`
- `[x]` 提供 Harness 专项清单 `docs/harness-engineering/CHECKLIST.md`
- `[x]` 提供阶段路线图 `docs/harness-engineering/ROADMAP.md`
- `[x]` 提供执行计划目录 `docs/exec-plans/`
- `[x]` 提供执行计划模板 `docs/exec-plans/templates/execution-plan-template.md`
- `[~]` OpenSpec 已沉淀工程规范，但仍需覆盖更多具体场景
- `[x]` 建立 docs 索引与交叉链接检查
- `[x]` 建立文档过期扫描与定期整理机制

## B. 让 agent 有稳定执行入口

- `[x]` 提供标准本地启动脚本 `scripts/dev/up.sh`
- `[x]` 提供标准关闭脚本 `scripts/dev/down.sh`
- `[x]` 提供关键服务启动脚本 `scripts/dev/run-system.sh`
- `[x]` 提供关键服务启动脚本 `scripts/dev/run-auth.sh`
- `[x]` 提供关键服务启动脚本 `scripts/dev/run-gateway.sh`
- `[x]` 提供 Symphony 启动脚本 `scripts/dev/run-symphony.sh`
- `[x]` 提供统一健康检查脚本 `scripts/dev/health.sh`
- `[x]` 提供服务日志查看脚本 `scripts/dev/tail-log.sh`
- `[x]` 提供增量验证脚本 `scripts/harness/verify-changed.sh`
- `[x]` 提供全量验证脚本 `scripts/harness/full-verify.sh`
- `[x]` 提供 OpenSpec 同步检查脚本 `scripts/harness/check-openspec-sync.sh`
- `[x]` 提供最小 smoke 脚本 `scripts/smoke/system-lookup.sh`
- `[~]` Symphony 已具备编排器，但 workflow 模板还需继续细化
- `[~]` 为更多服务补 smoke 脚本
- `[~]` 为启动失败、慢启动、配置缺失补自动检查脚本

## C. 让约束可验证，而不只是写在文档里

- `[~]` 已有 Checkstyle、SpotBugs、测试依赖与 pre-commit
- `[x]` 将 Spotless、Checkstyle、SpotBugs 绑定到 `mvn verify`
- `[~]` 已有部分 ArchUnit 与集成测试
- `[ ]` 将层间依赖约束系统化扩展到更多模块
- `[x]` 增加基础 CI 工作流执行标准验证
- `[x]` 将 OpenSpec 同步规则接入 CI 差异比较逻辑
- `[ ]` 增加契约变更检查与 API 文档同步检查
- `[ ]` 增加覆盖率与关键路径测试基线

## D. 让 agent 按仓库结构工作，而不是按聊天记忆工作

- `[x]` 强化 `artemis-symphony/WORKFLOW.md.example`，要求 agent 先读仓库入口文档
- `[x]` 规定复杂任务优先落执行计划
- `[x]` 规定代码、规范、脚本、文档尽量成组交付
- `[ ]` 为常见任务补专用 runbook，例如“新增领域服务”“新增 Dubbo client”“补 ArchUnit 约束”
- `[ ]` 为 agent 建立更细粒度的 skills / prompts
- `[ ]` 建立 agent 自评与 reviewer 回路

## E. 控制熵增，避免 AI slop 扩散

- `[x]` 建立质量评分入口 `QUALITY_SCORE.md`
- `[x]` 建立执行计划沉淀目录
- `[~]` pre-commit 已能提醒 OpenSpec 未同步
- `[ ]` 建立重复模式扫描，例如重复 DTO、重复异常映射、重复脚本
- `[ ]` 建立周期性“文档整理 / 工程整理”任务
- `[ ]` 建立质量问题的归档与关闭标准

## F. 提升环境与可观测性 legibility

- `[~]` 已有固定端口、Docker Compose、日志文件与 Symphony 状态 API
- `[x]` 为系统服务补最小 HTTP smoke
- `[x]` 给 agent 增加统一健康检查入口
- `[x]` 给 agent 增加日志检索入口
- `[~]` 给 agent 增加启动成功、依赖就绪、关键端点可达的断言脚本
- `[x]` 给 `artemis-symphony` 增加本仓库常见故障 runbook

## G. 面向交付的工程骨架

- `[~]` 已有多模块、环境 profile、Nacos 模板、Compose
- `[x]` 为关键 `-start` 模块补 Dockerfile
- `[x]` 为服务启动与打包补统一命令入口
- `[x]` 为部署与回滚补 runbook
- `[x]` 为 CI 输出补基础工作流

## 当前建议的推进顺序

1. 先把仓库级入口文档与脚本入口固定下来
2. 再把脚本验证上收为 Maven / CI 守门
3. 再把 smoke、日志、健康检查做成 agent 可复用能力
4. 最后补容器化与文档治理的持续机制
