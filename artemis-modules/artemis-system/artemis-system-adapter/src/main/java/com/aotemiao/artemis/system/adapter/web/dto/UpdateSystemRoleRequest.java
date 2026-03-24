package com.aotemiao.artemis.system.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 更新系统角色请求。 */
public record UpdateSystemRoleRequest(
        @NotBlank String roleKey,
        @NotBlank String roleName,
        @NotNull Boolean enabled) {}
