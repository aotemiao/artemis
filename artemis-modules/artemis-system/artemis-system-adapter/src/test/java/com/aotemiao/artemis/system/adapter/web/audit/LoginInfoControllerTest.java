package com.aotemiao.artemis.system.adapter.web.audit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.app.command.audit.ClearLoginInfoCmdExe;
import com.aotemiao.artemis.system.app.command.audit.DeleteLoginInfoCmdExe;
import com.aotemiao.artemis.system.app.query.audit.FindLoginInfoByIdQryExe;
import com.aotemiao.artemis.system.app.query.audit.LoginInfoPageQryExe;
import com.aotemiao.artemis.system.domain.model.audit.LoginInfo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class LoginInfoControllerTest {

    private MockMvc mockMvc;

    private LoginInfoPageQryExe loginInfoPageQryExe;
    private FindLoginInfoByIdQryExe findLoginInfoByIdQryExe;

    @BeforeEach
    void setUp() {
        loginInfoPageQryExe = mock(LoginInfoPageQryExe.class);
        findLoginInfoByIdQryExe = mock(FindLoginInfoByIdQryExe.class);
        DeleteLoginInfoCmdExe deleteLoginInfoCmdExe = mock(DeleteLoginInfoCmdExe.class);
        ClearLoginInfoCmdExe clearLoginInfoCmdExe = mock(ClearLoginInfoCmdExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new LoginInfoController(
                        loginInfoPageQryExe, findLoginInfoByIdQryExe, deleteLoginInfoCmdExe, clearLoginInfoCmdExe))
                .build();
    }

    @Test
    void page_returnsLoginInfoPage() throws Exception {
        when(loginInfoPageQryExe.execute(any())).thenReturn(PageResult.of(1, List.of(sampleLoginInfo()), 1));

        mockMvc.perform(get(LoginInfoController.BASE_PATH).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.content[0].username").value("admin"));
    }

    @Test
    void getById_returnsLoginInfo() throws Exception {
        when(findLoginInfoByIdQryExe.execute(any())).thenReturn(Optional.of(sampleLoginInfo()));

        mockMvc.perform(get(LoginInfoController.BASE_PATH + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));
    }

    @Test
    void delete_returnsOk() throws Exception {
        mockMvc.perform(delete(LoginInfoController.BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [1, 2]
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void clear_returnsOk() throws Exception {
        mockMvc.perform(post(LoginInfoController.BASE_PATH + "/clear")).andExpect(status().isOk());
    }

    private static LoginInfo sampleLoginInfo() {
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setId(1L);
        loginInfo.setTenantId("000000");
        loginInfo.setUsername("admin");
        loginInfo.setClientId("artemis-admin");
        loginInfo.setDeviceType("PC");
        loginInfo.setIpaddr("127.0.0.1");
        loginInfo.setLoginLocation("未知");
        loginInfo.setBrowser("Chrome");
        loginInfo.setOs("Windows");
        loginInfo.setStatus("SUCCESS");
        loginInfo.setMsg("登录成功");
        loginInfo.setLoginTime(LocalDateTime.now());
        return loginInfo;
    }
}
