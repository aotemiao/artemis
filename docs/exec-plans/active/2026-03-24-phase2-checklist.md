# Phase 2 Checklist

## 背景

本 checklist 对应演进计划中的 Phase 2：验证模板可复制、形成第二个领域服务，并把新服务纳入默认契约与运行资产守门。

## Checklist

- [x] 使用脚手架生成第二个领域服务 `artemis-resource`
- [x] 注册 `artemis-resource` 到 `artemis-modules`，注册 `artemis-api-resource` 到 `artemis-api`
- [x] 将 `resource` 纳入服务目录、运行脚本、smoke、Dockerfile 与 Nacos 模板
- [x] 让 API 文档同步脚本自动发现脚手架生成的 `SERVICE_API.md`
- [x] 新增 `scripts/harness/check-service-catalog.sh`，校验领域服务的运行资产是否齐全
- [x] 将服务目录与资产守门接入治理回路

## 已交付产物

- `artemis-modules/artemis-resource/`
- `artemis-api/artemis-api-resource/`
- `scripts/lib/service-catalog.sh`
- `scripts/harness/check-service-catalog.sh`

## 结果

Phase 2 已完成当前仓库范围内的 checklist。Artemis 已有一个真实的第二领域服务样板，可以用于继续扩业务或验证模板复制路径。
