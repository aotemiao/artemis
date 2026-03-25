# Symphony Live E2E 对齐计划

## 背景

当前 `artemis-symphony` 已基本对齐 OpenAI Symphony 的核心运行时语义，但仍缺少官方参考实现中的“真实 live e2e”闭环。官方 `openai/symphony` 会在受环境变量保护的前提下，真实创建临时 Linear project / issue，启动真实 `codex app-server`，分别验证本地 worker 与 SSH worker，并要求 agent 通过 `linear_graphql` 写评论、推进 issue 到完成态。

用户要求不只建立说明，而是把这套能力在 Artemis 中落地到“可直接执行”的程度，并持续推进到可验证为止。

## 目标

- 为 `artemis-symphony` 增加一套受显式开关保护的真实 live e2e harness
- 对齐官方参考实现的关键验收标准：
  - 临时创建 Linear project / issue
  - 真实跑 `codex app-server`
  - 验证 workspace 副作用文件
  - 验证 `linear_graphql` 评论写回
  - 验证 issue 进入完成态
  - 同时覆盖 local worker 与 SSH worker
- 将入口脚本、测试支撑、docker SSH worker 支撑、文档与规范一起落仓

## 非目标

- 不追求 Phoenix LiveView 仪表盘层面的 UI 对齐
- 不把 live e2e 变成默认 `mvn test` 或 `mvn verify` 都会触发的高成本测试
- 不修改 Artemis 其他微服务的业务逻辑

## 范围

- `artemis-symphony/artemis-symphony-start`
- `artemis-symphony/test-support/live-e2e-docker`
- `scripts/e2e/`
- `artemis-symphony/README.md`
- 根 `README.md`
- 相关 OpenSpec artifact

## Checklist

- `[x]` 新增 live e2e 规范，明确“受开关保护、真实外部依赖、local + ssh 两场景”的稳定约束
- `[x]` 在 `artemis-symphony-start` 增加受环境变量保护的 live e2e 集成测试
- `[x]` 增加 Linear test client / helper，用于创建临时 project、issue、读取评论与完成态
- `[x]` 增加 local worker live e2e 场景
- `[x]` 增加 SSH worker live e2e 场景
- `[x]` 在未提供真实 SSH hosts 时，增加 docker fallback worker 支撑
- `[x]` 增加统一入口脚本，避免手工拼接 Maven / 环境变量命令
- `[x]` 在 README 中补齐前置条件、环境变量、执行方式与验收标准
- `[x]` 执行模块测试与至少一轮真实 live e2e 验证

## 分步实施

1. 先补计划与 OpenSpec，固定范围、入口与完成标准
2. 实现测试支撑与 local worker 场景，确保真实 `codex app-server` + `linear_graphql` 链路能跑通
3. 实现 SSH worker 场景与 docker fallback，补齐远程工作区与远程 app-server 真实 transport
4. 增加脚本入口与文档，降低后续重复运行成本
5. 执行单元验证与 live e2e，按结果回写计划状态与残余风险

## 风险

- 真实 `codex app-server` 与 Linear 都是外部依赖，失败模式会比普通单测复杂
- SSH worker docker 场景依赖 `docker`、`ssh-keygen` 与宿主机 `~/.codex/auth.json`
- live e2e 一旦没有显式保护，容易误创建 Linear 资源，因此必须保持 gated 语义

## 验证

- `source scripts/lib/common.sh && run_mvn -q test -pl artemis-symphony/artemis-symphony-start -am`
- `scripts/e2e/run-symphony-live-e2e.sh`

本轮实际通过：

- `bash -lc 'cd /Users/aotemiao/Documents/artemis && source scripts/lib/common.sh && run_mvn -q -pl artemis-symphony/artemis-symphony-start -am test -DskipTests=false'`
- `bash -lc 'cd /Users/aotemiao/Documents/artemis && export LINEAR_API_KEY=*** SYMPHONY_LIVE_LINEAR_TEAM_KEY=AOT && scripts/e2e/run-symphony-live-e2e.sh'`

收口说明：

- SSH live e2e 初始失败并非业务逻辑缺失，而是 docker fallback worker 在 OrbStack 环境下无法让 `workspaceWrite` sandbox 创建 user namespace，导致远端 `exec_command` 被 `bwrap` 拒绝。
- 已在 `artemis-symphony/test-support/live-e2e-docker/docker-compose.yml` 为 fallback worker 补齐 `privileged: true`，从而保留官方默认 sandbox 语义，而不是通过降级 sandbox 规避问题。
- live e2e helper 也补齐了 Linear GraphQL 瞬时 EOF 重试与 `SYMPHONY_LIVE_E2E_KEEP_ARTIFACTS=1` 调试现场保留能力。

通过标准：

- 未显式开启 live e2e 时，默认测试回路不会误触真实外部资源
- 开启 live e2e 后，至少可以稳定完成 local worker 场景
- 在具备 docker 或真实 SSH worker 条件时，SSH worker 场景也能完成同一验收链路
- 文档、脚本与测试实际行为一致
