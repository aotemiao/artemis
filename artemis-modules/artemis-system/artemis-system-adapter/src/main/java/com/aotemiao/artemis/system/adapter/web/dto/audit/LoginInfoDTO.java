package com.aotemiao.artemis.system.adapter.web.dto.audit;

import java.time.LocalDateTime;

public record LoginInfoDTO(
        Long id,
        String tenantId,
        String username,
        String clientId,
        String deviceType,
        String ipaddr,
        String loginLocation,
        String browser,
        String os,
        String status,
        String msg,
        LocalDateTime loginTime) {}
