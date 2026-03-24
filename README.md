# Artemis

Status: maintained
Last Reviewed: 2026-03-23
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
│   └── artemis-system       # 系统管理 (client / adapter / app / domain / infra / start)
├── artemis-visual           # 运维基础设施（按需扩展）
└── artemis-symphony         # Symphony：编码代理编排（WORKFLOW.md + Linear + Codex，可独立运行；见子目录 README）
```

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

   ```bash
   scripts/dev/run-symphony.sh
   ```

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

仓库已补齐一组面向 agent 与开发者共用的入口文件与脚本，用于把工程知识、执行步骤和验证回路固化在仓库内：

- `AGENTS.md`：agent 最小稳定入口
- `ARCHITECTURE.md`：仓库级架构地图
- `QUALITY_SCORE.md`：当前质量评分与优先补强项
- `docs/harness-engineering/`：落地清单与阶段路线图
- `docs/exec-plans/`：复杂任务执行计划目录
- `scripts/dev/`：本地环境与服务启动入口
- `scripts/dev/package-service.sh`：统一服务打包入口
- `scripts/dev/build-image.sh`：统一镜像构建入口
- `scripts/dev/wait-http.sh`：统一 HTTP 等待 / 启动断言入口
- `scripts/dev/check-service-config.sh`：配置缺失检查入口
- `scripts/dev/check-service-readiness.sh`：启动成功 / 慢启动 / 关键端点可达性断言入口
- `scripts/dev/health.sh`：关键依赖与服务健康检查
- `scripts/dev/tail-log.sh`：统一日志查看入口
- `scripts/harness/`：OpenSpec 同步、增量验证、全量验证
- `scripts/harness/run-governance-checks.sh`：文档、契约、重复模式与质量问题治理入口
- `scripts/smoke/`：最小 smoke 验证脚本
- `scripts/smoke/all-services.sh`：关键服务聚合 smoke 入口
- `docs/harness-engineering/SERVICE_SMOKE_RUNBOOK.md`：服务 smoke 与启动顺序 runbook
- `docs/harness-engineering/ADD_DOMAIN_SERVICE_RUNBOOK.md`：新增领域服务 runbook
- `docs/harness-engineering/ADD_DUBBO_CLIENT_RUNBOOK.md`：新增 Dubbo client runbook
- `docs/harness-engineering/ADD_ARCHUNIT_RULE_RUNBOOK.md`：ArchUnit 约束 runbook
- `docs/harness-engineering/AGENT_REVIEW_LOOP.md`：agent 自评与 reviewer 回路
- `docs/harness-engineering/QUALITY_ISSUE_STANDARD.md`：质量问题归档与关闭标准
- `docs/harness-engineering/DOC_FRESHNESS_POLICY.md`：核心文档审阅 cadence 与防漂移规则
- `docs/harness-engineering/DEPLOY_AND_ROLLBACK_RUNBOOK.md`：打包、镜像、部署与回滚 runbook
- `docs/harness-engineering/SYMPHONY_TROUBLESHOOTING.md`：Symphony 常见故障 runbook
- `.github/workflows/governance.yml`：周期性文档 / 工程治理工作流
- `.github/workflows/verify.yml`：CI 标准验证工作流
- `docker/Dockerfile.*`：关键服务容器化模板

### 架构定位

Artemis 将 Harness Engineering 作为仓库级工程结构落地，而不是将其视为单独工具。其目标是把工程知识、执行入口、验证回路和 agent 编排统一沉淀在仓库内，使开发者与 agent 共享同一套事实来源和交付流程。

### 架构组成

1. **知识层**

   - `README.md`：仓库总览、启动方式与工程入口
   - `AGENTS.md`：agent 稳定入口与执行约束
   - `ARCHITECTURE.md`：模块边界、服务拓扑、依赖方向
   - `QUALITY_SCORE.md`：当前质量状态、封板结论与扩展方向
   - `openspec/specs/`：分层、测试、工程约束等规范事实

2. **执行层**

   - `scripts/dev/`：基础设施、服务启动、健康检查、日志查看
  - `scripts/dev/package-service.sh`：统一服务打包入口
  - `scripts/dev/build-image.sh`：统一镜像构建入口
  - `scripts/dev/wait-http.sh`：HTTP 等待与启动断言入口
  - `scripts/dev/check-service-config.sh`：配置模板检查入口
  - `scripts/dev/check-service-readiness.sh`：服务就绪断言入口
  - `scripts/smoke/`：system、auth、gateway、symphony 等最小 smoke 入口
  - `docs/harness-engineering/SERVICE_SMOKE_RUNBOOK.md`：启动顺序与 smoke 组合标准

3. **验证层**

  - `scripts/harness/verify-changed.sh`：增量验证入口
  - `scripts/harness/full-verify.sh`：仓库级全量验证入口
  - `scripts/harness/run-governance-checks.sh`：治理与文档守门入口
  - `mvn verify`：测试、Spotless、Checkstyle、SpotBugs 统一质量门
  - `docs/harness-engineering/DOC_FRESHNESS_POLICY.md`：核心文档审阅 cadence 与防漂移规则
  - `.github/workflows/verify.yml`：CI 中的 OpenSpec 差异检查、全量验证与镜像构建
  - `.github/workflows/governance.yml`：周期性治理与文档整理守门

4. **编排层**

   - `artemis-symphony`：任务编排、隔离 workspace、状态观测与 workflow 驱动执行
   - `artemis-symphony/WORKFLOW.md.example`：编排器默认读取的仓库级执行模型
   - `docs/exec-plans/`：复杂任务计划、决策与归档目录

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
