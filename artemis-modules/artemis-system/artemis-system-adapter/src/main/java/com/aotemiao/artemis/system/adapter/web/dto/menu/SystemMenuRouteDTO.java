package com.aotemiao.artemis.system.adapter.web.dto.menu;

import java.io.Serializable;
import java.util.List;

/** 前端路由菜单响应 DTO。 */
public record SystemMenuRouteDTO(
        Long id,
        Long parentId,
        String name,
        String path,
        String component,
        String queryParam,
        Boolean externalLink,
        Boolean cacheable,
        Boolean visible,
        String permissionCode,
        String icon,
        List<SystemMenuRouteDTO> children)
        implements Serializable {

    public SystemMenuRouteDTO {
        children = children == null ? List.of() : List.copyOf(children);
    }
}
