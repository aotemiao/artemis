package com.aotemiao.artemis.auth.web.dto;

import java.util.List;

/** 登录成功响应，返回 Token 与最小授权快照。 */
public record LoginResponse(String token, Long userId, List<String> roleKeys) {

    public LoginResponse {
        roleKeys = roleKeys == null ? List.of() : List.copyOf(roleKeys);
    }
}
