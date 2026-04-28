package com.aotemiao.artemis.system.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 系统客户端请求。 */
public record SystemClientRequest(
        @NotBlank String clientId,
        @NotBlank String clientKey,
        @NotBlank String clientSecret,
        @NotBlank String grantTypes,
        @NotBlank String deviceType,
        @NotNull Long activeTimeoutSeconds,
        @NotNull Long fixedTimeoutSeconds,
        @NotBlank String status,
        String remarks) {}
