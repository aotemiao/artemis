# Artemis Architecture

Status: maintained
Last Reviewed: 2026-03-23
Review Cadence: 90 days

## 仓库定位

Artemis 是一个基于 Java 21、Spring Boot 3.5、Spring Cloud 2025 的微服务管理后台脚手架，采用 DDD/COLA 分层，并通过 `artemis-symphony` 提供编码代理编排能力。

## 仓库模块

| 模块 | 作用 |
|------|------|
| `artemis-dependencies` | BOM 与统一依赖版本 |
| `artemis-framework` | 公共 starter，提供 core/web/security/jdbc/redis/log/doc 能力 |
| `artemis-gateway` | Spring Cloud Gateway，统一入口 |
| `artemis-auth` | 认证服务 |
| `artemis-modules/artemis-system` | 系统管理领域服务 |
| `artemis-visual` | 运维基础设施预留目录 |
| `artemis-symphony` | 基于 OpenAI Symphony SPEC 的编码代理编排服务 |

## 运行拓扑

本地开发默认依赖：

- PostgreSQL：`5432`
- Redis：`6379`
- Nacos：`8848`
- Gateway：`8080`
- Auth：`9200`
- System：`9300`

启动顺序建议：

1. `scripts/dev/up.sh`
2. 上传或校验 `config/nacos` 下配置
3. `scripts/dev/run-system.sh`
4. `scripts/dev/run-auth.sh`
5. `scripts/dev/run-gateway.sh`
6. `scripts/dev/health.sh`

## 领域服务分层

以 `artemis-system` 为例，遵循 COLA 五模块拆分：

| 子模块 | 责任 |
|--------|------|
| `-adapter` | REST Controller、Dubbo adapter、DTO、参数校验 |
| `-app` | CmdExe、QryExe、事务边界、用例编排 |
| `-domain` | 聚合、实体、值对象、Gateway 接口、领域规则 |
| `-infra` | Gateway 实现、Repository、DO、外部系统适配 |
| `-start` | Spring Boot 启动、配置、模块组装 |

依赖方向必须保持为：

`adapter -> app -> domain <- infra`

`start` 负责组装，不承载业务规则。

## 关键调用链

### 对外请求

浏览器或客户端
-> `artemis-gateway`
-> 对应业务服务 REST API

### 内部认证

`artemis-auth`
-> 依赖 `artemis-system-client`
-> 通过 Dubbo 调用 `artemis-system`

这条链路的核心约束是：调用方只能依赖 `*-client` 契约，不能直接依赖对方内部层。

## Harness Engineering 入口

本仓库的 Harness Engineering 不是单一工具，而是一套仓库内结构：

- `AGENTS.md`
  agent 的最小稳定入口
- `docs/harness-engineering/`
  落地清单、路线图、操作模型
- `docs/exec-plans/`
  复杂任务的执行计划与决策沉淀
- `scripts/dev/`
  本地环境标准入口、健康检查与日志入口
- `scripts/dev/check-service-config.sh`
  配置模板缺失的启动前断言
- `scripts/dev/check-service-readiness.sh`
  启动失败、慢启动、关键端点可达性的统一断言
- `scripts/dev/package-service.sh`
  统一服务打包入口
- `scripts/dev/build-image.sh`
  统一镜像构建入口
- `scripts/harness/`
  验证、规范同步、增量回路
- `scripts/harness/run-governance-checks.sh`
  文档、契约、重复模式与质量问题归档的治理入口
- `scripts/smoke/`
  面向 agent 与开发者的快速验证入口
- `artemis-symphony/WORKFLOW.md.example`
  让编排器默认按这套结构驱动 agent

## 适合 agent 的改动方式

- 小改动：读 `AGENTS.md` 与相关 spec，直接改代码并跑 `scripts/harness/verify-changed.sh`
- 跨模块改动：先补 `docs/exec-plans/active/` 计划，再实施
- 工程化改动：优先新增脚本和文档入口，再补约束与守门
- 行为或契约改动：同时更新 OpenSpec 与对外文档
- 常见模式任务：优先复用 `docs/harness-engineering/*RUNBOOK.md` 与 `artemis-symphony/skills/`

## 封板后的扩展方向

- 仍需持续扩展更多模块的契约检查、层间约束与覆盖率基线
- 仓库级知识入口已补齐，但仍需继续治理内容新鲜度与重复模式
- 容器化部署骨架已具备统一入口，仍需在真实环境持续演练
- smoke 与可观测性已覆盖核心链路，但还需继续补更多业务服务场景
