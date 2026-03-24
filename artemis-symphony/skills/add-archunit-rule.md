# 补 ArchUnit 约束 Skill

适用场景：需要把已有工程规则转成可执行测试。

## 先读

- `docs/harness-engineering/ADD_ARCHUNIT_RULE_RUNBOOK.md`
- 对应 OpenSpec

## 输出要求

- 一次只补一组高价值规则
- 规则名称与 OpenSpec 用语保持一致
- 补完后运行相关模块测试与 `scripts/harness/check-critical-path-tests.sh`
