package com.aotemiao.artemis.system.app.command.auth;

/** 校验用户凭证命令（供内部 auth 调用接口使用）。 */
public record ValidateCredentialsCmd(
        String tenantId, String clientId, String grantType, String username, String password) {

    public ValidateCredentialsCmd(String username, String password) {
        this("000000", "artemis-admin", "password", username, password);
    }

    public ValidateCredentialsCmd(String clientId, String grantType, String username, String password) {
        this("000000", clientId, grantType, username, password);
    }
}
