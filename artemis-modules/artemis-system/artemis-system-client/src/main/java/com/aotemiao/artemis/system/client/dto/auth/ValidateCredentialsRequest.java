package com.aotemiao.artemis.system.client.dto.auth;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/** 用户凭证校验请求（与 REST 契约及 ValidateCredentialsCmd 对齐）。 */
public record ValidateCredentialsRequest(
        String tenantId,
        @NotBlank(message = "clientId must not be blank") String clientId,
        @NotBlank(message = "grantType must not be blank") String grantType,
        @NotBlank(message = "username must not be blank") String username,
        @NotBlank(message = "password must not be blank") String password)
        implements Serializable {

    public ValidateCredentialsRequest(String clientId, String grantType, String username, String password) {
        this(null, clientId, grantType, username, password);
    }

    public ValidateCredentialsRequest(String username, String password) {
        this(null, "artemis-admin", "password", username, password);
    }
}
