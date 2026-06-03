package com.aotemiao.artemis.system.client.dto.client;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/** 客户端授权校验请求。 */
public record ValidateClientRequest(
        @NotBlank(message = "clientId must not be blank") String clientId,
        @NotBlank(message = "grantType must not be blank") String grantType)
        implements Serializable {}
