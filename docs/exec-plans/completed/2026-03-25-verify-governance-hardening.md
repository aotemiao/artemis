# Verify 治理检查可观测性加固

## 背景

GitHub Actions `Verify` workflow 在 `2026-03-25 16:38:17`（北京时间）针对提交 `f2b159d0b09f64b0de520295c6152d2ae3763e52` 失败。失败 job 为：

- workflow run: `23532147193`
- job: `68498310084`
- step: `Governance Checks`

公开页面与 annotations 只保留了 `Process completed with exit code 1.`，无法直接定位是哪个治理子检查失败；而同一提交在本地工作区与干净克隆中执行 `scripts/harness/run-governance-checks.sh` 均通过。这暴露出两个工程缺口：

1. `Verify` / `Governance` workflow 把 11 个治理脚本压缩在一个 step 中，失败时 GitHub UI 缺少精确定位。
2. 多个治理脚本依赖本地文件系统存在性判断，macOS 默认大小写不敏感，容易让“本地通过、Linux CI 失败”的路径大小写问题漏检。

## 目标

- 让 GitHub Actions 在治理检查失败时能直接显示具体失败的子检查名称
- 让本地治理检查对 repo 内路径执行大小写敏感校验，更贴近 Ubuntu CI
- 保持本地 `scripts/harness/run-governance-checks.sh` 仍然是统一入口

## 非目标

- 本计划不改变治理规则本身的业务含义
- 本计划不修改 Full Verify、镜像构建或普通单元测试逻辑

## Checklist

- `[x]` 为本次 CI 失败建立执行计划并记录已知事实
- `[x]` 为 repo 内路径检查补充大小写敏感公共能力
- `[x]` 将相关治理脚本切换到大小写敏感路径校验
- `[x]` 改进 `run-governance-checks.sh`，让本地失败也能直接指出失败子检查
- `[x]` 拆细 `verify.yml` 与 `governance.yml` 中的治理检查步骤
- `[x]` 本地验证治理检查、workflow 相关脚本与关键路径
- `[x]` 完成后归档计划并回写结果

## 计划实施

1. 补公共精确路径检查能力，优先覆盖治理脚本最常见的 repo 内文件断言场景
2. 修改治理脚本，让路径校验在 macOS 本地也能尽量模拟 Linux CI
3. 将 GitHub workflow 中的治理子检查拆成独立 step
4. 执行本地验证，并在需要时用干净克隆复验

## 验证

- `bash -n $(find scripts -type f -name '*.sh' | sort)`
- `scripts/harness/run-governance-checks.sh`
- `bash -lc 'cd /tmp/artemis-ci-repro && scripts/harness/run-governance-checks.sh'`

## 完成标准

- GitHub Actions 中治理检查失败时能直接看到具体失败 step
- 本地治理检查能拦住 repo 路径大小写问题，不再依赖 Linux CI 才发现
- 本地与干净克隆验证通过

## 本次结果

- 已为 repo 内路径检查补充大小写敏感公共能力：`repo_path_exists_exact` / `require_repo_path_exact`
- 已将多项治理脚本切换到精确路径校验，降低 macOS 本地与 Ubuntu CI 的差异
- 已将 `Verify` / `Governance` workflow 中的治理检查拆成独立 step，后续失败会直接显示具体子检查名
- 已改进本地统一入口 `scripts/harness/run-governance-checks.sh`，失败时会打印明确的子检查名称
- 后续在 Linux 容器中进一步确认，真实根因为 `docs/exec-plans/completed/2026-03-24-phase1-checklist.md` 中误提交了本机绝对路径链接；现已改为仓库内相对链接，并在 `check-doc-links.sh` 中禁止此类路径再次进入仓库

## 本次验证结果

- `bash -n $(find scripts -type f -name '*.sh' | sort)`：通过
- `scripts/harness/run-governance-checks.sh`：通过
- `bash -lc 'source scripts/lib/common.sh && run_in_repo_root && if repo_path_exists_exact readme.md; then echo BAD; exit 1; else echo exact-case-check-ok; fi'`：通过
- `bash -lc 'cd /tmp/artemis-ci-repro && git fetch --quiet /Users/aotemiao/Documents/artemis HEAD && git checkout --quiet FETCH_HEAD && bash -n $(find scripts -type f -name "*.sh" | sort) && scripts/harness/run-governance-checks.sh'`：通过
