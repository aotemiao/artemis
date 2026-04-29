package com.aotemiao.artemis.system.app.command.tenant;

import java.time.LocalDateTime;

public record UpdateTenantCmd(
        Long id,
        String companyName,
        String contactName,
        String contactPhone,
        String socialCreditCode,
        String address,
        String domain,
        String intro,
        LocalDateTime expireTime,
        Integer userLimit,
        String remarks) {}
