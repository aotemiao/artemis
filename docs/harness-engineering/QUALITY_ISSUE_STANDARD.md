# 质量问题归档与关闭标准

Status: maintained
Last Reviewed: 2026-03-23
Review Cadence: 90 days

本文件定义如何记录、归档和关闭仓库中的质量问题，让问题治理可追踪、可复盘、可复用。

## 目录约定

- `docs/harness-engineering/quality-issues/active/`
  仍在处理中的质量问题
- `docs/harness-engineering/quality-issues/archive/`
  已关闭或已接受的历史问题

## 何时建档

出现以下情况之一时，建议建立质量问题记录：

- 新增守门脚本发现真实问题
- checklist / quality score 中出现需要连续治理的缺口
- 某类重复模式、测试缺口或文档漂移需要分阶段清理

## Active 记录至少包含

- 问题标题
- 背景与影响
- 当前状态
- 关闭条件
- 验证方式

## 关闭条件

满足以下条件后，可迁移到 archive：

- 对应代码、脚本、文档或 runbook 已落仓
- 至少一个仓库验证入口可重复证明问题已被控制
- 关闭日期与验证方式已写入归档记录

## 允许归档的例外

少量短期无法消除、但已明确边界和替代方案的问题，可以“接受并归档”。归档记录中必须写清：

- 为什么暂不继续处理
- 何时重新评估
- 依赖的替代守门或人工检查
