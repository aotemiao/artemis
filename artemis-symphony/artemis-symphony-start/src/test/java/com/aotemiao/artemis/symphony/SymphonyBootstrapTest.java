package com.aotemiao.artemis.symphony;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.symphony.api.SymphonyStateController;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

class SymphonyBootstrapTest {

    @TempDir
    Path tempDir;

    private final WebApplicationContextRunner contextRunner =
            new WebApplicationContextRunner().withUserConfiguration(SymphonyApplication.class);

    @Test
    void contextLoadsWhenDispatchConfigIsIncomplete() throws IOException {
        Path workflow = tempDir.resolve("WORKFLOW.md");
        Files.writeString(workflow, """
                ---
                tracker:
                  kind: linear
                workspace:
                  root: ./symphony_workspaces
                codex:
                  command: codex app-server
                ---

                Test prompt
                """);

        contextRunner
                .withPropertyValues("symphony.workflow-path=" + workflow.toAbsolutePath(), "server.port=0")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(SymphonyStateController.class);
                });
    }
}
