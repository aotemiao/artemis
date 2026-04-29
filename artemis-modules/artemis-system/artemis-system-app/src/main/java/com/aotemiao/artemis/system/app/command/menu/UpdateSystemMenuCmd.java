package com.aotemiao.artemis.system.app.command.menu;

public record UpdateSystemMenuCmd(
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
        String remarks) {}
