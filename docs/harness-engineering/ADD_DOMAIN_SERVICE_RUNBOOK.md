# 新增领域服务 Runbook

Status: maintained
Last Reviewed: 2026-03-24
Review Cadence: 90 days

本 runbook 用于指导在 Artemis 中新增一个符合 DDD/COLA 约束的领域服务，避免只补一个 `-start` 模块或只补 REST Controller。

## 开始前先读

1. `README.md`
2. `ARCHITECTURE.md`
3. `openspec/specs/ddd-cola-layering/spec.md`
4. `openspec/specs/engineering-constraints/spec.md`
5. 如为复杂任务，先在 `docs/exec-plans/active/` 建计划

## 标准拆分

新增领域服务时，至少按以下模块拆分：

- `*-client`
- `*-domain`
- `*-infra`
- `*-app`
- `*-adapter`
- `*-start`

依赖方向保持：

`adapter -> app -> domain <- infra`

`start` 只负责组装。

## 推荐步骤

1. 优先使用 `scripts/dev/new-domain-service.sh <domain>` 生成骨架。
2. 生成后先检查以下默认产物是否存在：
   - `artemis-api/artemis-api-<domain>`
   - `artemis-modules/artemis-<domain>/artemis-<domain>-client`
   - `SERVICE_API.md`
   - `config/nacos/artemis-<domain>.yml`
   - `scripts/dev/run-<domain>.sh`
   - `scripts/smoke/<domain>-ping.sh`
3. 先建聚合、Gateway 接口和 client 契约，再补 app/adapter/start。
4. 优先从最小闭环开始：
   一个领域用例、一个对外入口、一个持久化实现、一个 smoke 或最小测试。
5. 同步补 Nacos 模板、Dockerfile、run script 与验证入口。
6. 同步更新相关 OpenSpec、README 或模块文档。

## 至少要补的验证

- App 层执行器单元测试
- Infra Gateway 集成测试或等价关键路径测试
- 分层依赖约束测试
- 至少一个 smoke 或 readiness 断言入口

## 推荐命令

- `scripts/dev/new-domain-service.sh <domain>`
- `scripts/harness/verify-changed.sh`
- `scripts/harness/full-verify.sh`
- `scripts/dev/package-service.sh <service>`
- `scripts/dev/build-image.sh <service>`

## 常见风险

- 直接让 `adapter` 或 `app` 依赖 `infra`
- 只补 Controller，不补 client 契约和测试
- 把新增规则写进文档却没有落成脚本或测试
