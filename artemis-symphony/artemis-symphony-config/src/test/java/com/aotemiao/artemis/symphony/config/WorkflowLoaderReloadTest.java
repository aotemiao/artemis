package com.aotemiao.artemis.symphony.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowLoaderReloadTest {

    @Test
    void load_validMinimalWorkflow_succeeds(@TempDir Path dir) throws Exception {
        Path wf = dir.resolve("WORKFLOW.md");
        Files.writeString(
                wf,
                """
                ---
                tracker:
                  kind: linear
                  api_key: "test-key"
                  project_slug: "proj"
                ---
                Prompt body.
                """);
        WorkflowLoadResult r = WorkflowLoader.load(wf);
        assertInstanceOf(WorkflowLoadResult.Success.class, r);
    }

    @Test
    void load_invalidYamlFrontMatter_fails(@TempDir Path dir) throws Exception {
        Path wf = dir.resolve("WORKFLOW.md");
        Files.writeString(
                wf,
                """
                ---
                [ not valid yaml
                ---
                body
                """);
        WorkflowLoadResult r = WorkflowLoader.load(wf);
        assertInstanceOf(WorkflowLoadResult.Error.class, r);
        assertTrue(((WorkflowLoadResult.Error) r).message().length() > 0);
    }
}
