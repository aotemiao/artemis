# Symphony Troubleshooting

Status: maintained
Last Reviewed: 2026-03-23
Review Cadence: 90 days

本 runbook 用于快速定位 `artemis-symphony` 在本仓库中的常见问题。

## 常见入口

- 启动：`scripts/dev/run-symphony.sh`
- 状态 smoke：`scripts/smoke/symphony-state.sh`
- 全量验证：`scripts/harness/full-verify.sh`

## 常见问题

### 1. Symphony 启动了，但看不到 HTTP 状态页

排查顺序：

1. 确认是否通过 `scripts/dev/run-symphony.sh` 启动
2. 默认端口应为 `9500`
3. 执行 `scripts/smoke/symphony-state.sh`
4. 若仍失败，确认是否手工传入了自定义 `--server.port=...` 或旧写法 `-Dspring-boot.run.arguments=...`

### 2. WORKFLOW 变更后没有生效

排查顺序：

1. 确认 `WORKFLOW.md` 路径是否正确
2. 查看控制台日志中是否有 workflow reload 相关错误
3. 确认 YAML 前件和模板变量是否完整
4. 调用 `POST /api/v1/refresh` 触发一次立即 tick

### 3. Linear / agent 任务没有推进

排查顺序：

1. 检查 `LINEAR_API_KEY` 是否存在
2. 检查 `WORKFLOW.md` 中的 tracker 配置，并确认 `tracker.project_slug` 已填写真实值
3. 查看 `GET /api/v1/state` 返回的 running / retrying 状态
4. 确认 Codex app-server 所需环境是否可用

### 4. 只想确认 Symphony 基础能力是否可用

直接执行：

```bash
scripts/smoke/symphony-state.sh
```

如果这个命令通过，至少说明：

- HTTP 端口可访问
- 根说明页可访问
- 状态 API 可访问

## 建议做法

- 不要只盯日志，先跑 `scripts/smoke/symphony-state.sh`
- 不要把临时排查命令只留在聊天里，新的稳定排查动作应补进 runbook 或脚本
- 遇到复杂问题时，先在 `docs/exec-plans/active/` 建计划再继续排查
