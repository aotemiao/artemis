# Docker Templates

本目录包含 Artemis 本地 Compose 编排、Nacos 配置加载器和关键服务的容器化模板，目标是让服务级镜像构建方式也成为仓库内知识的一部分。

## 一键启动

从仓库根目录执行：

```bash
scripts/dev/start-all.sh
```

该入口会调用 `docker/docker-compose.yml`，启动 PostgreSQL、Redis、Nacos、Nacos 配置加载器、`artemis-system`、`artemis-auth` 与 `artemis-gateway`，随后执行主机侧 readiness 检查。

说明：`nacos/nacos-server:v2.3.2` 固定使用 `linux/amd64` 平台，Apple Silicon 本地会通过 Docker Desktop / OrbStack 的兼容模式运行。

如需同时启动 resource/workflow：

```bash
scripts/dev/start-all.sh --full
```

`full` profile 中 resource/workflow 暂时复用本地 `artemis_system` 数据库，并通过独立 Flyway history table 避免不同服务的 `V1__...` 迁移互相冲突。

停止服务：

```bash
scripts/dev/stop-all.sh
```

如需清空本地数据库与 Nacos 数据卷：

```bash
scripts/dev/stop-all.sh --volumes
```

## 可用模板

- `Dockerfile.gateway`
- `Dockerfile.auth`
- `Dockerfile.system`
- `Dockerfile.resource`
- `Dockerfile.workflow`

## 构建示例

从仓库根目录执行：

```bash
docker build -f docker/Dockerfile.gateway -t artemis-gateway:local .
docker build -f docker/Dockerfile.auth -t artemis-auth:local .
docker build -f docker/Dockerfile.system -t artemis-system:local .
docker build -f docker/Dockerfile.resource -t artemis-resource:local .
docker build -f docker/Dockerfile.workflow -t artemis-workflow:local .
```

## 说明

- 这些 Dockerfile 使用多阶段构建。
- 构建上下文为仓库根目录，以便继承根 BOM 和多模块依赖关系。
- 默认使用 `-DskipTests`，CI 仍应通过 `mvn verify` 守住质量门。
