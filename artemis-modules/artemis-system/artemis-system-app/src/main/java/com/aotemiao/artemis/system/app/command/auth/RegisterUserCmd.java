package com.aotemiao.artemis.system.app.command.auth;

/** 用户自助注册命令。 */
public record RegisterUserCmd(
        String tenantId, String clientId, String grantType, String username, String password, String userType) {}
