package com.aotemiao.artemis.system.app.command.menu;

public record UpdateSystemMenuCmd(
        Long id,
        Long parentId,
        String menuType,
        String menuName,
        Integer sortOrder,
        String path,
        String component,
        String permissionCode,
        Boolean visible,
        Boolean enabled) {}
