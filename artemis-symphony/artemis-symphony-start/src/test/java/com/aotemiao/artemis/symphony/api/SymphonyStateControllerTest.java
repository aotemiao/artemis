package com.aotemiao.artemis.symphony.api;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aotemiao.artemis.symphony.core.model.Issue;
import com.aotemiao.artemis.symphony.orchestrator.Orchestrator;
import com.aotemiao.artemis.symphony.orchestrator.RunningEntry;
import com.aotemiao.artemis.symphony.workspace.WorkspaceManager;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SymphonyStateController.class)
class SymphonyStateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Orchestrator orchestrator;

    @MockBean
    private WorkspaceManager workspaceManager;

    @Test
    void postRefresh_returnsAcceptedWithQueued() throws Exception {
        when(orchestrator.requestImmediateTick()).thenReturn(false);
        mockMvc.perform(post("/api/v1/refresh"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.queued", is(true)))
                .andExpect(jsonPath("$.coalesced", is(false)));
    }

    @Test
    void postRefresh_whenCoalesced_trueInBody() throws Exception {
        when(orchestrator.requestImmediateTick()).thenReturn(true);
        mockMvc.perform(post("/api/v1/refresh"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.coalesced", is(true)));
    }

    @Test
    void getIssue_whenUnknown_returns404Envelope() throws Exception {
        when(orchestrator.findRunningByIdentifier("X-1")).thenReturn(null);
        when(orchestrator.findRetryByIdentifier("X-1")).thenReturn(null);
        mockMvc.perform(get("/api/v1/issues/X-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("not_found")));
    }

    @Test
    void getIssue_whenRunning_includesWorkspacePath() throws Exception {
        Issue issue = new Issue("id-1", "MT-1", "t", null, 1, "Todo", null, null, List.of(), List.of(), null, null);
        RunningEntry entry = new RunningEntry("id-1", "MT-1", issue, 0, Instant.parse("2025-01-01T00:00:00Z"));
        when(orchestrator.findRunningByIdentifier("MT-1")).thenReturn(entry);
        when(workspaceManager.getWorkspaceRoot())
                .thenReturn(Path.of("/symphony").toAbsolutePath());
        mockMvc.perform(get("/api/v1/issues/MT-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phase", is("running")))
                .andExpect(jsonPath("$.issue_identifier", is("MT-1")));
    }
}
