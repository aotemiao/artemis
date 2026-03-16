# Design: OpenSpec Archive 不纳入 Git

## Context

- **当前状态**：`openspec/changes/archive/` 下有多条按日期命名的归档变更，与当前实现或 specs 多已不同步。
- **目标**：所有 Archived Changes 都不提交到 Git；initial commit 及后续提交中仓库不包含 archive 内容。
- **约束**：不改变 OpenSpec 本地工作流（归档仍可存在于本地）；不修改 `openspec/specs/` 或活跃变更。

## Goals / Non-Goals

**Goals:**

- 确保 `openspec/changes/archive/` 及其子内容不被 Git 追踪、不进入版本库。
- 通过 `.gitignore` 实现排除，无需删除本地 archive 目录，本地仍可保留归档供参考。

**Non-Goals:**

- 不删除本地 archive 目录或其中任何文件。
- 不评估「哪些归档有价值」——统一不提交。

## Decisions

### 1. 使用 .gitignore 排除整个 archive

- **选择**：在项目根目录 `.gitignore` 中增加一行：`openspec/changes/archive/`（或等效规则），使该目录及子内容均被忽略。
- **理由**：实现简单、可逆；本地 archive 保留，仅从版本库中排除；无需逐条评估或物理删除。
- **备选**：物理删除整个 archive 再提交——会丢失本地归档，已否决。

### 2. 不提交任何 archive，不做白名单

- **选择**：不采用「默认忽略 + 个别目录用 `!` 拉回」；所有 archive 一律不提交。
- **理由**：与用户决策「Archived Changes 都不要提交」一致，避免后续维护白名单。

## Risks / Trade-offs

| 风险 | 缓解 |
|------|------|
| 日后若希望某条归档进库 | 可从 .gitignore 中暂时移除或使用 `git add -f` 单次提交；或将该归档移出 archive 目录后提交。 |
| 新 clone 的仓库无 archive 内容 | 预期内；archive 视为本地/个人历史，不共享到版本库。 |

## Migration Plan

1. 在 `.gitignore` 中添加 `openspec/changes/archive/`。
2. 若该目录曾被 `git add` 过（例如在未忽略前已暂存），执行 `git rm -r --cached openspec/changes/archive/` 以从索引中移除，避免仍被提交。
3. 执行 initial commit（或后续提交），确认 `git status` 中无 `openspec/changes/archive/` 下文件。

## Open Questions

- 无。
