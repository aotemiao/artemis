package com.aotemiao.artemis.system.adapter.web.dto.role;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/** 批量替换角色菜单请求。 */
public record ReplaceRoleMenusRequest(@NotNull List<@NotNull Long> menuIds) {

    public ReplaceRoleMenusRequest {
        menuIds = menuIds == null ? List.of() : List.copyOf(menuIds);
    }
}
