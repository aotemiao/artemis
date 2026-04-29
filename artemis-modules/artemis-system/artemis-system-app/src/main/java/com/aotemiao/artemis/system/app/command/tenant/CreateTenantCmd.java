package com.aotemiao.artemis.system.app.command.tenant;

public record CreateTenantCmd(
        String companyName,
        String contactName,
        String contactPhone,
        String socialCreditCode,
        String address,
        String domain,
        String intro,
        Long packageId,
        java.time.LocalDateTime expireTime,
        Integer userLimit,
        String remarks) {}
