## ADDED Requirements

### Requirement: 归档不纳入版本库

OpenSpec 归档目录（`openspec/changes/archive/`）下的内容 SHALL 不被 Git 追踪，SHALL NOT 随任何 commit 提交到版本库。归档仅作为本地或工作区历史存在，不进入仓库。

#### Scenario: 提交时 archive 未被包含

- **WHEN** 执行任意 commit（含 initial commit）
- **THEN** 仓库中 SHALL NOT 包含 `openspec/changes/archive/` 下的任何文件或目录

#### Scenario: 忽略规则生效

- **WHEN** 项目配置（如 `.gitignore`）已按约定排除 `openspec/changes/archive/`
- **THEN** 该目录及其子内容 SHALL 不出现在 `git status` 的已追踪或待提交列表中
