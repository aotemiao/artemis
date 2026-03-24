package com.aotemiao.artemis.auth.web;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.auth.client.SystemUserValidateClient;
import com.aotemiao.artemis.system.client.dto.ValidateCredentialsRequest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private SystemUserValidateClient systemUserValidateClient;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_whenCredentialsInvalid_throwsInvalidCredentialsException() {
        when(systemUserValidateClient.validate("admin", "bad-password")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authController.login(new ValidateCredentialsRequest("admin", "bad-password")))
                .isInstanceOf(AuthController.InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");
    }
}
