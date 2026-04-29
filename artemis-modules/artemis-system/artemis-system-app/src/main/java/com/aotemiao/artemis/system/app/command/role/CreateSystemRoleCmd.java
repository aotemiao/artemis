package com.aotemiao.artemis.system.app.command.role;

public record CreateSystemRoleCmd(String roleKey, String roleName, String dataScope) {

    public CreateSystemRoleCmd(String roleKey, String roleName) {
        this(roleKey, roleName, "ALL");
    }
}
