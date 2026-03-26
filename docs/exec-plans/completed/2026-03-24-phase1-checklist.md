# Phase 1 Checklist

## 背景

本 checklist 对应 [Artemis 演进计划（基于本地参考源）](2026-03-24-artemis-evolution-with-local-references.md) 中的 Phase 1：把 `artemis-system` 产品化成服务模板，让新增一个领域服务不再依赖手工拼装。

## Checklist

- [x] 建立 `artemis-api` 顶层聚合模块，并接入根 POM
- [x] 建立 `artemis-api-bom`，收敛内部 API bridge 与 `*-client` 版本
- [x] 建立 `artemis-api-system` bridge，并将 `artemis-auth` 切换到该入口
- [x] 回写 OpenSpec，允许 `artemis-api` 成为仓库级结构的一部分
- [x] 将 Dubbo client 契约检查改为按 `*-client` 自动发现，而不是只特判 system
- [x] 提供 `scripts/dev/new-domain-service.sh`，默认生成 API bridge、COLA 六模块、Nacos 模板、运行脚本、readiness、smoke、Dockerfile、API 文档、CLIENT_CONTRACT、ArchUnit 与 JaCoCo
- [x] 将 `package-service.sh`、`build-image.sh`、`check-service-config.sh`、`check-service-readiness.sh`、`tail-log.sh` 泛化为可接未来领域服务
- [x] 新增 `scripts/harness/check-domain-service-scaffold.sh`，持续验证脚手架默认产物没有退化
- [x] 将脚手架检查接入治理回路与 pre-commit
- [x] 回写 README、ARCHITECTURE、AGENTS、runbook 与 docs 索引

## 已交付产物

- `artemis-api/`
- `scripts/dev/new-domain-service.sh`
- `scripts/harness/check-domain-service-scaffold.sh`
- `docs/harness-engineering/ADD_DOMAIN_SERVICE_RUNBOOK.md`
- `openspec/specs/repository-structure/spec.md`

## 验证

- `bash -n scripts/dev/new-domain-service.sh`
- `bash -n scripts/harness/check-domain-service-scaffold.sh`
- `scripts/harness/check-domain-service-scaffold.sh`
- `scripts/harness/run-governance-checks.sh`
- `mvn -B -pl artemis-api,artemis-auth -am verify`

## 结果

Phase 1 的 checklist 已完成当前定义范围内的全部事项。下一步应转向 Phase 2：优先补齐 `artemis-system` 的主数据能力，并用第二个领域服务验证脚手架是否真能低成本复制。
