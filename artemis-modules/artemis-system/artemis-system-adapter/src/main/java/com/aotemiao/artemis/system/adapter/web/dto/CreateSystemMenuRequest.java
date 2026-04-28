package com.aotemiao.artemis.system.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;

/** 新增系统菜单请求。 */
public record CreateSystemMenuRequest(
        Long parentId,
        @NotBlank String menuType,
        @NotBlank String menuName,
        Integer sortOrder,
        String path,
        String component,
        String permissionCode,
        Boolean visible) {}
