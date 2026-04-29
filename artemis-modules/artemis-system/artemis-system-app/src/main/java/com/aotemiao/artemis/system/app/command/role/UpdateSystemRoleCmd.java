package com.aotemiao.artemis.system.app.command.role;

public record UpdateSystemRoleCmd(Long id, String roleKey, String roleName, String dataScope, Boolean enabled) {

    public UpdateSystemRoleCmd(Long id, String roleKey, String roleName, Boolean enabled) {
        this(id, roleKey, roleName, "ALL", enabled);
    }
}
