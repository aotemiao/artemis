package com.aotemiao.artemis.system.adapter.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.app.command.ValidateCredentialsCmdExe;
import com.aotemiao.artemis.system.app.query.GetUserAuthorizationQryExe;
import com.aotemiao.artemis.system.domain.model.UserAuthorizationSnapshot;
import jakarta.servlet.ServletException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class InternalAuthControllerTest {

    private MockMvc mockMvc;

    private ValidateCredentialsCmdExe validateCredentialsCmdExe;
    private GetUserAuthorizationQryExe getUserAuthorizationQryExe;

    @BeforeEach
    void setUp() {
        validateCredentialsCmdExe = mock(ValidateCredentialsCmdExe.class);
        getUserAuthorizationQryExe = mock(GetUserAuthorizationQryExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new InternalAuthController(validateCredentialsCmdExe, getUserAuthorizationQryExe))
                .build();
    }

    @Test
    void validate_whenCredentialsValid_returnsUserId() throws Exception {
        when(validateCredentialsCmdExe.execute(any())).thenReturn(Optional.of(7L));

        mockMvc.perform(post(InternalAuthController.BASE_PATH + "/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data").value(7));
    }

    @Test
    void validate_whenCredentialsInvalid_throwsBizException() {
        when(validateCredentialsCmdExe.execute(any())).thenReturn(Optional.empty());

        ServletException exception = assertThrows(
                ServletException.class,
                () -> mockMvc.perform(post(InternalAuthController.BASE_PATH + "/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "bad"
                                }
                                """)));

        assertThat(exception.getCause()).isInstanceOf(BizException.class);
    }

    @Test
    void getAuthorization_whenUserExists_returnsSnapshot() throws Exception {
        when(getUserAuthorizationQryExe.execute(any()))
                .thenReturn(Optional.of(new UserAuthorizationSnapshot(7L, "admin", "管理员", List.of("super-admin"))));

        mockMvc.perform(get(InternalAuthController.BASE_PATH + "/users/7/authorization"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.userId").value(7))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.displayName").value("管理员"))
                .andExpect(jsonPath("$.data.roleKeys[0]").value("super-admin"));
    }
}
