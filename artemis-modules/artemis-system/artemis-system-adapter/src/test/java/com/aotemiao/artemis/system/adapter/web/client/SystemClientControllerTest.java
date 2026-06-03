package com.aotemiao.artemis.system.adapter.web.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.app.command.client.CreateSystemClientCmdExe;
import com.aotemiao.artemis.system.app.command.client.DeleteSystemClientCmdExe;
import com.aotemiao.artemis.system.app.command.client.UpdateSystemClientCmdExe;
import com.aotemiao.artemis.system.app.query.client.FindSystemClientByIdQryExe;
import com.aotemiao.artemis.system.app.query.client.SystemClientPageQryExe;
import com.aotemiao.artemis.system.domain.model.client.SystemClient;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SystemClientControllerTest {

    private MockMvc mockMvc;
    private CreateSystemClientCmdExe createSystemClientCmdExe;
    private UpdateSystemClientCmdExe updateSystemClientCmdExe;
    private DeleteSystemClientCmdExe deleteSystemClientCmdExe;
    private FindSystemClientByIdQryExe findSystemClientByIdQryExe;
    private SystemClientPageQryExe systemClientPageQryExe;

    @BeforeEach
    void setUp() {
        createSystemClientCmdExe = mock(CreateSystemClientCmdExe.class);
        updateSystemClientCmdExe = mock(UpdateSystemClientCmdExe.class);
        deleteSystemClientCmdExe = mock(DeleteSystemClientCmdExe.class);
        findSystemClientByIdQryExe = mock(FindSystemClientByIdQryExe.class);
        systemClientPageQryExe = mock(SystemClientPageQryExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SystemClientController(
                        createSystemClientCmdExe,
                        updateSystemClientCmdExe,
                        deleteSystemClientCmdExe,
                        findSystemClientByIdQryExe,
                        systemClientPageQryExe))
                .build();
    }

    @Test
    void create_returnsCreatedClient() throws Exception {
        when(createSystemClientCmdExe.execute(any())).thenReturn(sampleClient());

        mockMvc.perform(post(SystemClientController.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.clientId").value("artemis-admin"));
    }

    @Test
    void update_returnsUpdatedClient() throws Exception {
        SystemClient client = sampleClient();
        client.setStatus("DISABLED");
        when(updateSystemClientCmdExe.execute(any())).thenReturn(client);

        mockMvc.perform(put(SystemClientController.BASE_PATH + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sampleJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DISABLED"));
    }

    @Test
    void getById_returnsClient() throws Exception {
        when(findSystemClientByIdQryExe.execute(any())).thenReturn(Optional.of(sampleClient()));

        mockMvc.perform(get(SystemClientController.BASE_PATH + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.clientKey").value("artemis-admin-web"));
    }

    @Test
    void page_returnsClientPage() throws Exception {
        when(systemClientPageQryExe.execute(any())).thenReturn(PageResult.of(1, List.of(sampleClient()), 1));

        mockMvc.perform(get(SystemClientController.BASE_PATH).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.content[0].clientId").value("artemis-admin"));
    }

    @Test
    void delete_returnsOk() throws Exception {
        mockMvc.perform(delete(SystemClientController.BASE_PATH + "/{id}", 1L)).andExpect(status().isOk());
    }

    private static String sampleJson() {
        return """
                {
                  "clientId": "artemis-admin",
                  "clientKey": "artemis-admin-web",
                  "clientSecret": "change-me",
                  "grantTypes": "password,refresh_token",
                  "deviceType": "PC",
                  "activeTimeoutSeconds": 1800,
                  "fixedTimeoutSeconds": 86400,
                  "status": "NORMAL"
                }
                """;
    }

    private static SystemClient sampleClient() {
        SystemClient client = new SystemClient();
        client.setId(1L);
        client.setClientId("artemis-admin");
        client.setClientKey("artemis-admin-web");
        client.setClientSecret("change-me");
        client.setGrantTypes("password,refresh_token");
        client.setDeviceType("PC");
        client.setActiveTimeoutSeconds(1800L);
        client.setFixedTimeoutSeconds(86400L);
        client.setStatus("NORMAL");
        return client;
    }
}
