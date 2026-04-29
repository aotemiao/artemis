package com.aotemiao.artemis.system.adapter.web.dto;

import java.util.List;

/** 替换角色数据权限请求。 */
public record ReplaceRoleDataScopeRequest(String dataScope, List<Long> departmentIds) {

    public ReplaceRoleDataScopeRequest {
        departmentIds = departmentIds == null ? List.of() : List.copyOf(departmentIds);
    }
}
