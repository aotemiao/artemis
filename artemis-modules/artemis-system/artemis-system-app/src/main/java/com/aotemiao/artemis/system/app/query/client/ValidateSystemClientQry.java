package com.aotemiao.artemis.system.app.query.client;

/** 校验系统客户端授权请求。 */
public record ValidateSystemClientQry(String clientId, String grantType) {}
