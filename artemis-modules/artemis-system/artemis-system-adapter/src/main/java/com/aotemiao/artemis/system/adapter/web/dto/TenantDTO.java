package com.aotemiao.artemis.system.adapter.web.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record TenantDTO(
        Long id,
        String tenantNo,
        String companyName,
        String contactName,
        String contactPhone,
        String socialCreditCode,
        String address,
        String domain,
        String intro,
        Long packageId,
        LocalDateTime expireTime,
        Integer userLimit,
        String status,
        String remarks)
        implements Serializable {}
