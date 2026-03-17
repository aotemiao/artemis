package com.aotemiao.artemis.auth.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求。
 */
public record LoginRequest(
        @NotBlank(message = "username must not be blank") String username,
        @NotBlank(message = "password must not be blank") String password
) {
}
