package com.aotemiao.artemis.symphony.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aotemiao.artemis.symphony.persistence.RunHistoryEvent;
import com.aotemiao.artemis.symphony.persistence.RunHistoryMetrics;
import com.aotemiao.artemis.symphony.persistence.RunHistoryRecord;
import com.aotemiao.artemis.symphony.persistence.RunHistoryRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SymphonyHistoryController.class)
class SymphonyHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RunHistoryRepository runHistoryRepository;

    @Test
    void listRuns_returnsRecentRunHistory() throws Exception {
        when(runHistoryRepository.listRecentRuns(5))
                .thenReturn(List.of(new RunHistoryRecord(
                        "run-1",
                        "issue-1",
                        "ART-1",
                        "SQLite history",
                        "In Progress",
                        "completed",
                        0,
                        "worker-1",
                        "/tmp/symphony/ART-1",
                        "thread-1",
                        "session-1",
                        "1234",
                        11,
                        22,
                        33,
                        "",
                        Instant.parse("2026-06-03T01:00:00Z"),
                        Instant.parse("2026-06-03T01:01:00Z"),
                        Instant.parse("2026-06-03T01:02:00Z"))));

        mockMvc.perform(get("/api/v1/history/runs?limit=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limit", is(5)))
                .andExpect(jsonPath("$.runs[0].run_id", is("run-1")))
                .andExpect(jsonPath("$.runs[0].issue_identifier", is("ART-1")))
                .andExpect(jsonPath("$.runs[0].tokens.total_tokens", is(33)));
    }

    @Test
    void listEvents_returnsRunEvents() throws Exception {
        when(runHistoryRepository.listRunEvents("run-1", 10))
                .thenReturn(List.of(new RunHistoryEvent(
                        1L,
                        "run-1",
                        Instant.parse("2026-06-03T01:00:05Z"),
                        "turn_completed",
                        "session-1",
                        "{\"usage\":{\"total_tokens\":33}}")));

        mockMvc.perform(get("/api/v1/history/runs/run-1/events?limit=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.run_id", is("run-1")))
                .andExpect(jsonPath("$.events[0].event_type", is("turn_completed")))
                .andExpect(jsonPath("$.events[0].payload", containsString("total_tokens")));
    }

    @Test
    void metrics_returnsRunHistorySummary() throws Exception {
        when(runHistoryRepository.summarizeRecentRuns(100))
                .thenReturn(new RunHistoryMetrics(
                        100,
                        4,
                        2,
                        1,
                        1,
                        0,
                        0,
                        1,
                        40,
                        60,
                        100,
                        12.5,
                        Instant.parse("2026-06-03T01:00:00Z"),
                        Instant.parse("2026-06-03T01:10:00Z"),
                        Map.of("completed", 2, "failed", 1, "terminated", 1),
                        Map.of("codex_runtime", 1, "terminated", 1)));

        mockMvc.perform(get("/api/v1/history/metrics?limit=100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limit", is(100)))
                .andExpect(jsonPath("$.metrics.total_runs", is(4)))
                .andExpect(jsonPath("$.metrics.completed_runs", is(2)))
                .andExpect(jsonPath("$.metrics.tokens.total_tokens", is(100)))
                .andExpect(jsonPath("$.metrics.status_counts.completed", is(2)))
                .andExpect(jsonPath("$.metrics.failure_category_counts.codex_runtime", is(1)));
    }

    @Test
    void runsPage_returnsHtml() throws Exception {
        mockMvc.perform(get("/runs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Symphony 运行历史")))
                .andExpect(content().string(containsString("运行指标")))
                .andExpect(content().string(containsString("failure_category_counts")))
                .andExpect(content().string(containsString("/api/v1/history/metrics")))
                .andExpect(content().string(containsString("/api/v1/history/runs")));
    }
}
