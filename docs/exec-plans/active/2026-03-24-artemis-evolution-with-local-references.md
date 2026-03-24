# Artemis 演进计划（基于本地参考源）

## 背景

Artemis 已完成本轮 Harness Engineering 封板，仓库入口、验证脚本、治理回路、runbook 与 agent 资产已形成稳定骨架。接下来的主要矛盾不再是“缺少工程入口”，而是“如何把现有骨架复制到更多真实业务、真实部署与真实协作中”。

本计划将演进讨论锚定到三个本地参考源，避免只凭印象讨论：

- `RuoYi-Cloud-Plus`：`/Users/aotemiao/IdeaProjects/RuoYi-Cloud-Plus`
- `COLA`：`/Users/aotemiao/IdeaProjects/COLA`
- `aws-sdk-java-v2`：`/Users/aotemiao/IdeaProjects/aws-sdk-java-v2`

## 目标

- 在 90 天内把 Artemis 从“已有一个样板业务服务的工程骨架”推进为“可持续扩业务、可稳定交付、可被 agent 复用的后台平台”
- 明确三个参考源各自的借鉴边界，避免混用设计语言
- 将后续演进优先级聚焦到“服务复制能力、契约治理、运行时可靠性、agent 交付能力”

## 非目标

- 本计划不要求一次性把 Artemis 做成 RuoYi-Cloud-Plus 的功能等价替代
- 本计划不直接照搬 COLA 组件或 AWS SDK 的全部模块化策略
- 本计划不在当前阶段引入新的重型基础设施平台或大规模重构

## 当前判断

### 已具备的能力

- 仓库级文档、脚本、CI、治理与质量门已形成闭环
- `artemis-system` 已具备可复制的 COLA 五层样板
- `artemis-auth -> artemis-system-client -> artemis-system` 的内部契约链路已建立
- `scripts/harness/verify-changed.sh` 与 `scripts/harness/full-verify.sh` 已可以作为团队默认验证入口

### 当前主要短板

- 业务模块复制能力还没有被产品化，`artemis-system` 仍更像“手工样板”而不是“服务模板”
- 契约治理刚起步，缺少更明确的版本化、兼容性和回归策略
- 部署 / 回滚 runbook 已有，但真实环境演练仍不足
- `artemis-symphony` 已有基础资产，但尚未成为标准交付引擎

## 参考策略

### 1. 参考 `RuoYi-Cloud-Plus`

适合借鉴：

- 后台平台能力版图：认证、网关、系统管理、资源、任务、工作流等服务划分
- `ruoyi-api` 聚合与 `*-api-*` 契约模块组织方式
- 配置中心、部署脚本、环境切换、Nacos 约定与运维入口
- “能跑起来、能部署、能扩模块”的平台工程思路

不建议直接照搬：

- 功能面过宽时的一次性全量铺开
- 为兼容大量历史能力而形成的复杂插件面
- 与 Artemis 当前 DDD/COLA 边界不一致的模块内聚方式

对 Artemis 的直接启发：

- 新增 `artemis-api/` 聚合层，统一收敛 `*-client` 契约模块与未来的客户端 BOM
- 以“系统、资源、任务、工作流”而不是纯技术分层来规划下一批业务服务
- 让部署、环境、脚本入口继续保持平台化，而不是散落到单个服务

### 2. 参考 `COLA`

适合借鉴：

- `client / adapter / app / domain / infrastructure / start` 的职责边界
- 以业务为核心、隔离技术复杂度的分层理念
- 通过 archetype 固化服务模板，而不是靠口头约定复用
- 应用服务、命令查询、领域规则、网关接口的稳定组织方式

不建议直接照搬：

- 为了“像 COLA”而引入当前并不急需的全部组件
- 忽略 Artemis 现有微服务边界，退回到单体式 package 分层
- 把领域模型抽象到脱离当前业务阶段的程度

对 Artemis 的直接启发：

- 以 `artemis-system` 为基线，沉淀 Artemis 自己的领域服务模板与脚本生成入口
- 继续使用 ArchUnit 与 OpenSpec 让分层约束可执行
- 对新增服务坚持“先建 client 契约，再建 app/domain/infra/adaptor/start”

### 3. 参考 `aws-sdk-java-v2`

适合借鉴：

- BOM 优先、模块最小依赖、按需引入模块的依赖治理方式
- 清晰的 core / spi / transport / service modules 结构
- builder 风格、可插拔实现、兼容性意识和长期演进视角
- 大仓库下通过测试与兼容性检查维持演进速度的方式

不建议直接照搬：

- 过早把 Artemis 切成过细的技术模块
- 为了“可插拔”而引入当前用不到的 SPI 层数
- 使用面向通用 SDK 的设计去替代业务服务的领域建模

对 Artemis 的直接启发：

- 在内部契约模块上引入更明确的 BOM / 聚合管理
- 对 `*-client` 保持最小依赖原则，不把服务内部实现泄露给调用方
- 为未来的客户端兼容性测试、弃用策略和版本管理预留机制

## 建议的总体演进方向

### Phase 1：把 `artemis-system` 产品化成服务模板（0-30 天）

目标：让新增一个业务服务不再需要手工拼装。

关键动作：

1. 建立 `artemis-api/` 聚合模块
2. 在 `artemis-api/` 下按服务沉淀 `*-client`，并新增客户端 BOM
3. 基于 `artemis-system` 提炼领域服务模板，提供脚本入口或模板目录
4. 将 ArchUnit、JaCoCo、API 文档、client 契约、readiness、smoke 作为模板默认件
5. 补“新增业务服务”执行 runbook，明确最小交付清单

完成标准：

- 新增一个领域服务时，不需要从零拷贝验证与治理脚本
- 调用方默认只依赖 `artemis-api` 或对应 `*-client`
- 新服务天然继承文档、测试、契约和守门能力

### Phase 2：围绕平台主链路扩业务，而不是只扩工程（30-60 天）

目标：把 Artemis 从“工程骨架”推进到“有代表性业务平台”。

建议优先顺序：

1. 完整补齐 `artemis-system` 的核心主数据能力
2. 梳理认证与系统服务之间的职责边界
3. 视实际需求新增 `resource` 或 `workflow` 这类第二个领域服务

优先补齐的系统域能力：

- 用户、角色、菜单、部门、租户
- 字典、配置、审计
- 认证所需的最小账户与权限查询接口

完成标准：

- `artemis-auth` 不再承载超出认证本身的业务逻辑
- `artemis-system` 成为真正的系统域服务，而不是只保留示例能力
- 第二个领域服务能复用第一阶段沉淀的模板与守门

### Phase 3：把运行时可靠性做成默认能力（60-90 天）

目标：把“能开发”推进到“能稳定运行和回滚”。

关键动作：

1. 为关键服务补充更细的启动失败分类、慢启动断言和故障 smoke
2. 将部署 / 回滚 runbook 升级为周期性演练动作
3. 补充链路追踪、日志关联、关键端点 SLA 与故障观测入口
4. 让镜像、部署、回滚验证进入默认交付回路

完成标准：

- 关键服务具备清晰的启动、健康、日志、故障与回滚入口
- 每次发布不只验证编译通过，还验证关键链路能运行
- 真实环境问题可以通过 runbook 和脚本重复定位

### Phase 4：把 `artemis-symphony` 推进成标准交付引擎（并行推进）

目标：让 agent 不只“会改代码”，还“会按仓库规则交付”。

关键动作：

1. 将 exec plan、skills、prompts、review loop 与验证脚本更紧密串联
2. 为“新增服务、补契约、扩 smoke、做回滚演练”提供任务模板
3. 让 Symphony 能自动产出计划、执行、验证、归档与 handoff

完成标准：

- 常见工程任务具备固定任务资产和回放路径
- agent 的输出默认带验证结果与风险说明
- 复杂任务减少对聊天上下文的依赖

## 结构级建议

### 建议新增 `artemis-api/`

参考来源：

- `RuoYi-Cloud-Plus/ruoyi-api`
- `RuoYi-Cloud-Plus/ruoyi-api/ruoyi-api-bom`
- `aws-sdk-java-v2/bom`

建议用途：

- 聚合所有 `*-client`
- 提供内部客户端 BOM
- 让调用方统一通过契约依赖服务能力

### 建议新增服务模板入口

参考来源：

- `COLA/cola-archetypes/cola-archetype-web`
- `artemis-modules/artemis-system`

建议形态：

- `scripts/dev/new-domain-service.sh`
- 或 `templates/domain-service/`
- 或二者并存：模板目录 + 生成脚本

模板默认包含：

- `client / domain / infra / app / adapter / start`
- 最小测试、ArchUnit、JaCoCo、API 文档、smoke、readiness

### 建议强化客户端治理

参考来源：

- `aws-sdk-java-v2/bom`
- `aws-sdk-java-v2/services`

建议动作：

- 明确 `*-client` 的最小依赖边界
- 增加契约兼容性回归测试
- 增加弃用标记与升级说明模板

## 近期优先任务

1. 建立 `artemis-api/` 聚合与客户端 BOM 设计稿
2. 抽取 `artemis-system` 作为新增服务模板的最小骨架
3. 为 `artemis-system` 补齐用户、角色、菜单、部门、租户主链路
4. 为第二个领域服务挑选范围并验证模板可复制性
5. 为部署 / 回滚建立一次真实环境演练计划
6. 为 Symphony 增加“新增服务”和“契约变更”任务模板

## 风险

- 若直接对标 RuoYi 的功能面，可能在业务尚未成形时引入过宽范围
- 若过度强调 COLA 纯度，可能导致模板复杂度高于当前团队收益
- 若过度模仿 AWS SDK 的模块化粒度，可能形成过早抽象
- 若不先完成模板化，就直接扩更多服务，会放大手工复制成本

## 验证与里程碑

### 30 天里程碑

- 本地参考源路径已切换到当前机器可读路径
- 完成 `artemis-api/` 与服务模板方案设计
- 形成一条“新增服务”的标准交付路线

### 60 天里程碑

- `artemis-system` 主链路能力明显补齐
- 至少有一个新领域服务按模板落地
- 客户端契约与兼容性治理开始成为默认回路

### 90 天里程碑

- 部署 / 回滚演练完成至少一轮
- 关键服务具备可重复的运行时验证入口
- Symphony 可以覆盖至少一类复杂工程任务的端到端交付

## 决策记录

- `2026-03-24`：将三个参考源统一切换到当前机器的本地路径，避免继续依赖失效的 Windows 路径
- `2026-03-24`：RuoYi-Cloud-Plus 主要用于平台能力与工程入口参考，COLA 主要用于分层与模板参考，AWS SDK v2 主要用于 BOM、客户端与模块治理参考
- `2026-03-24`：Artemis 下一阶段优先目标定为“复制能力 + 契约治理 + 运行时可靠性”，而不是继续增加仓库元文档
