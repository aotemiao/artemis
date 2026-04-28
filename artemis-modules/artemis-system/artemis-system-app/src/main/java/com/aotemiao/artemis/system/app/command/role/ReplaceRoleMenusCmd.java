package com.aotemiao.artemis.system.app.command.role;

import java.util.List;

public record ReplaceRoleMenusCmd(Long roleId, List<Long> menuIds) {

    public ReplaceRoleMenusCmd {
        menuIds = menuIds == null ? List.of() : List.copyOf(menuIds);
    }
}
