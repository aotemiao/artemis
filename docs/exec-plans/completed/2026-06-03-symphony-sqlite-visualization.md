# Symphony SQLite 运行记录与可视化计划

Status: completed
Last Reviewed: 2026-06-03
Review Cadence: 90 days

## 背景

`artemis-symphony` 当前已具备 Linear 拉取、workspace、Codex app-server、重试、状态 API 与日志能力，但运行记录主要保存在内存和日志中。服务重启后无法查看历史 attempt，也缺少一个面向本地操作者的简单页面来观察运行历史、事件和重试结果。

本计划根据当前评估建议，先补最小可用的本地 SQLite 运行记录与可视化界面，让 Symphony 从“当前状态可见”推进到“历史运行可追溯”。

## 关联需求与规范

- Feature Spec：无需 Feature Spec。本次不涉及业务域规则、用户目录、权限模型或对外业务 API。
- OpenSpec：`openspec/specs/symphony-run-history/spec.md`
- Runbook / API / 其它资产：`artemis-symphony/README.md`、`artemis-symphony/WORKFLOW.md.example`、`scripts/smoke/symphony-state.sh`

## 目标

- 为 Symphony 增加本地 SQLite 运行记录，保存 run、attempt、关键 Codex / worker 事件和失败原因。
- 提供简单可视化页面，能通过浏览器访问运行总览、最近运行、单个 issue 的事件。
- 保持现有 `/api/v1/state` 接口不破坏，并新增历史查询 API。
- 使用仓库脚本和模块测试验证改动。

## 非目标

- 不实现多租户控制台、鉴权体系或生产级审计平台。
- 不实现 PR 创建、CI 状态回收或人工审批恢复。
- 不替换现有内存调度状态；SQLite 先作为运行记录与可视化数据源。

## 范围

- 新增 `artemis-symphony-persistence` 模块。
- 修改 `artemis-symphony-orchestrator` 写入运行记录。
- 修改 `artemis-symphony-start` 暴露历史 API 与静态 UI。
- 更新 Symphony README、workflow 示例和 OpenSpec。

## 风险

- SQLite 写入失败不应中断 worker 主链路。
- 事件 payload 可能包含较长文本，需要限制页面展示和 API 输出范围。
- 本地数据库路径必须默认落在仓库运行目录可控位置，避免误写系统目录。

## 风险分类

| 风险项 | 是否涉及 | 方案检查点 | Reviewer 关注点 |
|--------|----------|------------|-----------------|
| 领域建模 | 否 | 仅记录编排运行态，不进入业务领域模型 | 不应误放到业务 DDD 模块 |
| 权限 / 安全 | 是 | UI 默认仅本地运维面；不写入密钥；payload 展示做长度限制 | 是否泄露 Linear token、Codex auth 或完整敏感输出 |
| 幂等 / 并发 / 锁 | 是 | SQLite 表用 run_id / event_id 主键；写入方法同步并启用 busy_timeout | 并发 worker 写入是否稳定 |
| 事务 / 数据一致性 | 是 | 单次 run/event 写入使用短事务或单语句 upsert | 失败是否影响主链路 |
| SQL 性能 | 是 | 按 started_at / issue_identifier 查询；列表分页限制 | 是否无界查询历史 |
| 日志 / 可观测性 | 是 | 记录 run lifecycle 与事件；API/UI 可查看历史 | 是否能定位失败 attempt |

## 任务拆解

| 编号 | 任务 | 输入 | 输出 | 验收标准 |
|------|------|------|------|----------|
| T-001 | 定义运行历史规范与计划 | 评估报告、现有 Symphony 文档 | OpenSpec 与执行计划 | 已完成：规范说明 SQLite、API、UI 最小能力 |
| T-002 | 新增 SQLite persistence 模块 | 现有 Maven/BOM | repository、record、测试 | 已实现，等待模块测试 |
| T-003 | 编排器写入运行记录 | Orchestrator / AgentRunner / Codex 事件 | run start / finish / event 记录 | 已实现，等待模块测试 |
| T-004 | HTTP API 与 UI | start 模块 | `/api/v1/history/**` 与 `/runs` 页面 | 已实现，等待 MockMvc 测试 |
| T-005 | 文档与验证 | README、workflow 示例、脚本 | 使用说明与验证结果 | 文档已更新，等待 `verify-changed` 或模块级验证 |

## 分步执行

1. 新增 OpenSpec 与执行计划。
2. 新增 `artemis-symphony-persistence` 模块和 SQLite schema 初始化。
3. 在 Spring 装配中创建默认 SQLite repository，并注入 Orchestrator。
4. 在 worker start / Codex event / worker exit / retry 调度处记录历史。
5. 新增历史 API 与简单 HTML 页面。
6. 补测试、文档并执行验证。

## 验收映射

| 验收编号 | 来源 | 验证入口 | 通过标准 |
|----------|------|----------|----------|
| AC-001 | 本计划 | `mvn -B -pl artemis-symphony/artemis-symphony-persistence -am test` | SQLite schema 初始化、run/event 查询测试通过 |
| AC-002 | 本计划 | `mvn -B -pl artemis-symphony/artemis-symphony-start -am test` | API/UI 装配与 MockMvc 测试通过 |
| AC-003 | 本计划 | `scripts/harness/verify-changed.sh working-tree` | OpenSpec、治理检查与模块 verify 通过 |

## 验证结果

- `JAVA_HOME=$(/usr/libexec/java_home -v 21) PATH="$JAVA_HOME/bin:$PATH" mvn -B -pl artemis-symphony/artemis-symphony-persistence -am test`：通过。
- `JAVA_HOME=$(/usr/libexec/java_home -v 21) PATH="$JAVA_HOME/bin:$PATH" mvn -B -pl artemis-symphony/artemis-symphony-start -am test`：通过。
- `scripts/harness/verify-changed.sh working-tree`：通过。脚本在 OpenSpec sync 阶段提示仓库已有 `openspec/changes/remove-artemis-visual` 进行中变更，本次未改该 change；后续 governance、Spotless、Checkstyle、SpotBugs、模块 verify 和 JaCoCo 均通过。

## 验证计划

- 先执行新增模块和 start 模块测试。
- 最后执行 `scripts/harness/verify-changed.sh working-tree`。
- 若本地环境耗时过长，至少保留失败输出和已通过的模块级测试。

## 验证分类

| 变更类型 | 最小验证入口 | 是否需要 |
|----------|--------------|----------|
| 文档 / 规范 | `scripts/harness/run-governance-checks.sh` | 是 |
| Java / POM | `scripts/harness/verify-changed.sh working-tree` | 是 |
| API / 契约 | `mvn -B -pl artemis-symphony/artemis-symphony-start -am test` | 是 |
| 数据迁移 | `mvn -B -pl artemis-symphony/artemis-symphony-persistence -am test` | 是 |
| 服务行为 | `scripts/smoke/symphony-state.sh` 或人工访问 `/runs` | 是 |
| agent 编排 | `scripts/harness/check-symphony-assets.sh` | 是 |

## 回滚策略

- 回滚新增 persistence 模块、Orchestrator 注入点、HTTP API/UI 和文档即可。
- SQLite 数据库是本地运行资产，不参与生产业务数据迁移；删除本地 `symphony_runs.sqlite` 可清理运行历史。

## 决策记录

- `2026-06-03`：选择本地 SQLite 作为 Symphony 运行历史默认存储；该存储只记录编排运行态，不替换业务服务的 PostgreSQL / Spring Data JDBC 默认持久化规则。

## 遗留问题

- 后续可继续补人工审批恢复、PR/CI 闭环和 OpenTelemetry traces。
