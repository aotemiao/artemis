# Pre-commit：OpenSpec 变更未同步规则

## 目录约定

- **进行中变更**：`openspec/changes/` 下、**非** `archive/` 子目录的任意一级子目录，视为一个进行中的 change（例如 `openspec/changes/my-feature/`）。仅当变更已归档到 `openspec/changes/archive/YYYY-MM-DD-<name>/` 后，不再视为进行中。
- **变更目录**：指该进行中 change 的根目录及其所有子路径（如 `openspec/changes/my-feature/` 与 `openspec/changes/my-feature/design.md`、`openspec/changes/my-feature/specs/...` 等）。

## 未同步判定

在一次提交（commit）中，若**同时**满足以下两条，则视为 **OpenSpec 变更未同步**：

1. **存在至少一个进行中变更**：当前仓库中存在至少一个 `openspec/changes/<name>/`（`<name>` 不是 `archive`）。
2. **本次 staged 文件未触及任一进行中变更**：本次 `git diff --cached --name-only` 得到的文件列表中，**没有任何**文件路径落在任一进行中变更目录下（即不以 `openspec/changes/<name>/` 为前缀）。

含义：在有进行中 change 的前提下，若本次提交只改了代码或其它非 OpenSpec 内容，而未改动该 change 下的任何文件，则判定为可能「只改代码、未同步文档」，可触发提醒或拦截。

## 一次性例外

以下情况 **不** 视为未同步（即不触发拦截/提醒，或应在实现中排除）：

- **仅提交 OpenSpec 的提交**：本次 staged 文件全部落在某进行中变更目录下（例如新建 change 或只更新 tasks/design），允许通过。
- **无进行中变更**：`openspec/changes/` 下除 `archive/` 外无其它子目录，不检查。
- **仅改动的路径不在约定范围内**：例如只改 `README.md`、`docker/`、`config/` 等与变更文档无直接关联的路径时，若团队约定此类提交可不关联 change，可在规则中排除（可选：仅当 staged 中包含 `artemis-*` 或 `openspec/specs/` 等「与实现/规范强相关」的路径时才做未同步检查，避免所有提交都被要求带 OpenSpec 变更）。

## 建议实现行为

- **提醒模式**：检测到未同步时，仅打印提示（如「存在进行中变更 xxx，本次提交未包含其目录下文件，请确认是否需更新 tasks/design」），`exit 0`，不阻止提交。
- **严格模式**：检测到未同步时，`exit 1` 并输出上述提示，阻止提交；开发者可通过 `git commit --no-verify` 在确有理由时绕过。

团队可先采用提醒模式，再视情况是否启用严格模式。
