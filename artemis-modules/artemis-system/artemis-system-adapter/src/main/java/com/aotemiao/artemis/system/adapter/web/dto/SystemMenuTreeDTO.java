package com.aotemiao.artemis.system.adapter.web.dto;

import java.io.Serializable;
import java.util.List;

/** 系统菜单树响应 DTO。 */
public record SystemMenuTreeDTO(SystemMenuDTO menu, Boolean checked, List<SystemMenuTreeDTO> children)
        implements Serializable {

    public SystemMenuTreeDTO {
        children = children == null ? List.of() : List.copyOf(children);
    }
}
