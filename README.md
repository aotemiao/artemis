# Artemis

Status: maintained
Last Reviewed: 2026-05-29
Review Cadence: 90 days

Spring Cloud 微服务管理后台脚手架，采用 DDD/COLA 分层架构，参考 RuoYi-Cloud-Plus 与 COLA，面向 Clean Code 与可维护性。

## 技术栈

- **JDK 21** · **Spring Boot 3.5** · **Spring Cloud 2025**
- **PostgreSQL** 数据库（本地默认）· **Nacos** 注册/配置中心 · **Sa-Token** 认证 · **Spring Data JDBC**（默认持久化）· **MyBatis-Plus**（可选）· **Redisson** 缓存与分布式锁
- **Spring Cloud Gateway** 网关

## 模块结构

```
artemis/
├── artemis-dependencies     # BOM 依赖版本
├── artemis-framework        # 公共 starter
│   ├── artemis-framework-core
│   ├── artemis-framework-web
│   ├── artemis-framework-security
│   ├── artemis-framework-mybatis
│   ├── artemis-framework-jdbc
│   ├── artemis-framework-redis
│   ├── artemis-framework-log
│   └── artemis-framework-doc
├── artemis-gateway          # 网关
├── artemis-auth             # 认证服务
├── artemis-modules          # 业务微服务（按领域拆分，对外 REST API、对内 *-client + Dubbo）
│   ├── artemis-system       # 系统管理 (client / adapter / app / domain / infra / start)
│   ├── artemis-resource     # 资源管理服务 (client / adapter / app / domain / infra / start)
│   └── artemis-workflow     # 工作流服务 (client / adapter / app / domain / infra / start)
├── artemis-visual           # 运维基础设施（按需扩展）
└── artemis-symphony         # Symphony：编码代理编排（WORKFLOW.md + Linear + Codex，可独立运行；见子目录 README）
```

## 如何最快了解项目

如果你第一次关注 Artemis，建议按下面顺序阅读，而不是从全部目录开始扫：

1. `README.md`
   了解项目定位、技术栈、模块结构、启动方式和 Harness 分层。
2. `ARCHITECTURE.md`
   了解服务拓扑、DDD/COLA 分层、内部 Dubbo client 契约和核心调用链。
3. `QUALITY_SCORE.md`
   了解当前工程质量、已封板能力和下一阶段优先级。
4. 下方“文档与目录职责”和“常见问题定位”
   按资产类型直接跳转到 runbook、治理规则、报告、执行计划或 Symphony 资产。
5. `openspec/specs/`
   查看稳定规则，例如模块边界、契约治理、质量门和默认 agent workflow。

需要实际开发或验证时，再进入 `AGENTS.md`、`docs/agent-workflow/AGENT_DEVELOPMENT_WORKFLOW.md`、相关 runbook 与 `scripts/` 入口。

## 快速开始

1. **启动基础设施**

   本地默认使用 **PostgreSQL**、Redis、Nacos（与 `config/nacos/datasource.yml` 一致）。首次从旧版 MySQL 编排切换时，可先执行 `docker-compose down -v` 再启动。

   ```bash
   cd docker && docker-compose up -d
   ```

2. **编译**

   ```bash
   mvn clean install -DskipTests
   ```

3. **运行系统服务**

   ```bash
   scripts/dev/run-system.sh
   ```

4. **运行认证服务与网关**

   ```bash
   scripts/dev/run-auth.sh
   scripts/dev/run-gateway.sh
   ```

5. **（可选）运行 Symphony**

   先复制 `artemis-symphony/WORKFLOW.md.example` 为仓库根 `WORKFLOW.md`，并按需设置 `LINEAR_API_KEY`。若希望 Symphony 在处理 issue 后把进度摘要评论回写到 Linear，可在 workflow 中开启 `reporting.linear_comments.enabled: true`。脚本默认会打开本地 `9500` 端口用于观测；如需自定义，可直接追加 `--server.port=...`，也兼容旧写法 `-Dspring-boot.run.arguments=...`。

   ```bash
   scripts/dev/run-symphony.sh
   ```

   若要执行与官方 `openai/symphony` 对齐的真实 live e2e 演练，可显式提供 `LINEAR_API_KEY` 后运行：

   ```bash
   scripts/e2e/run-symphony-live-e2e.sh
   ```

   若未提供真实 SSH worker，脚本会自动使用 `artemis-symphony/test-support/live-e2e-docker/` 中的 docker fallback worker；该 fallback 会保留官方默认 `workspace-write` sandbox 语义，并可通过 `SYMPHONY_LIVE_E2E_KEEP_ARTIFACTS=1` 保留失败现场。

6. **（可选）编译 Symphony 子工程**

   须在**仓库根目录**执行，以便继承根 BOM 与 `dependencyManagement`（勿仅在 `artemis-symphony` 目录单独 `mvn compile`）。详见 [`artemis-symphony/README.md`](artemis-symphony/README.md)。

   ```bash
   mvn compile -pl artemis-symphony/artemis-symphony-start -am
   ```

说明：项目源码目标版本为 **JDK 21**。`mvn verify` 的质量门也按 **JDK 21** 运行时固化；若本机默认 Java 不是 21，优先使用仓库下的 `scripts/` 入口，它们会在 macOS 上自动优先选择已安装的 Java 21。

## 多环境

- `dev`（默认）· `test` · `prod`，通过 Maven profile 切换；`spring.profiles.active` 由打包时 `@profiles.active@` 注入。
- Nacos 地址、group、username/password 由根 `pom.xml` 的 profile 定义（dev 默认 `127.0.0.1:8848`），生产构建可 `-Pprod` 或 `-Dnacos.server=...` 覆盖。

## Nacos 配置（参考 RuoYi script/config/nacos）

- 各服务仅保留**一个** `application.yml`（含 `---` 分段与 Nacos config.import），与 RuoYi 一致。
- Nacos 中需提前按 **`config/nacos`** 目录下的模板创建配置（Data ID：application-common、datasource、artemis-xxx）。数据源模板为 PostgreSQL，默认库名 `artemis_system`，与 `config/nacos/datasource.yml` 及本地 docker-compose 一致。详见 `config/nacos/README.md`。

## 规范与质量

- 代码风格：Checkstyle（根目录 `checkstyle.xml`）
- 静态检查：SpotBugs
- 架构与工程规范见 `openspec/specs/`（如 ddd-cola-layering、repository-structure、tech-stack 等）。

## Harness Engineering（仓库内落地）

Artemis 将 Harness Engineering 作为仓库级工程结构落地，而不是将其视为单独工具。其目标是让开发者和 agent 共享同一套事实来源、执行入口、验证回路和交付编排。

### 分层收敛方向

为了避免“知识库”和“执行层”继续交织，仓库按以下职责收敛：

- 事实源只说明项目是什么、规则是什么、当前状态是什么，不复制脚本细节。
- 执行入口只说明怎么启动、怎么操作、怎么排障，不重新定义长期规则。
- 验证守门只负责把规则变成可重复失败的检查，不承载业务架构说明。
- 任务过程只记录本次复杂工作如何推进，不替代长期规范。
- 编排资产只引用事实源和执行入口，不成为新的规则库。

### 文档与目录职责

| 层 | 主要位置 | 职责 | 不承担 |
|----|----------|------|--------|
| 事实源 | `README.md`、`ARCHITECTURE.md`、`QUALITY_SCORE.md`、`openspec/specs/`、`artemis-modules/*/*-client` | 项目定位、模块边界、稳定规则、质量状态、内部契约 | 复制完整脚本清单和一次性执行步骤 |
| 执行入口 | `scripts/dev/`、`scripts/smoke/`、`docs/runbooks/` | 启动服务、查看状态、smoke、打包、镜像、部署、回滚、常见任务操作步骤 | 定义模块边界、契约规则和默认 workflow 长期约束 |
| 验证守门 | `scripts/harness/`、`mvn verify`、`.github/workflows/verify.yml`、`.github/workflows/governance.yml`、`docs/governance/DOC_FRESHNESS_POLICY.md` | 增量验证、全量验证、契约/API 文档同步、重复模式扫描、CI 与文档新鲜度守门 | 替代人工需求判断或记录复杂任务计划 |
| 任务过程 | `docs/exec-plans/active/`、`docs/exec-plans/completed/`、`docs/exec-plans/templates/` | 复杂任务的背景、范围、步骤、风险、验证和归档 | 替代 OpenSpec 里的稳定规则 |
| 编排资产 | `artemis-symphony/`、`artemis-symphony/WORKFLOW.md.example`、`artemis-symphony/skills/`、`artemis-symphony/prompts/` | issue 拉取、隔离 workspace、Codex 执行、进度回写、常见任务提示和自评模板 | 成为新的知识库或规范事实来源 |
| 运行与交付资产 | `docker/`、`config/nacos/`、`scripts/e2e/`、`docs/reports/deploy-drills/` | 本地依赖、配置模板、镜像构建、真实 e2e 和部署/回滚演练记录 | 定义业务需求和领域模型 |

### 常见问题定位

| 你想了解 | 优先读 |
|----------|--------|
| 这个项目是什么、有哪些模块 | `README.md`、`ARCHITECTURE.md` |
| 当前做到什么程度、还有什么缺口 | `QUALITY_SCORE.md`、`docs/reports/PROJECT_PROGRESS_REPORT.md` |
| 某条规则是否是长期约束 | `openspec/specs/` |
| 一个需求应该改代码、OpenSpec、runbook 还是 Symphony | `docs/agent-workflow/AGENT_DEVELOPMENT_WORKFLOW.md` |
| 如何新增领域服务、Dubbo client 或 ArchUnit 规则 | `docs/runbooks/`、`artemis-symphony/skills/` |
| 如何启动、smoke、打包、部署或回滚 | `scripts/dev/`、`scripts/smoke/`、`docs/runbooks/SERVICE_SMOKE_RUNBOOK.md`、`docs/runbooks/DEPLOY_AND_ROLLBACK_RUNBOOK.md` |
| 如何验证本次改动 | `scripts/harness/verify-changed.sh`；高风险或跨模块改动使用 `scripts/harness/full-verify.sh` |
| 如何理解 agent 编排 | `artemis-symphony/README.md`、`artemis-symphony/WORKFLOW.md.example` |

### 标准工作流

1. 阅读 `README.md`、`AGENTS.md`、`ARCHITECTURE.md` 与相关 OpenSpec
2. 对跨模块或多步骤任务，在 `docs/exec-plans/active/` 建立执行计划
3. 通过 `scripts/dev/` 启动依赖、服务并完成健康检查
4. 通过 `scripts/smoke/` 或 `SERVICE_SMOKE_RUNBOOK.md` 完成最小行为验证
5. 实施代码、测试、脚本和文档的成组变更
6. 优先执行 `scripts/harness/verify-changed.sh`
7. 对高风险或跨模块改动执行 `scripts/harness/full-verify.sh`
8. 通过 CI 工作流重复执行仓库级守门

### 适用范围

这套结构同时服务于人工开发与 agent 驱动开发：

- 对开发者，提供统一的启动、验证、交付与排障入口
- 对 agent，提供稳定上下文、执行顺序、验证约束与回放路径
- 对仓库本身，提供低漂移的知识沉淀与持续守门机制

## Pre-commit 钩子（可选）

仓库提供可选的 pre-commit 钩子，在提交前执行：**格式化**（Spotless，Palantir Java Format）、**Checkstyle**（仅本次改动涉及模块）、**脚本语法检查**、**契约 / 文档 / 重复模式治理检查**、**OpenSpec 变更未同步提醒**。钩子为可选，未安装不影响正常开发与 CI。

- **脚本位置**：`.githooks/pre-commit`
- **安装**（在仓库根目录执行）：
  - 复制到本地钩子目录：`cp .githooks/pre-commit .git/hooks/pre-commit`（Windows Git Bash 同），然后 `chmod +x .git/hooks/pre-commit`
  - 或使用仓库钩子目录：`git config core.hooksPath .githooks`
- **绕过**：确有需要时可使用 `git commit --no-verify` 跳过钩子。
- 规则与例外见 `openspec/docs/pre-commit-openspec-sync-rule.md`；严格模式（未同步时阻止提交）可通过环境变量 `OPENSPEC_STRICT=1` 启用。

## 贡献约定

- **注释与文档**：新增或修改的代码注释、配置注释与面向贡献者的文档须使用中文；技术术语（如 stub、contract、DTO）可保留英文。命名（类名、方法名、变量名、配置 key 等）保持英文、符合英语母语习惯。

## License

MIT
