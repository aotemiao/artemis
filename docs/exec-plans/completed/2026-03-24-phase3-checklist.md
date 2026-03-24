# Phase 3 Checklist

## 背景

本 checklist 对应演进计划中的 Phase 3：让运行态检查、部署演练与回滚演练成为默认入口，而不是临时命令拼装。

## Checklist

- [x] 建立统一服务目录，供运行态与演练脚本复用
- [x] 将 `scripts/smoke/all-services.sh` 改为按服务目录动态执行
- [x] 将 `scripts/dev/health.sh` 改为按服务目录逐项执行 readiness
- [x] 新增 `scripts/dev/service-status.sh`
- [x] 新增 `scripts/dev/deploy-drill.sh`
- [x] 新增 `scripts/dev/rollback-drill.sh`
- [x] 新增 `docs/harness-engineering/deploy-drills/` 报告目录
- [x] 回写部署 / 回滚 runbook、README、ARCHITECTURE 与 docs 索引

## 已交付产物

- `scripts/dev/service-status.sh`
- `scripts/dev/deploy-drill.sh`
- `scripts/dev/rollback-drill.sh`
- `docs/harness-engineering/deploy-drills/README.md`

## 结果

Phase 3 已完成当前仓库范围内的 checklist。Artemis 已具备服务状态总览、部署演练与回滚演练的标准入口。
