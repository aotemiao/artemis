package com.aotemiao.artemis.resource.adapter.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aotemiao.artemis.resource.app.query.GetResourcePingQryExe;
import com.aotemiao.artemis.resource.domain.model.ServicePing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ResourcePingControllerTest {

    private MockMvc mockMvc;

    private GetResourcePingQryExe getResourcePingQryExe;

    @BeforeEach
    void setUp() {
        getResourcePingQryExe = mock(GetResourcePingQryExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ResourcePingController(getResourcePingQryExe))
                .build();
    }

    @Test
    void ping_should_return_payload() throws Exception {
        when(getResourcePingQryExe.execute())
                .thenReturn(new ServicePing("artemis-resource", "Service scaffold is ready"));

        mockMvc.perform(get(ResourcePingController.BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.serviceCode").value("artemis-resource"))
                .andExpect(jsonPath("$.data.message").value("Service scaffold is ready"));
    }
}
