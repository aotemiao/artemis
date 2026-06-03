# Artemis Threat Model

Status: maintained
Last Reviewed: 2026-06-02
Review Cadence: 90 days

本文件记录 Artemis 当前默认威胁模型，供人和 agent 在方案设计、Feature Spec、执行计划和最终 review 中复用。

## 保护目标

- 用户身份、token、角色、权限点和数据权限上下文。
- 租户、部门、角色、菜单、审计日志等管理后台核心数据。
- 内部 Dubbo client 契约和只能服务间调用的内部接口。
- Nacos、数据库、Redis、对象存储、Linear / Symphony 等外部系统凭证。
- agent workspace、运行日志、issue 评论和运行轨迹摘要中的敏感信息。

## 信任边界

| 边界 | 风险 | 默认要求 |
|------|------|----------|
| 外部客户端到 Gateway | 未登录、伪造 token、越权访问 | Gateway 必须校验登录态和最小 RBAC，并阻断内部接口 |
| Gateway 到业务服务 | 用户上下文伪造、角色透传不一致 | 下游服务不能盲信未受控来源的上下文头 |
| Auth 到 System client | 内部契约滥用、授权快照不一致 | 调用方只依赖 `*-client`，契约变更必须同步测试和文档 |
| App 到 Domain / Infra | 事务边界错位、业务规则泄漏到 infra | App 管事务，Domain 表达规则，Infra 只做技术实现 |
| Agent 到本地仓库 | 越权写文件、泄露密钥、执行外部副作用 | 使用 workspace sandbox、权限 runbook 和人工审批 |
| Symphony 到 Linear / SSH worker | 外部写回、远端执行、日志泄露 | 记录副作用，限制 worker 权限，敏感信息不进入评论 |

## 主要威胁

| 编号 | 威胁 | 典型场景 | 缓解措施 |
|------|------|----------|----------|
| TM-001 | 越权访问 | 普通用户访问管理接口或内部接口 | RBAC、数据权限、Gateway 内部接口阻断、无权限测试 |
| TM-002 | 授权快照陈旧 | 角色或权限变更后旧会话仍可访问 | 会话刷新、权限变更踢在线用户或缓存失效策略 |
| TM-003 | 重复提交 | 创建、授权、导入、回调重复执行 | 唯一约束、幂等键、状态机校验 |
| TM-004 | 并发写冲突 | 并发替换角色菜单、租户初始化、流程办理 | 事务、唯一约束、锁或乐观并发控制 |
| TM-005 | 跨服务一致性失败 | Auth 会话、System 权限、Gateway RBAC 不一致 | 明确同步时机、补偿或失效策略，smoke 覆盖关键链路 |
| TM-006 | SQL 性能退化 | 大列表、树查询、导出、权限过滤 | 分页、索引、查询范围、避免 N+1 |
| TM-007 | 敏感信息泄露 | token、密钥、连接串进入日志、trace、issue 评论 | 脱敏、禁止提交、低敏摘要 |
| TM-008 | Agent 外部副作用失控 | agent 静默部署、推送、删除、写外部系统 | `AGENT_PERMISSION_RUNBOOK.md`、sandbox、人工审批 |

## Review 要求

高风险改动必须在 handoff 中说明：

- 涉及哪些威胁编号。
- 哪些威胁由测试、smoke、harness 或人工验收覆盖。
- 哪些威胁仍有残余风险。

具体审查清单见 `docs/patterns/security-review-checklist.md`。
