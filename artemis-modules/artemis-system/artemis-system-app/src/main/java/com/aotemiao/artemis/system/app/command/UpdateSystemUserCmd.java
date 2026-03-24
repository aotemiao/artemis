package com.aotemiao.artemis.system.app.command;

/** 更新系统用户命令。 */
public record UpdateSystemUserCmd(Long id, String displayName, String password, Boolean enabled) {}
