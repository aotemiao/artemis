package com.aotemiao.artemis.system.app.command.tenant;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDateTime;

/** 租户写入命令。 */
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        justification = "Command records are request boundary objects and copied into domain entities.")
public record TenantCmd(
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
        String remarks) {}
