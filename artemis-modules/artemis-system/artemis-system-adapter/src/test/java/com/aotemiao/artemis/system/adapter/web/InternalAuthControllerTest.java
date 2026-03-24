package com.aotemiao.artemis.system.adapter.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.app.command.ValidateCredentialsCmdExe;
import jakarta.servlet.ServletException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class InternalAuthControllerTest {

    private MockMvc mockMvc;

    private ValidateCredentialsCmdExe validateCredentialsCmdExe;

    @BeforeEach
    void setUp() {
        validateCredentialsCmdExe = mock(ValidateCredentialsCmdExe.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new InternalAuthController(validateCredentialsCmdExe))
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
}
