package com.aotemiao.artemis.resource.adapter.web;

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
import com.aotemiao.artemis.resource.app.command.config.ChangeOssConfigStatusCmdExe;
import com.aotemiao.artemis.resource.app.command.config.CreateOssConfigCmdExe;
import com.aotemiao.artemis.resource.app.command.config.DeleteOssConfigCmdExe;
import com.aotemiao.artemis.resource.app.command.config.SetDefaultOssConfigCmdExe;
import com.aotemiao.artemis.resource.app.command.config.UpdateOssConfigCmdExe;
import com.aotemiao.artemis.resource.app.query.config.FindOssConfigByIdQryExe;
import com.aotemiao.artemis.resource.app.query.config.OssConfigPageQryExe;
import com.aotemiao.artemis.resource.domain.model.config.OssConfig;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class OssConfigControllerTest {

    private MockMvc mockMvc;
    private CreateOssConfigCmdExe createOssConfigCmdExe;
    private UpdateOssConfigCmdExe updateOssConfigCmdExe;
    private DeleteOssConfigCmdExe deleteOssConfigCmdExe;
    private ChangeOssConfigStatusCmdExe changeOssConfigStatusCmdExe;
    private SetDefaultOssConfigCmdExe setDefaultOssConfigCmdExe;
    private FindOssConfigByIdQryExe findOssConfigByIdQryExe;
    private OssConfigPageQryExe ossConfigPageQryExe;

    @BeforeEach
    void setUp() {
        createOssConfigCmdExe = mock(CreateOssConfigCmdExe.class);
        updateOssConfigCmdExe = mock(UpdateOssConfigCmdExe.class);
        deleteOssConfigCmdExe = mock(DeleteOssConfigCmdExe.class);
        changeOssConfigStatusCmdExe = mock(ChangeOssConfigStatusCmdExe.class);
        setDefaultOssConfigCmdExe = mock(SetDefaultOssConfigCmdExe.class);
        findOssConfigByIdQryExe = mock(FindOssConfigByIdQryExe.class);
        ossConfigPageQryExe = mock(OssConfigPageQryExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new OssConfigController(
                        createOssConfigCmdExe,
                        updateOssConfigCmdExe,
                        deleteOssConfigCmdExe,
                        changeOssConfigStatusCmdExe,
                        setDefaultOssConfigCmdExe,
                        findOssConfigByIdQryExe,
                        ossConfigPageQryExe))
                .build();
    }

    @Test
    void page_returnsConfigs() throws Exception {
        when(ossConfigPageQryExe.execute(any())).thenReturn(PageResult.of(1, List.of(sampleConfig()), 1));

        mockMvc.perform(get(OssConfigController.BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].configKey").value("local"));
    }

    @Test
    void getById_returnsConfig() throws Exception {
        when(findOssConfigByIdQryExe.execute(any())).thenReturn(Optional.of(sampleConfig()));

        mockMvc.perform(get(OssConfigController.BASE_PATH + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bucket").value("bucket"));
    }

    @Test
    void create_returnsConfig() throws Exception {
        when(createOssConfigCmdExe.execute(any())).thenReturn(sampleConfig());

        mockMvc.perform(post(OssConfigController.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.provider").value("LOCAL"));
    }

    @Test
    void update_returnsConfig() throws Exception {
        when(updateOssConfigCmdExe.execute(any())).thenReturn(sampleConfig());

        mockMvc.perform(put(OssConfigController.BASE_PATH + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.configKey").value("local"));
    }

    @Test
    void changeStatus_returnsConfig() throws Exception {
        when(changeOssConfigStatusCmdExe.execute(any())).thenReturn(sampleConfig());

        mockMvc.perform(put(OssConfigController.BASE_PATH + "/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void setDefault_returnsConfig() throws Exception {
        when(setDefaultOssConfigCmdExe.execute(any())).thenReturn(sampleConfig());

        mockMvc.perform(put(OssConfigController.BASE_PATH + "/{id}/default", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.defaultFlag").value(1));
    }

    @Test
    void delete_returnsTrue() throws Exception {
        mockMvc.perform(delete(OssConfigController.BASE_PATH + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    private static String requestJson() {
        return """
                {
                  "configKey":"local",
                  "accessKey":"access",
                  "secretKey":"secret",
                  "bucket":"bucket",
                  "provider":"local",
                  "status":1,
                  "defaultFlag":1,
                  "builtIn":0
                }
                """;
    }

    private static OssConfig sampleConfig() {
        OssConfig config = new OssConfig();
        config.setId(1L);
        config.setConfigKey("local");
        config.setAccessKey("access");
        config.setSecretKey("secret");
        config.setBucket("bucket");
        config.setPrefix("prefix");
        config.setEndpoint("endpoint");
        config.setCustomDomain("cdn.example.com");
        config.setHttpsEnabled(true);
        config.setRegion("cn-hz");
        config.setAccessPolicy("PRIVATE");
        config.setStatus(1);
        config.setDefaultFlag(1);
        config.setBuiltIn(0);
        config.setProvider("LOCAL");
        config.setExtJson("{}");
        return config;
    }
}
