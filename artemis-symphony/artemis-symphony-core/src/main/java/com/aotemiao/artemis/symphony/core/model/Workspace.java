package com.aotemiao.artemis.symphony.core.model;

import java.nio.file.Path;

/**
 * Filesystem workspace assigned to one issue identifier.
 *
 * @see <a href="https://github.com/openai/symphony/blob/main/SPEC.md">Symphony SPEC</a> Section 4.1.4
 */
public record Workspace(Path path, String workspaceKey, boolean createdNow) {}
