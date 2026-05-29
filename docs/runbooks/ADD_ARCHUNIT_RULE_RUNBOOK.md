# 补 ArchUnit 约束 Runbook

Status: maintained
Last Reviewed: 2026-03-23
Review Cadence: 90 days

本 runbook 用于指导如何把“写在文档里的约束”逐步转成 ArchUnit 测试。

## 选择切入点

优先把以下高价值约束落成测试：

- `adapter` 不依赖 `infra`
- `app` 不依赖 `infra`
- `domain` 不依赖其他内部层
- `client` 不依赖实现层
- 调用方不依赖被调用方的内部模块

## 推荐步骤

1. 先确认约束已经写在 OpenSpec 或仓库入口文档中。
2. 选一个最小可执行规则，不要一次堆很多模糊断言。
3. 把测试放到最容易拿到完整 classpath 的模块。
4. 测试命名显式表达规则，例如 `SystemLayerDependencyRulesTest`。
5. 规则落仓后，同步更新 runbook / checklist / quality score。

## 推荐写法

- 使用 `ClassFileImporter().importPackages(...)`
- 一条测试只表达一类约束，便于失败时快速定位
- 失败信息尽量保留默认 ArchUnit 输出，便于查依赖链

## 至少要补的验证

- `mvn test`
- `scripts/harness/check-critical-path-tests.sh`
- 需要时运行 `scripts/harness/full-verify.sh`

## 常见风险

- 规则写得过宽，导致误报
- 没有先清理现存违例就直接把强规则绑到 CI
- 规则名和 OpenSpec 用语不一致，后续难维护
