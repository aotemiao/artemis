# 移除 artemis-api 聚合层

## 背景

`artemis-modules` 下的领域服务已经按 COLA 结构内置 `*-client` 契约模块。继续保留顶层 `artemis-api`、`artemis-api-bom` 与 `artemis-api-<domain>` bridge，会让内部契约多一层转发，并与 `per-domain-client-contracts` 中“client 随领域放置”的规则不一致。

## 目标

- 移除 `artemis-api` 顶层聚合模块。
- 将内部调用方统一切到领域自身的 `*-client`。
- 同步脚手架、治理脚本、文档与 OpenSpec。

## 非目标

- 不修改 Dubbo 接口、DTO 字段或 REST API 行为。
- 不调整 `artemis-system`、`artemis-resource` 的业务实现。

## 范围

- Maven reactor、BOM、`artemis-auth` 依赖。
- `scripts/dev/new-domain-service.sh` 与服务资产守门脚本。
- README、架构文档、runbook、项目进度文档与 OpenSpec。

## 风险

- 调用方若仍依赖 `artemis-api-system` 会构建失败，因此需要同步切到 `artemis-system-client`。
- 脚手架若仍生成 API bridge，会重新引入已移除结构，因此必须同步修改预览检查。

## 分步任务

1. 移除根 POM 中的 `artemis-api` 模块声明。
2. 删除 `artemis-api-*` 依赖管理，保留 `*-client` 依赖管理。
3. 将 `artemis-auth` 从 `artemis-api-system` 切到 `artemis-system-client`。
4. 修改领域服务脚手架与服务目录守门，直接登记和校验 `*-client`。
5. 删除 `artemis-api` 聚合模块文件。
6. 同步文档、OpenSpec 与验证。

## 验证

- `scripts/harness/check-domain-service-scaffold.sh`：通过，确认脚手架不再生成 API bridge。
- `scripts/harness/check-service-catalog.sh`：通过，确认领域服务资产守门检查 `*-client`。
- `scripts/harness/run-governance-checks.sh`：通过。
- `scripts/harness/verify-changed.sh working-tree`：治理检查通过，Maven scoped verify 因当前环境缺少 Linux `java` 阻塞。

## 决策记录

- `2026-04-28`：内部 Java 契约入口统一收敛到各领域服务 colocated `*-client`，不再保留顶层 `artemis-api` bridge。

## 遗留问题

- 当前环境 PATH 中没有可执行的 Linux `java`，`scripts/harness/verify-changed.sh working-tree` 的治理检查已通过，但 Maven scoped verify 在 Java 检查处阻塞。
