package com.aotemiao.artemis.system.app.command;

public record CreateSystemMenuCmd(
        Long parentId,
        String menuType,
        String menuName,
        Integer sortOrder,
        String path,
        String component,
        String permissionCode,
        Boolean visible) {}
