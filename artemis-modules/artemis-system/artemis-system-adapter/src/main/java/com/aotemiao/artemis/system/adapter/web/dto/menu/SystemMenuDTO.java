package com.aotemiao.artemis.system.adapter.web.dto.menu;

import java.io.Serializable;

/** 系统菜单响应 DTO。 */
public record SystemMenuDTO(
        Long id,
        Long parentId,
        String menuType,
        String menuName,
        Integer sortOrder,
        String path,
        String component,
        String queryParam,
        Boolean externalLink,
        Boolean cacheable,
        String permissionCode,
        String icon,
        Boolean visible,
        Boolean enabled,
        String remarks)
        implements Serializable {}
