# 2026-03-23 OpenSpec 与当前框架对齐

## 背景

`openspec/specs/` 中有一部分规范仍保留旧架构假设，已经和当前仓库的实际实现、根文档与构建配置出现偏移。主要表现为：

- 内部跨服务调用方式仍有旧的 REST 说法，与当前 `*-client + Dubbo` 约定冲突
- 少量规范仍使用旧 starter 名称（`artemis-common-*`）
- 个别规范仍残留 MySQL、本地 bootstrap 二选一、MyBatis Mapper 默认实现等旧叙述
- 某些规范内部自相矛盾，或与当前模块结构（含 `*-client`）不完全一致

## 目标

- 将 OpenSpec 对齐到当前仓库事实来源：`README.md`、`ARCHITECTURE.md`、`pom.xml`、实际代码与配置
- 先消除规范之间的直接冲突，再修正文档漂移，减少后续 agent 和开发者误读
- 保持“内部 RPC / 外部 REST”“默认 JDBC / 可选 MyBatis”“默认 PostgreSQL”这些主约束表述一致

## 非目标

- 不在本计划中调整业务代码行为
- 不在本计划中重写所有 spec，只修复已确认不一致或冲突的条目
- 不在本计划中扩展新的工程约束

## 范围

- 会改动 `openspec/specs/` 下与当前框架不一致的规范文件
- 会新增一份执行计划记录本次对齐过程与结论
- 不会改动服务实现逻辑、数据库脚本或运行脚本

## 风险

- 多份 spec 之间存在交叉引用，修一处时若表述不一致，容易引入新的文档冲突
- 某些规范同时承担“默认约定”和“可选能力说明”，收口时需要避免把可选能力误写成默认能力

## 分步任务

1. 建立本执行计划，并把发现的问题整理为代办项
2. 修复 `repository-structure` 中仍要求跨服务统一走外部 REST 的旧表述
3. 修复 `per-domain-client-contracts` 中将 gateway / 第三方也视为 `*-client` 消费方的混淆表述
4. 修复 `engineering-constraints` 中本地基础设施仍写 MySQL 的残留
5. 修复 `tech-stack` 中旧 starter 名称、Dubbo 注解表述与 Java 21 preview 要求
6. 修复 `internal-rpc-dubbo` 中消费者注解与当前实现不一致的问题
7. 修复 `coding-standards` 中 infra 默认命名仍偏向 MyBatis Mapper 的问题
8. 修复 `nacos-config-management` 中 `config.import` 与 bootstrap 并存的冲突表述
9. 修复 `ddd-cola-layering` 中对业务模块结构未体现 `*-client` 契约模块的问题
10. 运行仓库验证脚本，确认本次 OpenSpec 对齐未破坏文档一致性

## 验证

- 已执行 `scripts/harness/check-openspec-sync.sh`
- 已执行 `scripts/harness/check-doc-links.sh`
- 已执行 `scripts/harness/check-doc-consistency.sh`
- 已执行 `scripts/harness/verify-changed.sh working-tree`
- 通过标准：脚本退出码为 0，且本次修改后的 spec 不再出现已确认的冲突表述

## 决策记录

- `2026-03-23`：本次以“对齐当前仓库事实来源”为准，不回退到旧的 REST-only / MySQL-only / common-* 命名体系
- `2026-03-23`：在修 spec 的同时，顺手收口 Dubbo 注册中心外置方式与 `verify-changed.sh` 的空数组分支，避免事实来源与验证入口继续漂移

## 遗留问题

- 本轮计划范围内无阻塞性遗留问题
