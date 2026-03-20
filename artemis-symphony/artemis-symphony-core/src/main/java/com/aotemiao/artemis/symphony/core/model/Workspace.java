package com.aotemiao.artemis.symphony.core.model;

import java.nio.file.Path;

/**
 * 绑定到某一议题标识的文件系统工作区。
 *
 * @see <a href="https://github.com/openai/symphony/blob/main/SPEC.md">Symphony SPEC</a> 第 4.1.4 节
 */
public record Workspace(Path path, String workspaceKey, boolean createdNow) {}
