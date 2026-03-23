# Deploy And Rollback Runbook

Status: maintained
Last Reviewed: 2026-03-23
Review Cadence: 90 days

本 runbook 用于固定 Artemis 在本地或 CI 中的“打包 -> 构建镜像 -> 部署前检查 -> 回滚”最小操作模型。

## 统一入口

- 打包服务：`scripts/dev/package-service.sh <gateway|auth|system|symphony|all>`
- 构建镜像：`scripts/dev/build-image.sh <gateway|auth|system|all>`
- 全量验证：`scripts/harness/full-verify.sh`

## 推荐顺序

1. 先运行 `scripts/harness/full-verify.sh`
2. 打包需要交付的服务
3. 构建对应镜像
4. 部署前确认配置、端口、依赖服务和 smoke 入口
5. 部署后执行对应 smoke

## 打包示例

```bash
scripts/dev/package-service.sh gateway
scripts/dev/package-service.sh auth
scripts/dev/package-service.sh system
scripts/dev/package-service.sh symphony
```

## 构建镜像示例

```bash
scripts/dev/build-image.sh gateway
scripts/dev/build-image.sh auth
scripts/dev/build-image.sh system
```

## 部署后建议 smoke

- Gateway：`scripts/smoke/gateway-auth-refresh.sh`
- Auth：`scripts/smoke/auth-refresh.sh`
- System：`scripts/smoke/system-lookup.sh`
- Symphony：`scripts/smoke/symphony-state.sh`

## 回滚原则

- 优先回滚到上一个已验证通过的镜像或 jar，而不是现场手改配置
- 回滚后立即执行对应 smoke，确认行为恢复
- 如果问题来自配置而不是代码，保留当前制品，回滚配置并记录原因
- 回滚完成后，补执行计划或 runbook，避免同类问题再次只靠记忆处理
