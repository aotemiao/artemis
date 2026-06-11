# Quality Score

Status: maintained
Last Reviewed: 2026-06-10
Review Cadence: 90 days

本文件用于把“仓库当前质量”表达成 agent 和开发者都能快速理解的信号，而不是只靠感觉。

评分规则：

- `1/5` 缺失或高度依赖人工记忆
- `2/5` 有零散能力，但未形成稳定回路
- `3/5` 已有基础能力，可用于日常开发
- `4/5` 有明确入口、验证和守门
- `5/5` 基本实现自动化、自解释、低漂移

## 封板结论

截至 2026-03-23，Artemis 已达到本轮 Harness Engineering 封板标准：

- 核心入口文档、执行脚本、验证脚本、runbook、CI 守门已形成闭环
- 文档一致性、链接有效性、审阅日期与 OpenSpec 同步都能通过脚本重复校验
- 人与 agent 都可以沿“读文档 -> 启动服务 -> smoke -> verify -> 构建镜像”完成标准工作回路

## 当前评分

| 维度 | 分数 | 现状 | 下一步 |
|------|------|------|--------|
| 仓库可读性 | `5/5` | 已有 README、AGENTS、ARCHITECTURE、docs 索引、专项 runbook 与质量问题目录 | 继续在新增模块复用这套入口，避免回到聊天记忆 |
| 工程验证闭环 | `5/5` | `mvn verify`、治理脚本、OpenSpec 校验、本地 full-verify 与 CI 已形成统一入口；API 文档同步已覆盖 `artemis-system` Controller 自动发现 | 继续收敛误报并扩展到新增业务模块 |
| 本地环境确定性 | `5/5` | Docker Compose、固定端口、wait-http、config/readiness 断言、聚合 smoke、服务打包与镜像入口已齐备 | 后续按新增服务持续补同样的 readiness 模式 |
| 架构约束可执行性 | `4/5` | 已有 OpenSpec、认证依赖约束与 `artemis-system` 分层 ArchUnit 测试 | 继续把同类规则扩展到更多微服务 |
| Agent 工作流成熟度 | `5/5` | 已有 `artemis-symphony`、exec-plans、runbook、skills、prompts、dynamic tool registry、agent review loop、权限策略和 adversarial review 入口 | 后续继续基于真实业务补更多任务型 assets，并把更多工具纳入 registry 与审计 |
| Agent 运行质量可评测性 | `4/5` | 已有 agent eval fixture、可执行 memory dataset、静态 eval 脚本和 `scripts/e2e/run-symphony-agent-eval.sh`，可验证 Symphony memory tracker、fake Codex app-server、workspace 产物、SQLite 运行历史、权限预检失败路径、低敏 JSON summary 和运行环境快照；失败 run 可先生成需人工复核的 eval dataset 草稿 | 继续扩展更多 memory case 和受显式凭证保护的 Linear / live eval |
| 文档新鲜度控制 | `5/5` | 核心文档审阅元信息、freshness 守门、周期性治理工作流与质量问题归档标准已建立 | 继续保持 cadence 回写并清理历史冗余 |
| Smoke / 可观测性 | `5/5` | 已有 system/auth/gateway/symphony smoke、聚合 smoke、健康检查、readiness 断言、日志、状态入口、SQLite 运行历史、`/api/v1/history/metrics`、`/runs` 指标摘要、低敏 JSON run summary、run environment 快照、Harness metrics report 生成器、CI artifact 快照入口、GitHub event 低敏 delivery signal 采集、deploy drill report 指标摘要和权限 preflight / audit 字段 | 后续把指标快照扩展为 GitHub API 深度抓取、per-run 环境隔离和跨平台 dashboard |
| 部署骨架完整度 | `4/5` | 已有 Dockerfile、统一镜像构建脚本、部署回滚 runbook、CI 镜像构建和可聚合 deploy / rollback drill 摘要 | 在真实环境持续演练部署与回滚 |

## 封板后的扩展方向

优先级从高到低：

1. 将新增业务模块继续纳入契约检查、覆盖率基线和分层 ArchUnit
2. 在真实环境持续演练镜像、部署与回滚
3. 为更多服务补同等强度的 smoke / readiness / troubleshooting
4. 持续清理历史重复模式与遗留技术债
5. 继续把复杂任务先落 `docs/exec-plans/active/`，并通过 `scripts/harness/check-openspec-change-state.sh` 等守门定期归档已完成计划和 OpenSpec change，避免 active 区噪声扩大

## 验证与变更快照

详细完成清单以 `docs/governance/CHECKLIST.md` 和 `docs/reports/ROADMAP.md` 为准，本文件只保留质量信号，避免和治理文档重复。

- `2026-03-23`：本地通过 `scripts/harness/full-verify.sh`，CI、smoke、docs consistency、freshness、OpenSpec diff、契约 / API 文档同步、关键路径测试基线和部署 / 回滚 runbook 完成封板闭环。
- `2026-06-02`：完成 agentic harness 优化，新增资产见 `docs/exec-plans/completed/2026-06-02-agentic-harness-optimization.md`，守门入口为 `scripts/harness/check-agentic-harness-assets.sh`。
- `2026-06-09`：根据 Harness Engineering 市场差距报告，补强 API 文档自动发现、OpenSpec active change 状态守门和低敏 agent run 摘要样例；详见 `docs/exec-plans/completed/2026-06-09-harness-market-gap-improvements.md`。
- `2026-06-09`：继续补强 Symphony 真实运行闭环，新增可执行 memory agent eval、权限预检失败 eval、自动 JSON run summary、SQLite 运行历史、`/api/v1/history/metrics`、`/runs` 运行指标摘要、Harness metrics report 生成器和权限 preflight；详见 `docs/exec-plans/completed/2026-06-09-symphony-executable-agent-eval.md`。
- `2026-06-10`：本地通过 `scripts/harness/verify-changed.sh working-tree`，覆盖 governance、Symphony 资产检查与受当前工作树影响的 Symphony 模块 scoped verify。

## 封板范围外的扩展方向

- 还没有把新增守门扩展到未来的所有业务微服务
- 还没有在真实部署环境持续演练新增的部署 / 回滚 runbook
- 还需要将 executable agent eval 扩展到更多 memory 任务和受显式凭证保护的 Linear / live 场景
- Harness metrics 快照已接入 CI artifact，并支持 GitHub Actions event 低敏 PR / review finding delivery signal、run environment 分布和 deploy drill report 分布；尚未扩展为 GitHub API 深度抓取、per-run 环境隔离或跨平台 dashboard
