package com.aotemiao.artemis.system.client.dto;

import java.io.Serializable;

/**
 * 用户凭证校验请求（与 REST 契约及 ValidateCredentialsCmd 对齐）。
 */
public record ValidateCredentialsRequest(String username, String password) implements Serializable {
}
