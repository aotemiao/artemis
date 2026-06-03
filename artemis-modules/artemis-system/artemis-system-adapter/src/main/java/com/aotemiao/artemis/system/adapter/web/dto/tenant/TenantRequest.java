package com.aotemiao.artemis.system.adapter.web.dto.tenant;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/** 租户请求。 */
public record TenantRequest(
        @NotBlank String companyName,
        String contactName,
        String contactPhone,
        String socialCreditCode,
        String address,
        String domain,
        String intro,
        Long packageId,
        LocalDateTime expireTime,
        Integer userLimit,
        String remarks) {}
