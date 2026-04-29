# Workflow Runtime 专项计划

## 背景

`docs/system-requirements-ddd.md` 中 `WF-004` 到 `WF-010` 已进入流程运行时范围，覆盖启动、任务办理、我的任务、流程监控、实例管理、任务操作、状态枚举、节点办理人和按钮权限。当前 `artemis-workflow` 已完成流程分类、表达式和流程定义元数据，但还没有运行时实例、任务和节点模型。

## 目标

- 在 `artemis-workflow` 内分阶段补齐运行时最小闭环。
- 保持现有 DDD/COLA 分层：adapter 只做协议转换，app 承担用例编排，domain 表达业务模型和 Gateway，infra 负责持久化。
- 每个阶段都包含代码、迁移、测试、API 文档、清单回写和验证。

## 非目标

- 不在首个运行时提交中接入完整 BPMN 引擎。
- 不把系统域用户、角色、部门、岗位查询直接写进 app/adapter；跨域查询应通过 client 防腐层或后续 workflow resolver 网关。
- 不在没有实例表和任务表的情况下实现复杂加减签、委派、转办的最终行为。

## 分阶段 Checklist

### RT-001 流程运行时基础模型

- [ ] 新增 `flow_instances`、`flow_tasks`、`flow_his_tasks`、`flow_instance_biz_ext` 迁移。
- [ ] 建立 `FlowInstance`、`FlowTask`、`FlowHistoryTask`、`FlowInstanceBizExt` 领域模型和 Gateway。
- [ ] 定义流程状态枚举：`draft`、`waiting`、`finish`、`cancel`、`back`、`invalid`、`termination`。
- [ ] 定义任务历史状态枚举：撤销、通过、待审核、作废、退回、终止、转办、委托、抄送、加签、减签、超时。
- [ ] 提供基础转换器、Repository 和 Gateway 测试。

### RT-002 流程启动最小闭环

- [ ] 启动流程必须提供业务 ID 和流程编码。
- [ ] 仅允许已发布且激活的流程定义启动。
- [ ] 写入发起人、发起人部门、业务 ID 等流程变量。
- [ ] 保存业务扩展信息：业务编码、业务标题、业务 ID、实例 ID。
- [ ] 同一业务 ID 已存在实例时按状态校验是否允许再次提交。
- [ ] 启动后生成且只生成一个申请人初始任务。

### RT-003 任务办理最小闭环

- [ ] 支持办理当前任务，保存审批意见、附件、变量和消息请求。
- [ ] 任务不存在或已审批时拒绝办理。
- [ ] 流程实例不存在时拒绝办理。
- [ ] 草稿、已撤销、已退回流程再次办理时识别为重新提交。
- [ ] 办理通过后生成后续任务和历史任务。

### RT-004 我的任务与流程监控查询

- [ ] 查询当前用户待办、已办、抄送任务。
- [ ] 查询全部待办、全部已办任务，用于流程监控。
- [ ] 查询当前用户发起的流程实例。
- [ ] 查询运行中和已结束流程实例。
- [ ] 支持按业务 ID 查询流程实例详情。
- [ ] 输出流程图、审批历史和当前任务详情的最小数据结构。

### RT-005 流程实例管理

- [ ] 支持按业务 ID 删除流程实例。
- [ ] 支持按实例 ID 删除运行中实例和已完成历史实例。
- [ ] 删除、撤销、作废等操作校验创建人或管理员权限。
- [ ] 撤销前校验流程未完成、未作废、未终止、未退回、未撤销。
- [ ] 支持流程实例激活、挂起、查询变量和修改变量。

### RT-006 高级任务操作

- [ ] 支持终止任务。
- [ ] 支持驳回审批，并查询可驳回的前置节点。
- [ ] 支持委派、转办、加签、减签。
- [ ] 仅会签或票签节点允许加减签。
- [ ] 支持批量修改任务办理人和查询当前任务所有办理人。
- [ ] 支持催办任务，并预留站内信、邮件、短信消息通道。

### RT-007 节点办理人与按钮权限

- [ ] 节点办理人来源支持用户、角色、部门、岗位和 SpEL 表达式。
- [ ] SpEL 表达式支持 `$` 和 `#` 前缀变量策略。
- [ ] 节点按钮权限支持弹窗选人、委托、转办、抄送、退回、加签、减签、终止。
- [ ] 建立节点解析 Gateway，避免 app 层直接依赖系统 infra。

## 验证策略

- 每个阶段至少运行：
  - `mvn -B -pl artemis-modules/artemis-workflow/artemis-workflow-infra,artemis-modules/artemis-workflow/artemis-workflow-adapter,artemis-modules/artemis-workflow/artemis-workflow-start -am spotless:apply verify`
  - `scripts/harness/verify-changed.sh working-tree`
  - `git -c core.whitespace=blank-at-eol,blank-at-eof,space-before-tab,cr-at-eol diff --check`
- 涉及远程调用时补充 client contract 同步检查。
- 涉及 smoke 能力时补充 `scripts/smoke/workflow-*.sh`。

## 风险与决策

- `2026-04-30`：运行时先按自研轻量任务状态机推进，不直接引入 BPMN 引擎；后续若接入引擎，需要通过 domain gateway 隔离。
- `2026-04-30`：发布校验会在 RT-007 升级为节点级校验，当前 `WF-003` 的 JSON 标记校验作为过渡保护。
- `2026-04-30`：组织人员解析通过跨域 Gateway 或 client 防腐层实现，不允许 app/adapter 直接依赖系统 infra。
