# Proposal: OpenSpec Archive 不纳入 Git（Initial Commit）

## Why

项目即将进行 initial commit。`openspec/changes/archive/` 下存在大量按日期归档的变更目录，多与当前代码和 `openspec/specs/` 已不同步。若提交到 Git，会造成历史噪音与新人困惑。决定：**所有 Archived Changes 都不提交**，仓库中仅保留当前 `openspec/specs/` 与活跃变更，使 initial commit 保持清晰。

## What Changes

- 在 `.gitignore` 中排除 `openspec/changes/archive/`，使该目录及其内容不被 Git 追踪。
- 本地可继续使用 OpenSpec 的 archive 工作流（归档仍存在于工作区），但 archive 不会进入版本库。
- initial commit 及后续提交中，仓库不包含任何 archive 下的变更目录。

## Capabilities

### New Capabilities

- `openspec-archive-retention`：约定 OpenSpec 归档目录（`openspec/changes/archive/`）不纳入版本库；归档仅作为本地/工作区历史，不提交到 Git。

### Modified Capabilities

- 无（不改变任何现有 spec 的行为或需求）。

## Impact

- **.gitignore**：新增对 `openspec/changes/archive/` 的排除规则。
- **openspec/changes/archive/**：仍存在于本地文件系统，但不被 Git 追踪、不随 commit 提交。
- **仓库内容**：initial commit 及之后仅包含 `openspec/specs/`、活跃变更目录（如 nacos-logback-no-exclusion、optimize-dependency-management）及业务代码，不包含任何 archive。
