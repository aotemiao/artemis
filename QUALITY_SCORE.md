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
| 仓库可读性 | `5/5` | 已有 README、AGENTS、ARCHITECTURE、docs 索引、专项 runbook 与质量问题目录 | 继续在新增模块复用这套入口，避免回到聊天记忆 |
| 工程验证闭环 | `5/5` | `mvn verify`、治理脚本、OpenSpec 校验、本地 full-verify 与 CI 已形成统一入口 | 继续收敛误报并扩展到新增业务模块 |
| 本地环境确定性 | `5/5` | Docker Compose、固定端口、wait-http、config/readiness 断言、聚合 smoke、服务打包与镜像入口已齐备 | 后续按新增服务持续补同样的 readiness 模式 |
| 架构约束可执行性 | `4/5` | 已有 OpenSpec、认证依赖约束与 `artemis-system` 分层 ArchUnit 测试 | 继续把同类规则扩展到更多微服务 |
| Agent 工作流成熟度 | `5/5` | 已有 `artemis-symphony`、exec-plans、runbook、skills、prompts 与 agent review loop | 后续继续扩展更多任务型 assets |
| 文档新鲜度控制 | `5/5` | 核心文档审阅元信息、freshness 守门、周期性治理工作流与质量问题归档标准已建立 | 继续保持 cadence 回写并清理历史冗余 |
| Smoke / 可观测性 | `5/5` | 已有 system/auth/gateway/symphony smoke、聚合 smoke、健康检查、readiness 断言、日志与状态入口 | 后续扩展到新增业务服务和更多故障场景 |
| 部署骨架完整度 | `4/5` | 已有 Dockerfile、统一镜像构建脚本、部署回滚 runbook 与 CI 镜像构建 | 在真实环境持续演练部署与回滚 |

## 封板后的扩展方向

优先级从高到低：

1. 将新增业务模块继续纳入契约检查、覆盖率基线和分层 ArchUnit
2. 在真实环境持续演练镜像、部署与回滚
3. 为更多服务补同等强度的 smoke / readiness / troubleshooting
4. 持续清理历史重复模式与遗留技术债
5. 继续让复杂任务先落 `docs/exec-plans/active/`

## 2026-03-23 验证快照

- 本地已实际通过 `scripts/harness/full-verify.sh`
- 本地已实际通过 `artemis-symphony` 子工程 `mvn verify`
- CI 工作流已补 OpenSpec diff 检查与统一 `full-verify` 入口
- 本地已补 `auth / gateway / symphony` smoke 与 HTTP 等待入口
- docs 索引、docs consistency、markdown link check 已补齐
- 文档 freshness policy、审阅元信息与周期性 CI 巡检已补齐
- 服务打包、镜像构建、部署 / 回滚、Symphony 故障处理入口已补齐
- 已补契约 / API 文档同步检查、关键路径测试基线、重复模式扫描与质量问题归档标准
- 已补服务配置检查、启动就绪断言与聚合 smoke
- 已补常见任务 runbook、skills / prompts 与 agent review loop

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
- 新增契约 / API 文档同步检查脚本
- 新增关键路径测试基线与 JaCoCo 覆盖率基线
- 新增重复模式扫描、质量问题归档目录与治理工作流
- 新增常见任务 runbook、skills / prompts 与 agent review loop
- 新增服务配置检查、readiness 断言与聚合 smoke

## 封板范围外的扩展方向

- 还没有把新增守门扩展到未来的所有业务微服务
- 还没有在真实部署环境持续演练新增的部署 / 回滚 runbook
