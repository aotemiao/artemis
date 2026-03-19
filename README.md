# Artemis

Spring Cloud 微服务管理后台脚手架，采用 DDD/COLA 分层架构，参考 RuoYi-Cloud-Plus 与 COLA，面向 Clean Code 与可维护性。

## 技术栈

- **JDK 21** · **Spring Boot 3.5** · **Spring Cloud 2025**
- **PostgreSQL** 数据库（本地默认）· **Nacos** 注册/配置中心 · **Sa-Token** 认证 · **MyBatis-Plus** ORM · **Redisson** 缓存与分布式锁
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
├── artemis-modules          # 业务微服务（按领域拆分，对外通过 REST API 暴露能力）
│   └── artemis-system       # 系统管理 (adapter / app / domain / infra / start)
└── artemis-visual           # 运维基础设施（按需扩展）
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

3. **运行网关**

   ```bash
   cd artemis-gateway && mvn spring-boot:run
   ```

4. **运行系统服务**

   ```bash
   cd artemis-modules/artemis-system/artemis-system-start && mvn spring-boot:run
   ```

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

## Pre-commit 钩子（可选）

仓库提供可选的 pre-commit 钩子，在提交前执行：**格式化**（Spotless，Palantir Java Format）、**Checkstyle**（仅本次改动涉及模块）、**OpenSpec 变更未同步提醒**。钩子为可选，未安装不影响正常开发与 CI。

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
