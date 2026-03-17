package com.aotemiao.artemis.system.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 内部认证校验请求（供 artemis-auth 调用）。
 */
public record ValidateCredentialsRequest(
        @NotBlank(message = "username must not be blank") String username,
        @NotBlank(message = "password must not be blank") String password
) {
}
