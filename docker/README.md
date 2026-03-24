# Docker Templates

本目录包含 Artemis 关键服务的容器化模板，目标是让服务级镜像构建方式也成为仓库内知识的一部分。

## 可用模板

- `Dockerfile.gateway`
- `Dockerfile.auth`
- `Dockerfile.system`
- `Dockerfile.resource`

## 构建示例

从仓库根目录执行：

```bash
docker build -f docker/Dockerfile.gateway -t artemis-gateway:local .
docker build -f docker/Dockerfile.auth -t artemis-auth:local .
docker build -f docker/Dockerfile.system -t artemis-system:local .
docker build -f docker/Dockerfile.resource -t artemis-resource:local .
```

## 说明

- 这些 Dockerfile 使用多阶段构建。
- 构建上下文为仓库根目录，以便继承根 BOM 和多模块依赖关系。
- 默认使用 `-DskipTests`，CI 仍应通过 `mvn verify` 守住质量门。
