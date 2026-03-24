package com.aotemiao.artemis.auth.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AuthExceptionHandlerTest {

    private final AuthExceptionHandler authExceptionHandler = new AuthExceptionHandler();

    @Test
    void handleInvalidCredentials_returnsUnauthorizedBody() {
        ResponseEntity<Map<String, Object>> response =
                authExceptionHandler.handleInvalidCredentials(new AuthController.InvalidCredentialsException("bad"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("code", "UNAUTHORIZED");
        assertThat(response.getBody()).containsEntry("message", "bad");
    }
}
