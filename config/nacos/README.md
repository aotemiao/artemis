# Nacos 配置模板（层级化、模板化）

本目录参考 RuoYi-Cloud-Plus 的 `script/config/nacos` 结构，按**配置层级**组织：公共配置 → 数据源 → 各服务专属。各服务通过 **config.import 顺序** 拉取，实现模板化、少重复。

## 配置层级与引用关系

```
application-common.yml   ← 所有服务必引（Redis、Sa-Token、Redisson、logging、management 等）
        ↓
datasource.yml           ← 仅需数据库的服务引用（system 等）
        ↓
artemis-xxx.yml          ← 各服务专属（端口、路由、数据源引用等）
```

| 服务              | 拉取顺序 |
|-------------------|----------|
| artemis-gateway   | application-common → artemis-gateway |
| artemis-auth      | application-common → artemis-auth |
| artemis-system    | application-common → datasource → artemis-system |
| artemis-resource  | application-common → datasource → artemis-resource |

## 文件与 Data ID 对应关系

| 本地文件                  | Nacos Data ID             | 说明 |
|--------------------------|---------------------------|------|
| `application-common.yml`  | `application-common.yml`  | 公共配置，所有服务 config.import 首位 |
| `datasource.yml`          | `datasource.yml`          | 数据源定义，仅 system 等需库的服务引用 |
| `artemis-system.yml`      | `artemis-system.yml`      | 系统服务专属（引用 datasource 中的 system-master） |
| `artemis-resource.yml`    | `artemis-resource.yml`    | 资源服务专属（样板服务，默认也引用 datasource） |
| `artemis-gateway.yml`     | `artemis-gateway.yml`     | 网关专属（路由等），不引用 datasource |
| `artemis-auth.yml`        | `artemis-auth.yml`        | 认证服务专属，不引用 datasource |

- **Group**：未指定时使用 Nacos 默认 `DEFAULT_GROUP`。
- **多环境**：可按环境建不同 Data ID（如 `artemis-system-dev.yml`）或使用 Nacos 命名空间隔离。

## 上传顺序（重要）

1. **application-common.yml**（先传，被后续引用）
2. **datasource.yml**（数据源为 PostgreSQL，与本地 docker-compose 一致）
3. **artemis-system.yml**、**artemis-resource.yml**、**artemis-gateway.yml**、**artemis-auth.yml**

与 RuoYi 一致：将此目录下所有配置文件按上表 Data ID 复制到 Nacos 配置列表中即可。

### 通过脚本自动上传（Nacos Open API）

已提供脚本通过 Nacos 的 HTTP 接口批量发布配置（需先启动 Nacos，如 `docker-compose up -d`）：

```powershell
cd config/nacos
.\upload-configs.ps1
```

可选参数：

- `-NacosServer`：Nacos 地址，默认 `http://127.0.0.1:8848`
- `-NamespaceId`：命名空间 ID，默认空（public）

示例：指定服务器与命名空间

```powershell
.\upload-configs.ps1 -NacosServer "http://192.168.1.10:8848" -NamespaceId "dev"
```

## Nacos 连接信息（与 RuoYi 一致）

- **构建时注入**：各服务仅保留**一个** `application.yml`，其中 Nacos 地址等使用 Maven 占位符（`@nacos.server@`、`@nacos.username@`、`@nacos.password@`、`@nacos.discovery.group@`、`@nacos.config.group@`、`@profiles.active@`）。根目录 `pom.xml` 的 **Maven profile**（dev/test/prod）中定义上述变量，打包时替换。
- **默认**：profile `dev` 默认启用，`nacos.server=127.0.0.1:8848`。生产打包可指定 `-Pprod` 或在 pom 的 prod profile 中修改 `nacos.server`；也可构建时覆盖：`mvn package -Dnacos.server=192.168.1.10:8848`。
- **namespace**：与 RuoYi 一致，使用 `${spring.profiles.active}` 作为 Nacos 命名空间（dev/test/prod 各建一个 namespace 时，需在 Nacos 控制台创建并与 profile 对应）。

## 模板化说明

- **application-common**：多服务共用的中间件与框架配置（Redis、Sa-Token、Redisson、undertow、jackson、logging、management），各服务 yml 只写差异化部分。
- **datasource**：统一维护数据源连接（如 `datasource.system-master`），默认使用 **PostgreSQL**（端口 5432），与根目录 `docker/docker-compose.yml` 提供的本地数据库一致；业务服务在各自 yml 中通过 `${datasource.system-master.url}` 等引用，便于多环境只改一处。
- **各服务 yml**：仅保留端口、路由、数据源引用、白名单等该服务独有配置，不重复写公共项。

## Dubbo 注册中心（内部 RPC）

参与内部 RPC 的服务（如 artemis-auth、artemis-system）在各自 `artemis-xxx.yml` 中配置 `dubbo.application.name` 与 `dubbo.registry.address`。注册中心与 Nacos 共用：`dubbo.registry.address: nacos://${spring.cloud.nacos.server-addr:127.0.0.1:8848}`。超时、重试等可使用 Dubbo 默认，或通过 `dubbo.consumer`/`dubbo.provider` 在公共或服务专属配置中覆盖。

## 注意

- 本目录仅作**模板参考**，不参与应用打包。
- 敏感信息（密码、密钥）请勿提交仓库，应在 Nacos 或环境变量中配置。
