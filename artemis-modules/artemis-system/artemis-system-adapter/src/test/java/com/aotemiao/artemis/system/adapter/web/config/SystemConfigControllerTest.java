package com.aotemiao.artemis.system.adapter.web.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.app.command.config.CreateSystemConfigCmdExe;
import com.aotemiao.artemis.system.app.command.config.DeleteSystemConfigCmdExe;
import com.aotemiao.artemis.system.app.command.config.RefreshSystemConfigCacheCmdExe;
import com.aotemiao.artemis.system.app.command.config.UpdateSystemConfigCmdExe;
import com.aotemiao.artemis.system.app.command.config.UpdateSystemConfigValueCmdExe;
import com.aotemiao.artemis.system.app.query.config.FindSystemConfigByIdQryExe;
import com.aotemiao.artemis.system.app.query.config.GetSystemConfigValueQryExe;
import com.aotemiao.artemis.system.app.query.config.SystemConfigPageQryExe;
import com.aotemiao.artemis.system.domain.model.config.SystemConfig;
import jakarta.servlet.ServletException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SystemConfigControllerTest {

    private MockMvc mockMvc;

    private CreateSystemConfigCmdExe createSystemConfigCmdExe;
    private UpdateSystemConfigCmdExe updateSystemConfigCmdExe;
    private UpdateSystemConfigValueCmdExe updateSystemConfigValueCmdExe;
    private DeleteSystemConfigCmdExe deleteSystemConfigCmdExe;
    private RefreshSystemConfigCacheCmdExe refreshSystemConfigCacheCmdExe;
    private FindSystemConfigByIdQryExe findSystemConfigByIdQryExe;
    private SystemConfigPageQryExe systemConfigPageQryExe;
    private GetSystemConfigValueQryExe getSystemConfigValueQryExe;

    @BeforeEach
    void setUp() {
        createSystemConfigCmdExe = mock(CreateSystemConfigCmdExe.class);
        updateSystemConfigCmdExe = mock(UpdateSystemConfigCmdExe.class);
        updateSystemConfigValueCmdExe = mock(UpdateSystemConfigValueCmdExe.class);
        deleteSystemConfigCmdExe = mock(DeleteSystemConfigCmdExe.class);
        refreshSystemConfigCacheCmdExe = mock(RefreshSystemConfigCacheCmdExe.class);
        findSystemConfigByIdQryExe = mock(FindSystemConfigByIdQryExe.class);
        systemConfigPageQryExe = mock(SystemConfigPageQryExe.class);
        getSystemConfigValueQryExe = mock(GetSystemConfigValueQryExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SystemConfigController(
                        createSystemConfigCmdExe,
                        updateSystemConfigCmdExe,
                        updateSystemConfigValueCmdExe,
                        deleteSystemConfigCmdExe,
                        refreshSystemConfigCacheCmdExe,
                        findSystemConfigByIdQryExe,
                        systemConfigPageQryExe,
                        getSystemConfigValueQryExe))
                .build();
    }

    @Test
    void create_returnsCreatedConfig() throws Exception {
        when(createSystemConfigCmdExe.execute(any())).thenReturn(sampleConfig());

        mockMvc.perform(post(SystemConfigController.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "configName": "账号注册开关",
                                  "configKey": "sys.account.registerUser",
                                  "configValue": "false",
                                  "systemBuiltIn": true,
                                  "remarks": "是否允许注册"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.configKey").value("sys.account.registerUser"))
                .andExpect(jsonPath("$.data.configValue").value("false"))
                .andExpect(jsonPath("$.data.systemBuiltIn").value(true));
    }

    @Test
    void update_returnsUpdatedConfig() throws Exception {
        SystemConfig updated = sampleConfig();
        updated.setConfigValue("true");
        when(updateSystemConfigCmdExe.execute(any())).thenReturn(updated);

        mockMvc.perform(put(SystemConfigController.BASE_PATH + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "configName": "账号注册开关",
                                  "configKey": "sys.account.registerUser",
                                  "configValue": "true",
                                  "systemBuiltIn": true,
                                  "remarks": "是否允许注册"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.configValue").value("true"));
    }

    @Test
    void updateValueByKey_returnsUpdatedConfig() throws Exception {
        SystemConfig updated = sampleConfig();
        updated.setConfigValue("true");
        when(updateSystemConfigValueCmdExe.execute(any())).thenReturn(updated);

        mockMvc.perform(put(SystemConfigController.BASE_PATH + "/key/{configKey}", "sys.account.registerUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "configValue": "true"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.configValue").value("true"));
    }

    @Test
    void getById_returnsConfig() throws Exception {
        when(findSystemConfigByIdQryExe.execute(any())).thenReturn(Optional.of(sampleConfig()));

        mockMvc.perform(get(SystemConfigController.BASE_PATH + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.configKey").value("sys.account.registerUser"));
    }

    @Test
    void getById_whenMissing_throwsBizException() {
        when(findSystemConfigByIdQryExe.execute(any())).thenReturn(Optional.empty());

        ServletException exception = assertThrows(
                ServletException.class, () -> mockMvc.perform(get(SystemConfigController.BASE_PATH + "/{id}", 99L)));

        assertThat(exception.getCause()).isInstanceOf(BizException.class);
    }

    @Test
    void page_returnsConfigPage() throws Exception {
        when(systemConfigPageQryExe.execute(any())).thenReturn(PageResult.of(1, List.of(sampleConfig()), 1));

        mockMvc.perform(get(SystemConfigController.BASE_PATH).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.content[0].configKey").value("sys.account.registerUser"));
    }

    @Test
    void getValueByKey_returnsValue() throws Exception {
        when(getSystemConfigValueQryExe.execute(any())).thenReturn(Optional.of("false"));

        mockMvc.perform(get(SystemConfigController.BASE_PATH + "/key/{configKey}", "sys.account.registerUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("false"));
    }

    @Test
    void delete_returnsOk() throws Exception {
        mockMvc.perform(delete(SystemConfigController.BASE_PATH + "/{id}", 1L)).andExpect(status().isOk());
    }

    @Test
    void refreshCache_returnsOk() throws Exception {
        mockMvc.perform(post(SystemConfigController.BASE_PATH + "/cache/refresh"))
                .andExpect(status().isOk());
    }

    private static SystemConfig sampleConfig() {
        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setId(1L);
        systemConfig.setConfigName("账号注册开关");
        systemConfig.setConfigKey("sys.account.registerUser");
        systemConfig.setConfigValue("false");
        systemConfig.setSystemBuiltIn(true);
        systemConfig.setRemarks("是否允许注册");
        return systemConfig;
    }
}
