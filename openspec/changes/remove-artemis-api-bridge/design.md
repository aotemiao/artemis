## 设计说明

### 模块边界

内部 Java 契约以领域服务为归属边界：

- `artemis-modules/artemis-system/artemis-system-client`
- `artemis-modules/artemis-resource/artemis-resource-client`
- 后续新增领域服务的 `artemis-modules/artemis-<domain>/artemis-<domain>-client`

顶层不再提供 `artemis-api` 聚合，也不提供二级 `artemis-api-<domain>` bridge。这样调用方看到的依赖与契约所有权一致，减少 bridge 与真实 client 之间的漂移。

### 版本管理

`artemis-dependencies` 继续管理所有 `*-client` 的版本。调用方依赖 `artemis-system-client` 等 artifact 时不写版本，由根 POM 导入的 BOM 统一提供。

### 脚手架和治理

`scripts/dev/new-domain-service.sh` 生成服务时只生成领域服务六模块、脚本、Nacos 模板、Dockerfile、REST API 文档和 `CLIENT_CONTRACT.md`。服务目录最后一列从 `api_bridge` 改为 `client_module`，治理脚本据此检查服务内 client 模块。

### 兼容性

这是源码结构兼容性变更，不改变运行时接口。仍依赖 `artemis-api-*` 的调用方需要改为依赖对应 `*-client`。
