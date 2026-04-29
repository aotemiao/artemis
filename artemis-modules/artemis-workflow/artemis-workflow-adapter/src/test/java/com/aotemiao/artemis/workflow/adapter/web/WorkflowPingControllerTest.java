package com.aotemiao.artemis.workflow.adapter.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aotemiao.artemis.workflow.app.query.ping.GetWorkflowPingQryExe;
import com.aotemiao.artemis.workflow.domain.model.ping.ServicePing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class WorkflowPingControllerTest {

    private MockMvc mockMvc;

    private GetWorkflowPingQryExe getWorkflowPingQryExe;

    @BeforeEach
    void setUp() {
        getWorkflowPingQryExe = mock(GetWorkflowPingQryExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new WorkflowPingController(getWorkflowPingQryExe))
                .build();
    }

    @Test
    void ping_should_return_payload() throws Exception {
        when(getWorkflowPingQryExe.execute())
                .thenReturn(new ServicePing("artemis-workflow", "Service scaffold is ready"));

        mockMvc.perform(get(WorkflowPingController.BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.serviceCode").value("artemis-workflow"))
                .andExpect(jsonPath("$.data.capability").value("flow-category"))
                .andExpect(jsonPath("$.data.message").value("Service scaffold is ready"));
    }
}
