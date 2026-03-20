package com.aotemiao.artemis.symphony.orchestrator;

import com.aotemiao.artemis.symphony.config.WorkflowLoadResult;
import com.aotemiao.artemis.symphony.config.WorkflowLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SymphonyRuntimeHolderTest {

    private static final String VALID =
            """
            ---
            tracker:
              kind: linear
              api_key: "k"
              project_slug: "alpha"
            ---
            Hi
            """;

    @Test
    void tryReloadFromDisk_invalidFile_keepsSnapshot(@TempDir Path dir) throws Exception {
        Path wf = dir.resolve("WORKFLOW.md");
        Files.writeString(wf, VALID);
        WorkflowLoadResult first = WorkflowLoader.load(wf);
        assertInstanceOf(WorkflowLoadResult.Success.class, first);
        var holder =
                new SymphonyRuntimeHolder(wf, SymphonyRuntimeHolder.buildSnapshot(((WorkflowLoadResult.Success) first).definition()));
        assertEquals("alpha", holder.get().config().getTrackerProjectSlug());

        Files.writeString(
                wf,
                """
                ---
                [ broken
                ---
                x
                """);
        assertFalse(holder.tryReloadFromDisk());
        assertEquals("alpha", holder.get().config().getTrackerProjectSlug());
    }

    @Test
    void tryReloadFromDisk_validUpdate_replacesSnapshot(@TempDir Path dir) throws Exception {
        Path wf = dir.resolve("WORKFLOW.md");
        Files.writeString(wf, VALID);
        WorkflowLoadResult first = WorkflowLoader.load(wf);
        var holder =
                new SymphonyRuntimeHolder(wf, SymphonyRuntimeHolder.buildSnapshot(((WorkflowLoadResult.Success) first).definition()));
        assertEquals("alpha", holder.get().config().getTrackerProjectSlug());

        Files.writeString(
                wf,
                VALID.replace("project_slug: \"alpha\"", "project_slug: \"beta\""));
        assertTrue(holder.tryReloadFromDisk());
        assertEquals("beta", holder.get().config().getTrackerProjectSlug());
    }
}
