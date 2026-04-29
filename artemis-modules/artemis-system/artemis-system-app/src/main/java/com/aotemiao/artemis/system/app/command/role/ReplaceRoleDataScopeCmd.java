package com.aotemiao.artemis.system.app.command.role;

import java.util.List;

public record ReplaceRoleDataScopeCmd(Long roleId, String dataScope, List<Long> departmentIds) {

    public ReplaceRoleDataScopeCmd {
        departmentIds = departmentIds == null ? List.of() : List.copyOf(departmentIds);
    }
}
