package com.aotemiao.artemis.system.app.command;

/**
 * 校验用户凭证命令（供内部 auth 调用接口使用）。
 */
public record ValidateCredentialsCmd(String username, String password) {
}
