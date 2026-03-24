package com.aotemiao.artemis.system.adapter.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/** 批量替换用户角色请求。 */
public record ReplaceUserRolesRequest(@NotNull List<@NotNull Long> roleIds) {

    public ReplaceUserRolesRequest {
        roleIds = roleIds == null ? List.of() : List.copyOf(roleIds);
    }
}
