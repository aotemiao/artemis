package com.aotemiao.artemis.system.client.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

public record RegisterUserRequest(
        String tenantId,
        @NotBlank(message = "clientId must not be blank") String clientId,
        @NotBlank(message = "grantType must not be blank") String grantType,
        @NotBlank(message = "username must not be blank") String username,
        @NotBlank(message = "password must not be blank") String password,
        @NotBlank(message = "userType must not be blank") String userType)
        implements Serializable {}
