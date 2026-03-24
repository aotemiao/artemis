package com.aotemiao.artemis.system.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 更新系统用户请求。 */
public record UpdateSystemUserRequest(
        @NotBlank String displayName,
        String password,
        @NotNull Boolean enabled) {}
