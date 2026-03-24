# 新增 Dubbo Client Runbook

Status: maintained
Last Reviewed: 2026-03-23
Review Cadence: 90 days

本 runbook 用于指导新增内部 Dubbo 契约，确保调用方只依赖 `*-client`，而不是直接耦合目标服务内部层。

## 开始前先读

1. `ARCHITECTURE.md`
2. `openspec/specs/engineering-constraints/spec.md`
3. `openspec/specs/ddd-cola-layering/spec.md`
4. 目标服务相关 OpenSpec 与模块 README

## 标准位置

- Dubbo 接口：`*-client/src/main/java/.../client/api`
- 跨服务 DTO：`*-client/src/main/java/.../client/dto`
- 提供方实现：`*-adapter` 或合适的适配层模块
- 调用方封装：调用方本服务中的 `client/` 或等价适配类
- 契约文档：`*-client/CLIENT_CONTRACT.md`

## 推荐步骤

1. 在 `*-client` 中先定义接口与 DTO。
2. 同步补 `CLIENT_CONTRACT.md`，确保 `check-client-contracts.sh` 可校验。
3. 在提供方实现 Dubbo 服务，避免直接暴露 `app/domain/infra` 类型。
4. 在调用方补一个最小封装类，屏蔽 RPC 细节。
5. 补充调用方或提供方的关键路径测试。

## 至少要补的验证

- `scripts/harness/check-client-contracts.sh`
- 调用方依赖约束测试或 ArchUnit
- 相关模块 `mvn test` / `mvn verify`

## 常见风险

- 把 Dubbo 接口放进 `adapter` 或 `app`
- 让调用方直接依赖目标服务的内部模块
- Java 契约改了，但 `CLIENT_CONTRACT.md` 没同步
