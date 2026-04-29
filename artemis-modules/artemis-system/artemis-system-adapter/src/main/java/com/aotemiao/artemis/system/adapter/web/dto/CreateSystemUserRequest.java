package com.aotemiao.artemis.system.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;

/** 新增系统用户请求。 */
public record CreateSystemUserRequest(
        String tenantNo,
        @NotBlank String username,
        @NotBlank String displayName,
        String password) {}
