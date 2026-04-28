package com.aotemiao.artemis.system.adapter.web.dto;

import java.io.Serializable;

/** 系统客户端响应 DTO。 */
public record SystemClientDTO(
        Long id,
        String clientId,
        String clientKey,
        String clientSecret,
        String grantTypes,
        String deviceType,
        Long activeTimeoutSeconds,
        Long fixedTimeoutSeconds,
        String status,
        String remarks)
        implements Serializable {}
