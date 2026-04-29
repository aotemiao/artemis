package com.aotemiao.artemis.system.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 更新系统菜单请求。 */
public record UpdateSystemMenuRequest(
        Long parentId,
        @NotBlank String menuType,
        @NotBlank String menuName,
        Integer sortOrder,
        String path,
        String component,
        String queryParam,
        Boolean externalLink,
        Boolean cacheable,
        String permissionCode,
        String icon,
        @NotNull Boolean visible,
        @NotNull Boolean enabled,
        String remarks) {}
