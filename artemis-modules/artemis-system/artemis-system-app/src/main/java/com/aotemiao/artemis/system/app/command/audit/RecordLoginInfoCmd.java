package com.aotemiao.artemis.system.app.command.audit;

/** 记录登录访问日志命令。 */
public record RecordLoginInfoCmd(
        String tenantId,
        String username,
        String clientId,
        String deviceType,
        String ipaddr,
        String loginLocation,
        String browser,
        String os,
        String status,
        String msg) {}
