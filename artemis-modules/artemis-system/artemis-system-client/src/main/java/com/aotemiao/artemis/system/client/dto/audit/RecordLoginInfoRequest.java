package com.aotemiao.artemis.system.client.dto.audit;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

public record RecordLoginInfoRequest(
        String tenantId,
        @NotBlank(message = "username must not be blank") String username,
        String clientId,
        String deviceType,
        String ipaddr,
        String loginLocation,
        String browser,
        String os,
        @NotBlank(message = "status must not be blank") String status,
        String msg)
        implements Serializable {}
