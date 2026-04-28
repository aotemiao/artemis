package com.aotemiao.artemis.system.app.command.client;

/** 创建系统客户端命令。 */
public record CreateSystemClientCmd(
        String clientId,
        String clientKey,
        String clientSecret,
        String grantTypes,
        String deviceType,
        Long activeTimeoutSeconds,
        Long fixedTimeoutSeconds,
        String status,
        String remarks) {}
