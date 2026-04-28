package com.aotemiao.artemis.system.app.command.role;

public record UpdateSystemRoleCmd(Long id, String roleKey, String roleName, Boolean enabled) {}
