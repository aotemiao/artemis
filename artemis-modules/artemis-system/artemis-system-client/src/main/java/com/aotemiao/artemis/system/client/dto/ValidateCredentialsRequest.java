package com.aotemiao.artemis.system.client.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/** 用户凭证校验请求（与 REST 契约及 ValidateCredentialsCmd 对齐）。 */
public record ValidateCredentialsRequest(
        @NotBlank(message = "username must not be blank") String username,
        @NotBlank(message = "password must not be blank") String password)
        implements Serializable {}
