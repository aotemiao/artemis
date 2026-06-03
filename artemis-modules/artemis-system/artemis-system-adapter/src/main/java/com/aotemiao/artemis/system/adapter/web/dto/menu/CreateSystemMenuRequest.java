package com.aotemiao.artemis.system.adapter.web.dto.menu;

import jakarta.validation.constraints.NotBlank;

/** 新增系统菜单请求。 */
public record CreateSystemMenuRequest(
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
        Boolean visible,
        String remarks) {}
