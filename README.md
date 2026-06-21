# Artemis

Status: maintained
Last Reviewed: 2026-06-10
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
5. `docs/feature-specs/`
   查看业务需求级 Spec、验收标准和验证映射模板。
6. `openspec/specs/`
   查看稳定规则，例如模块边界、契约治理、质量门和默认 agent workflow。

需要实际开发或验证时，再进入 `AGENTS.md`、`docs/agent-workflow/AGENT_DEVELOPMENT_WORKFLOW.md`、相关 runbook 与 `scripts/` 入口。

## 快速开始

### 一键启动核心链路

本地开发可直接通过 Docker Compose 启动基础设施、加载 Nacos 模板、构建镜像、拉起 system/auth/gateway 并执行 readiness：

```bash
scripts/dev/start-all.sh
```

停止 Compose 服务：

```bash
scripts/dev/stop-all.sh
```

说明：一键脚本是 `docker/docker-compose.yml` 的薄封装，默认启动核心链路 `system auth gateway`。如需同时启动 resource/workflow，可执行 `scripts/dev/start-all.sh --full`。

### 手动启动

1. **启动基础设施**

   本地默认使用 **PostgreSQL**、Redis、Nacos（与 `config/nacos/datasource.yml` 一致）。首次从旧版 MySQL 编排切换时，可先执行 `scripts/dev/stop-all.sh --volumes` 再启动。

   ```bash
   scripts/dev/up.sh
   ```

2. **上传 Nacos 配置模板**

   ```bash
   scripts/dev/upload-nacos-configs.sh
   ```

3. **编译**

   ```bash
   mvn clean install -DskipTests
   ```

4. **运行系统服务**

   ```bash
   scripts/dev/run-system.sh
   ```

5. **运行认证服务与网关**

   ```bash
   scripts/dev/run-auth.sh
   scripts/dev/run-gateway.sh
   ```

6. **（可选）运行 Symphony**

   先复制 `artemis-symphony/WORKFLOW.md.example` 为仓库根 `WORKFLOW.md`，并按需设置 `LINEAR_API_KEY`。若希望 Symphony 在处理 issue 后把进度摘要评论回写到 Linear，可在 workflow 中开启 `reporting.linear_comments.enabled: true`。脚本默认会打开本地 `9500` 端口用于观测；如需自定义，可直接追加 `--server.port=...`，也兼容旧写法 `-Dspring-boot.run.arguments=...`。

   ```bash
   scripts/dev/run-symphony.sh
   ```

   若要执行与官方 `openai/symphony` 对齐的真实 live e2e 演练，可显式提供 `LINEAR_API_KEY` 后运行：

   ```bash
   scripts/e2e/run-symphony-live-e2e.sh
   ```

   若未提供真实 SSH worker，脚本会自动使用 `artemis-symphony/test-support/live-e2e-docker/` 中的 docker fallback worker；该 fallback 会保留官方默认 `workspace-write` sandbox 语义，并可通过 `SYMPHONY_LIVE_E2E_KEEP_ARTIFACTS=1` 保留失败现场。

7. **（可选）编译 Symphony 子工程**

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

## Harness Engineering

Harness Engineering 的概念、当前有效能力和已下线能力以 [`docs/governance/HARNESS.md`](docs/governance/HARNESS.md) 为准。本章只保留入口索引，避免 README 与治理清单重复维护同一套说明。

| 你想了解 | 优先读 |
|----------|--------|
| Harness Engineering 是什么、哪些能力仍有效、哪些已下线 | [`docs/governance/HARNESS.md`](docs/governance/HARNESS.md) |
| 当前做到什么程度、还有什么缺口 | [`QUALITY_SCORE.md`](QUALITY_SCORE.md)、[`docs/reports/PROJECT_PROGRESS_REPORT.md`](docs/reports/PROJECT_PROGRESS_REPORT.md) |
| 一个需求应该改代码、OpenSpec、runbook 还是 Symphony | [`docs/agent-workflow/AGENT_DEVELOPMENT_WORKFLOW.md`](docs/agent-workflow/AGENT_DEVELOPMENT_WORKFLOW.md) |
| 如何验证本次改动 | `scripts/harness/verify-changed.sh`；高风险或跨模块改动使用 `scripts/harness/full-verify.sh` |
| 如何启动、smoke、打包、部署或回滚 | `scripts/dev/`、`scripts/smoke/`、[`docs/runbooks/SERVICE_SMOKE_RUNBOOK.md`](docs/runbooks/SERVICE_SMOKE_RUNBOOK.md)、[`docs/runbooks/DEPLOY_AND_ROLLBACK_RUNBOOK.md`](docs/runbooks/DEPLOY_AND_ROLLBACK_RUNBOOK.md) |
| 某条规则是否是长期约束 | [`openspec/specs/`](openspec/specs/) |

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
