package com.aotemiao.artemis.system.app.command.client;

/** 更新系统客户端命令。 */
public record UpdateSystemClientCmd(
        Long id,
        String clientId,
        String clientKey,
        String clientSecret,
        String grantTypes,
        String deviceType,
        Long activeTimeoutSeconds,
        Long fixedTimeoutSeconds,
        String status,
        String remarks) {}
