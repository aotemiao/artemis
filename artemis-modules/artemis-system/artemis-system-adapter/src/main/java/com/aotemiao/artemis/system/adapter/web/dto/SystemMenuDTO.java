package com.aotemiao.artemis.system.adapter.web.dto;

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
        String permissionCode,
        Boolean visible,
        Boolean enabled)
        implements Serializable {}
