# Symphony 参考实现对齐计划

## 背景

当前 `artemis-symphony` 已覆盖 OpenAI Symphony SPEC 的核心链路，并补上了启动收口、状态页、评论回写等能力，但与参考实现 `/Users/aotemiao/IdeaProjects/symphony/elixir` 仍存在一批明确差异。用户目标不是继续做“自有变体”，而是尽量与参考实现的运行时能力、配置语义和默认工作流保持一致。

## 目标

- 建立一份可执行的参考实现对齐 checklist，并把实现、测试、文档统一回写到仓库
- 让 Java 版 Symphony 在 tracker、Codex app-server 交互、workflow/config 语义上尽量对齐参考实现
- 补齐用户已经明确感知到的关键缺口，例如 `Todo -> In Progress` 认领与 Linear 交互能力

## 非目标

- 本计划不追求按 Elixir/Phoenix 技术栈逐行复刻参考实现
- 本计划不要求在没有可验证环境的前提下引入无法落地的外部依赖
- 本计划不修改 Artemis 其他微服务的业务行为

## 范围

- `artemis-symphony/artemis-symphony-config`
- `artemis-symphony/artemis-symphony-tracker`
- `artemis-symphony/artemis-symphony-agent`
- `artemis-symphony/artemis-symphony-orchestrator`
- `artemis-symphony/artemis-symphony-workspace`
- `artemis-symphony/artemis-symphony-start`
- `artemis-symphony/README.md`
- `artemis-symphony/WORKFLOW.md.example`
- 相关 OpenSpec、执行计划、测试和验证脚本

## 风险

- 运行时协议对齐会碰到当前 `codex app-server` 版本兼容问题，必须边补边测
- tracker 写能力引入后，若状态更新时机选错，可能导致 Linear 状态抖动或误写
- worker/沙箱默认值一旦变化，必须保证现有 `WORKFLOW.md` 显式配置仍然兼容

## 参考差异 Checklist

- `[x]` 建立 tracker 抽象层，避免 orchestration 直接绑定 `LinearTrackerClient`
- `[x]` 支持 `tracker.kind: memory` 的内存适配器，便于本地/测试对齐
- `[x]` 支持 Linear `issueUpdate` 状态变更 mutation
- `[x]` 在 worker 真正开始执行时，将 `Todo` 自动推进到 `In Progress`
- `[x]` 在 `thread/start` 中宣告 `dynamicTools`
- `[x]` 支持 `linear_graphql` 动态工具执行与结果回传
- `[x]` 支持 `item/tool/call`、approval 请求等 app-server 消息处理
- `[x]` 支持 `tracker.assignee` 路由语义，并在 issue 脱离当前 worker 路由时停止运行
- `[x]` 对齐默认 prompt 兜底与 continuation prompt 语义
- `[x]` 对齐 `codex.turn_sandbox_policy` 默认行为，避免缺省时完全失去 turn 级工作区约束
- `[x]` 对齐 `worker.ssh_hosts` / `max_concurrent_agents_per_host` 语义，支持远程 workspace、远程 hook、远程 app-server 与按 host 复用重试
- `[x]` 支持 `codex.approval_policy` 的 string / map 两种配置形态
- `[x]` 更新 `WORKFLOW.md.example` 与 README，使默认 workflow 语义更接近参考实现
- `[x]` 用单元测试、smoke、本地实跑完成对齐验证

## 分步任务

1. 固化对齐清单，并标出运行时、配置、文档三类差异
2. 先补 tracker 读写抽象、memory adapter 与 `Todo -> In Progress`
3. 再补 `linear_graphql` 动态工具和 app-server 消息处理
4. 对齐默认 prompt、turn sandbox、assignee 路由和 worker 配置语义
5. 更新文档、OpenSpec、测试与本地验证，收口剩余差异

## 验证

- `source scripts/lib/common.sh && run_mvn test -pl artemis-symphony/artemis-symphony-start -am`
- `scripts/dev/check-service-config.sh symphony`
- `scripts/smoke/symphony-state.sh`
- `scripts/dev/check-service-readiness.sh symphony 3 1`
- 通过本地 `WORKFLOW.md` + Linear issue 实跑至少一次 dispatch

通过标准：

- Checklist 中的核心运行时差异都有对应代码与测试落地
- 用户创建的 `Todo` issue 能被 Symphony 认领并推进到 `In Progress`
- Java 版 Symphony 能处理参考实现中的 `linear_graphql` 动态工具链路
- 文档、示例 workflow、状态页与实际行为保持一致

## 决策记录

- `2026-03-25`：以“运行时能力对齐”为优先，不按 UI 或语言细节逐行模仿参考实现
- `2026-03-25`：用户已明确要求与参考项目对齐，因此新增稳定行为时同步补执行计划与相关 OpenSpec

## 遗留问题

- 远端 SSH worker 已通过假 ssh 包装脚本完成本地回归，但若要验证真实远端主机、端口与凭证联通性，仍需补一次有环境支撑的实跑确认
