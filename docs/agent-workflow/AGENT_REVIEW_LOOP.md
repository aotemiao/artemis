# Agent 自评与 Reviewer 回路

Status: maintained
Last Reviewed: 2026-03-23
Review Cadence: 90 days

本文件用于把 agent 交付后的自评与 reviewer 复核步骤固化下来，避免最终说明只剩“改了哪些文件”。

## Agent 自评最小模板

交付前至少回答以下问题：

1. 本次改动触发了哪些约束或风险？
2. 我实际执行了哪些验证？
3. 哪些风险仍未覆盖，为什么？
4. 哪些文档、脚本、OpenSpec 已同步回写？

## Reviewer 复核重点

1. 入口文档是否仍能找到新增能力
2. 是否存在只改代码、不改脚本或文档的情况
3. 新约束是否有对应验证入口
4. 说明中的“已验证”是否真能由仓库脚本重复执行

## 推荐复核顺序

1. 读本次任务相关的执行计划或 runbook
2. 看 `scripts/harness/verify-changed.sh` / `full-verify.sh` 输出
3. 看新增测试、脚本和文档是否互相引用
4. 最后再看实现细节与风格
