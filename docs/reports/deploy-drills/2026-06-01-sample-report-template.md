# Deploy Drill Report Template

Status: template
Last Reviewed: 2026-06-01
Review Cadence: 90 days

## 演练范围

- 服务：
- 环境：
- 目标镜像或构建产物：

## 执行命令

```bash
scripts/dev/deploy-drill.sh <service>
scripts/dev/rollback-drill.sh <service> <image-tag|jar-path>
```

## 验证结果

| 检查项 | 命令或证据 | 结果 |
|--------|------------|------|
| readiness | `scripts/dev/check-service-readiness.sh <service>` | 待填写 |
| smoke | `scripts/smoke/<name>.sh` | 待填写 |
| 回滚后状态 | `scripts/dev/service-status.sh <service>` | 待填写 |

## 问题与处理

- 待填写

## 结论

- 待填写
