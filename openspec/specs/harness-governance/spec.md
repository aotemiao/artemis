## ADDED Requirements

### Requirement: 周期性治理任务

仓库 SHALL 提供一个可周期执行的治理入口，用于统一执行文档整理、重复模式扫描、质量问题检查与相关报告输出。该入口 SHALL 可在本地脚本和 CI 定时任务中复用，避免出现“规则写了但无人执行”的状态。

#### Scenario: CI 定时执行治理入口

- **WHEN** GitHub Actions 的定时任务触发
- **THEN** SHALL 调用仓库内治理脚本，完成文档与工程治理检查，并输出可定位失败原因的日志

### Requirement: 重复模式扫描

仓库 SHALL 提供重复模式扫描脚本，至少覆盖以下高频熵增点：

- DTO / Request / Response 类
- 异常处理类
- `scripts/` 下的 shell 入口

扫描脚本 SHALL 使用稳定、可解释的归一化规则识别高度重复实现，并在发现重复模式时给出对应文件列表。

#### Scenario: 扫描重复 DTO

- **WHEN** 两个 DTO 或 Request/Response 文件在去除 package/import/空白差异后内容完全一致
- **THEN** 扫描脚本 SHALL 报告重复文件组并使检查失败

### Requirement: 质量问题归档与关闭标准

仓库 SHALL 为质量问题提供统一归档与关闭标准，至少定义：

- 问题如何建档
- 什么信号可视为关闭
- 何时从 active 状态迁移到 archive

该标准 SHALL 沉淀在仓库文档中，并被周期性治理入口引用。

#### Scenario: 质量问题达到关闭条件

- **WHEN** 某质量问题对应的脚本、测试、文档与验证入口均已落仓，并在标准验证中通过
- **THEN** 该问题 SHALL 可按文档约定迁移到 archive，并记录关闭日期与验证方式
