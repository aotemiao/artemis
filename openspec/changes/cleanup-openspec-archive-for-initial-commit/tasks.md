# Tasks: OpenSpec Archive 不纳入 Git

## 1. 配置忽略规则

- [x] 1.1 在项目根目录 `.gitignore` 中添加 `openspec/changes/archive/`，使该目录及子内容不被 Git 追踪
- [x] 1.2 若 `openspec/changes/archive/` 已被 Git 追踪，执行 `git rm -r --cached openspec/changes/archive/` 从索引中移除（不删本地文件）

## 2. 提交前确认

- [x] 2.1 执行 `git status`，确认无 `openspec/changes/archive/` 下文件被列入待提交
- [ ] 2.2 执行 initial commit（或由实施者在合适时机执行），确保提交内容包含代码、`openspec/specs/`、活跃变更目录，且不包含任何 archive
