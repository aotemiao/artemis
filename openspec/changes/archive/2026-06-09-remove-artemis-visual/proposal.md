## Why

`artemis-visual` 当前只是未使用的顶层预留模块，没有代码、服务入口或可验证能力。继续保留它会让仓库模块地图与实际可交付能力不一致，也会让根 POM 聚合一个空模块。

## What Changes

- 删除顶层 `artemis-visual` 模块。
- 从根 POM 的 `<modules>` 中移除 `artemis-visual`。
- 更新 README、架构文档和 repository-structure 规范中的顶层模块清单。

## Capabilities

### Modified Capabilities

- `repository-structure`：顶层模块固定成员列表不再包含 `artemis-visual`。

## Impact

- 不影响现有业务服务、公共框架、网关、认证服务或 Symphony。
- 不改变内部 Dubbo client 契约或对外 REST API。
- 后续如需运维可视化能力，应按新的真实能力重新创建模块、入口文档、执行脚本和验证方式。
