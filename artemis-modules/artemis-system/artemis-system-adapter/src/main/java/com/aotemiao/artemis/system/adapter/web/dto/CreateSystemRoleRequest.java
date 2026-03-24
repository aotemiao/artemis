package com.aotemiao.artemis.system.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;

/** 新增系统角色请求。 */
public record CreateSystemRoleRequest(
        @NotBlank String roleKey, @NotBlank String roleName) {}
