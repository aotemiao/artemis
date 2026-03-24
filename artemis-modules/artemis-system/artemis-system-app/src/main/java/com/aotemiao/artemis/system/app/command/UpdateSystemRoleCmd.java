package com.aotemiao.artemis.system.app.command;

public record UpdateSystemRoleCmd(Long id, String roleKey, String roleName, Boolean enabled) {}
