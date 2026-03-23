# Quality Score

Status: maintained
Last Reviewed: 2026-03-23
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
| 仓库可读性 | `4/5` | 已有 README、AGENTS、ARCHITECTURE、docs 索引和 Harness 文档目录 | 继续补文档新鲜度治理和重复内容收敛 |
| 工程验证闭环 | `5/5` | `mvn verify`、docs 守门、OpenSpec 校验、本地 full-verify 与 CI 已形成统一入口 | 继续收敛误报并扩展契约/覆盖率类约束 |
| 本地环境确定性 | `4/5` | Docker Compose、固定端口、wait-http、health、smoke、服务打包与镜像入口已齐备 | 继续补更多业务服务场景和配置装载自动化 |
| 架构约束可执行性 | `3/5` | 已有 OpenSpec 与部分 ArchUnit 测试 | 将更多层间约束转成可执行测试 |
| Agent 工作流成熟度 | `4/5` | 已有 `artemis-symphony`、AGENTS、exec-plans、smoke runbook 与 troubleshooting | 把更多专项 runbook 和 skills 接进 workflow |
| 文档新鲜度控制 | `4/5` | 核心文档已补审阅元信息、freshness 守门与 CI 周期性巡检 | 继续收敛重复内容并保持按 cadence 回写 |
| Smoke / 可观测性 | `4/5` | 已有 system/auth/gateway/symphony smoke、健康检查、状态接口与服务 smoke runbook | 继续补齐更多服务的启动断言与故障 runbook |
| 部署骨架完整度 | `4/5` | 已有 Dockerfile、统一镜像构建脚本、部署回滚 runbook 与 CI 镜像构建 | 在真实环境持续演练部署与回滚 |

## 封板后的扩展方向

优先级从高到低：

1. 为更多服务补 smoke、健康检查和日志检索入口
2. 让复杂任务先落 `docs/exec-plans/active/`，减少上下文散失
3. 扩展契约检查、覆盖率基线和层间约束
4. 在真实环境持续演练镜像、部署与回滚
5. 为文档建立“发现入口 + 定期清理”机制

## 2026-03-23 验证快照

- 本地已实际通过 `scripts/harness/full-verify.sh`
- 本地已实际通过 `artemis-symphony` 子工程 `mvn verify`
- CI 工作流已补 OpenSpec diff 检查与统一 `full-verify` 入口
- 本地已补 `auth / gateway / symphony` smoke 与 HTTP 等待入口
- docs 索引、docs consistency、markdown link check 已补齐
- 文档 freshness policy、审阅元信息与周期性 CI 巡检已补齐
- 服务打包、镜像构建、部署 / 回滚、Symphony 故障处理入口已补齐

## 本轮已完成

- 新增 `AGENTS.md`
- 新增 `ARCHITECTURE.md`
- 新增 `docs/harness-engineering/` 清单与路线图
- 新增 `docs/exec-plans/` 目录与模板
- 新增 `scripts/dev/`、`scripts/harness/`、`scripts/smoke/` 基础入口
- 强化 `artemis-symphony/WORKFLOW.md.example` 以贴合仓库结构
- 将 Spotless、Checkstyle、SpotBugs 接入 `mvn verify`
- 新增 CI 工作流与关键服务 Dockerfile 模板
- 新增健康检查与日志查看脚本
- 在本地实际跑通 `scripts/harness/full-verify.sh`
- 将 OpenSpec diff 检查接入 CI 工作流
- 新增服务 smoke runbook 与 `wait-http` 断言入口
- 新增 `auth / gateway / symphony` smoke 脚本
- 新增 docs 总索引与文档一致性守门
- 新增文档 freshness policy、审阅元信息与 freshness 守门
- 新增统一服务打包与镜像构建脚本
- 新增部署 / 回滚 runbook 与 Symphony troubleshooting runbook
- 将 docs 守门与镜像构建接入 CI

## 封板范围外的扩展方向

- 还没有为所有服务补齐 smoke 和 health 断言
- 还没有建立覆盖率基线与契约变化守门
- 还没有在真实部署环境持续演练新增的部署 / 回滚 runbook
