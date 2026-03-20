## ADDED Requirements

### Requirement: WORKFLOW.md 运行时重载

`artemis-symphony` SHALL 在进程运行期间监听已配置的 `WORKFLOW.md` 路径（或其所在目录）的文件变更，并在检测到变更后重新加载 YAML 前件与提示模板。重载成功后，后续 poll 间隔、并发上限、tracker 配置、hooks、codex 超时等 SHALL 从新配置生效。重载失败时 SHALL 保留上一次有效配置并输出操作者可见的错误日志，MUST NOT 因单次失败终止编排主循环。

#### Scenario: 有效 WORKFLOW 更新后配置生效

- **WHEN** 操作者保存对 `WORKFLOW.md` 的合法修改且解析与校验通过
- **THEN** 服务 SHALL 在合理延迟内将新配置应用于后续 tick、重试调度与 agent 启动（已在运行的会话不要求自动重启）

#### Scenario: 非法 WORKFLOW 更新不破坏当前运行

- **WHEN** 保存的 `WORKFLOW.md` 无法解析或导致 dispatch 前校验失败
- **THEN** 服务 SHALL 继续使用上一次有效配置并记录错误，编排循环 SHALL 保持存活
