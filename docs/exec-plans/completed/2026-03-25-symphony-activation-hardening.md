# Symphony 启用链路加固

## 背景

当前仓库已经落了 `artemis-symphony` 子工程、runbook、smoke 与 OpenSpec，但本地“实际启用 Symphony”的链路仍有明显断点：

- `scripts/dev/run-symphony.sh` 在无参数场景会因 Bash 兼容性问题直接退出
- 现有 `spring-boot:run` 调用会落到根 POM，无法正确启动目标模块
- `scripts/dev/service-status.sh` 的输出管道写法有问题，状态表会丢行
- Symphony 在 dispatch 预检失败时会直接中止启动，不利于本地先起 HTTP / reload 再补配置
- 部分文档对 Symphony 的本地启用前置条件说明还不够清晰

## 目标

- 修复 Symphony 以及同模式服务启动脚本的实际可用性
- 让 Symphony 在 workflow 已存在但 dispatch 配置不完整时仍可降级启动
- 修复状态查询与错误信封等明显工程缺口
- 补齐本地启用说明、排障路径与本地运行忽略项

## 非目标

- 本计划不接入真实 Linear 凭据或项目 slug
- 本计划不引入新的 tracker 类型或额外 worker 形态

## 范围

- `scripts/lib/` 与 `scripts/dev/` 中和启动、状态、配置相关的脚本
- `artemis-symphony` 的启动阶段、HTTP 运维接口与测试
- `README.md`、Symphony README / runbook / troublehooting 文档
- `.gitignore` 中的本地 Symphony 运行资产

## 风险

- 启动脚本从 `spring-boot:run` 切到“打包后运行 jar”会改变本地启动路径，需要补文档说明
- 放宽 Symphony 启动阶段的 fail-fast 后，必须靠日志和 HTTP 状态明确体现降级状态，避免误判为已可 dispatch
- 状态接口错误信封变更后，需要同步更新测试与文档，避免调用方认知偏差

## 分步任务

1. 修复公共启动脚本与 Symphony 启动入口
2. 修复状态脚本输出与本地运行资产管理
3. 调整 Symphony 启动阶段为“可降级启动，dispatch 失败不杀进程”
4. 修复 HTTP 错误信封与相关测试
5. 更新文档并执行脚本级、模块级验证

## 验证

- `mvn test -pl artemis-symphony/artemis-symphony-start -am`
- `scripts/dev/service-status.sh symphony`
- `scripts/dev/check-service-config.sh symphony`
- `scripts/dev/run-symphony.sh --symphony.workflow-path=<temp-workflow>`
- `scripts/smoke/symphony-state.sh <base-url>`

通过标准：

- 启动脚本可在仓库根目录直接拉起目标服务
- Symphony 在 dispatch 配置不完整时仍可起 HTTP 状态页
- 状态脚本能输出包含 `symphony` 的有效行
- 相关测试与 smoke 均通过

## 决策记录

- `2026-03-25`：启动脚本统一改为“根 reactor 打包目标模块后直接运行 boot jar”，避免 `spring-boot:run` 在多模块根 POM 上误触发
- `2026-03-25`：Symphony 启动阶段不再因 dispatch 预检失败而退出，改为降级启动并等待 workflow / env 修复

## 本次验证结果

- `scripts/dev/service-status.sh symphony`：通过，可输出包含 `symphony` 的有效状态行
- `SYMPHONY_WORKFLOW_PATH=/tmp/nonexistent-placeholder scripts/dev/check-service-config.sh symphony`：按预期失败，并给出复制 `WORKFLOW.md.example` 的修复提示
- `SYMPHONY_WORKFLOW_PATH=<temp-workflow> scripts/dev/check-service-config.sh symphony`：通过；当 `LINEAR_API_KEY` 未设置时仅输出告警，不阻断本地启动验证
- `SYMPHONY_WORKFLOW_PATH=<temp-workflow> scripts/dev/run-symphony.sh --server.port=9510`：通过；日志明确显示“以降级模式启动”
- `scripts/smoke/symphony-state.sh http://127.0.0.1:9510`：通过
- `scripts/harness/verify-changed.sh working-tree`：通过

## 收尾说明

- 本计划已归档；后续相关成果已继续收口到 `docs/exec-plans/completed/2026-03-25-symphony-linear-progress-comments.md`、`docs/exec-plans/completed/2026-03-25-symphony-reference-alignment.md` 与 `docs/exec-plans/completed/2026-03-25-symphony-live-e2e-alignment.md`
- 真实 Linear 凭据与项目 slug 仍由操作者自行配置，这不再阻断 Symphony 本地 HTTP 启动与状态排查
