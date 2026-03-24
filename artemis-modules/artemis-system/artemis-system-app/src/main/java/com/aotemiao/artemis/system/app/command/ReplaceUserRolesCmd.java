package com.aotemiao.artemis.system.app.command;

import java.util.List;

public record ReplaceUserRolesCmd(Long userId, List<Long> roleIds) {

    public ReplaceUserRolesCmd {
        roleIds = roleIds == null ? List.of() : List.copyOf(roleIds);
    }
}
