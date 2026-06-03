# modules 能力包平铺治理执行计划

Status: completed
Last Updated: 2026-06-03

## 背景

`artemis-modules` 已经按 `client / adapter / app / domain / infra / start` 拆分，并且 `app`、`domain`、`infra` 的核心分类包已按业务能力组织。当前仍存在平铺风险的位置主要是 `adapter.web`、`adapter.web.dto`、`adapter.dubbo`、`client.api`、`client.dto`，这些包下聚集了不同业务能力的具体类。

## 范围

- 将 `artemis-system`、`artemis-resource`、`artemis-workflow` 中上述平铺包的具体类移动到业务能力子包。
- 同步更新 Java package 声明和 import。
- 更新 OpenSpec 与 harness 脚本，使 adapter/client 平铺也能被守门。
- 运行结构检查和 Maven 编译验证迁移结果。

## 非目标

- 不调整业务逻辑、接口字段或 REST 路由。
- 不处理 `artemis-symphony` 当前已有改动。
- 不改变各服务的 Maven 分层依赖方向。

## 风险

- 包名迁移可能导致漏改 import，需通过 Maven 编译发现。
- client 契约包名变化会影响内部调用方，需搜索并同步所有引用。

## 步骤

1. 建立业务能力包映射。
2. 迁移 main/test Java 文件并更新 package/import。
3. 更新 `openspec/specs/ddd-cola-layering/spec.md` 与 `scripts/harness/check-capability-package-structure.sh`。
4. 执行结构检查与 Maven 编译。
5. 归档执行计划。

## 验证

- `scripts/harness/check-capability-package-structure.sh`
- `mvn -pl artemis-modules/artemis-system,artemis-modules/artemis-resource,artemis-modules/artemis-workflow -am compile`

## 结果

- 已将 `artemis-system`、`artemis-resource`、`artemis-workflow` 的 `adapter.web`、`adapter.web.dto`、`adapter.dubbo`、`client.api`、`client.dto` 具体类型迁移到业务能力子包。
- 已同步更新 API 文档路径守门、client 契约文档守门、关键路径测试基线、OpenSpec 包命名规则与能力包结构检查脚本。
- 已执行 `scripts/harness/verify-changed.sh`，通过 changed-scope governance 与 Maven verify；OpenSpec sync 输出现有 active change 提醒，但非 strict 模式下不阻断验证。
