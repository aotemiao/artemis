# Artemis

Spring Cloud 微服务管理后台脚手架，采用 DDD/COLA 分层架构，参考 RuoYi-Cloud-Plus 与 COLA，面向 Clean Code 与可维护性。

## 技术栈

- **JDK 21** · **Spring Boot 3.5** · **Spring Cloud 2025**
- **Nacos** 注册/配置中心 · **Sa-Token** 认证 · **MyBatis-Plus** ORM · **Redisson** 缓存与分布式锁
- **Spring Cloud Gateway** 网关

## 模块结构

```
artemis/
├── artemis-dependencies     # BOM 依赖版本
├── artemis-framework        # 公共 starter
│   ├── artemis-common-core
│   ├── artemis-common-web
│   ├── artemis-common-security
│   ├── artemis-common-mybatis
│   ├── artemis-common-redis
│   ├── artemis-common-log
│   └── artemis-common-doc
├── artemis-gateway          # 网关
├── artemis-auth             # 认证服务
├── artemis-modules          # 业务微服务（按领域拆分，对外通过 REST API 暴露能力）
│   └── artemis-system       # 系统管理 (adapter / app / domain / infra / start)
└── artemis-visual           # 运维基础设施（按需扩展）
```

## 快速开始

1. **启动基础设施**

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
- Nacos 中需提前按 **`config/nacos`** 目录下的模板创建配置（Data ID：application-common、datasource、artemis-xxx）。详见 `config/nacos/README.md`。

## 规范与质量

- 代码风格：Checkstyle（根目录 `checkstyle.xml`）
- 静态检查：SpotBugs
- 架构规范见 `openspec/` 下 foundation-spec。

## 贡献约定

- **注释与文档**：新增或修改的代码注释、配置注释与面向贡献者的文档须使用中文；技术术语（如 stub、contract、DTO）可保留英文。命名（类名、方法名、变量名、配置 key 等）保持英文、符合英语母语习惯。

## License

MIT
