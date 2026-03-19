## ADDED Requirements

### Requirement: Pre-commit 检查项可用

项目 SHALL 在仓库中提供可选的 pre-commit 钩子实现（脚本或配置），使开发者在提交前可执行：**格式化**（仅针对本次 staged 的 Java 文件，由 Spotless 执行，Java 风格为 Palantir Java Format，根 POM 中可配置 PALANTIR/AOSP/GOOGLE）、**Checkstyle**（仅针对本次改动涉及的 Maven 模块）、**OpenSpec 变更未同步**检查（规则与行为见 `openspec/docs/pre-commit-openspec-sync-rule.md`）。钩子 SHALL 为可选安装，MUST NOT 强制所有提交必须通过钩子；CI 仍为规范守门的最终依据。

#### Scenario: 安装后提交仅改代码

- **WHEN** 开发者已安装提供的 pre-commit 钩子并提交仅包含 Java 代码改动的变更
- **THEN** 钩子 SHALL 对本次 staged 的 Java 文件执行格式化（若已配置，经 Spotless + Palantir Java Format）并对涉及模块执行 Checkstyle；若存在进行中 OpenSpec 变更且本次 staged 未触及该变更目录，SHALL 至少打印提醒（默认不阻止提交）

#### Scenario: 未安装钩子仍可提交

- **WHEN** 开发者未安装钩子或使用 `git commit --no-verify`
- **THEN** 提交 SHALL 可正常完成，不依赖本地钩子；CI 仍可对规范与测试进行校验

### Requirement: OpenSpec 未同步规则一致

Pre-commit 中的 OpenSpec 未同步逻辑 SHALL 与 `openspec/docs/pre-commit-openspec-sync-rule.md` 一致：进行中变更的目录约定、未同步判定条件、一次性例外（仅提交 OpenSpec、无进行中变更、可选路径范围排除）。默认行为 SHALL 为提醒模式（不阻止提交）；若实现支持严格模式，SHALL 文档说明如何切换及 `--no-verify` 的用途。

#### Scenario: 存在进行中变更且本次未改 OpenSpec

- **WHEN** 存在 `openspec/changes/<name>/`（非 archive）且本次 staged 文件均不落在任一此类目录下
- **THEN** 钩子 SHALL 判定为可能未同步并至少输出提醒；是否阻止提交由配置或默认（提醒模式）决定

#### Scenario: 无进行中变更不检查

- **WHEN** `openspec/changes/` 下除 `archive/` 外无其他子目录
- **THEN** 钩子 SHALL 不进行 OpenSpec 未同步检查，不输出相关提醒

### Requirement: 钩子安装与绕过文档化

项目 SHALL 在文档（README 或 openspec/docs 或贡献者文档）中说明：钩子脚本或配置的位置、如何安装（如复制到 `.git/hooks/pre-commit` 或设置 `core.hooksPath`）、以及如何绕过（`git commit --no-verify`）。文档 SHALL 明确钩子为可选，不安装不影响正常开发与 CI。

#### Scenario: 新开发者查找安装方式

- **WHEN** 开发者希望启用 pre-commit 检查
- **THEN** 文档 SHALL 提供安装步骤（如脚本路径与复制/链接命令），使开发者能在本地启用钩子
