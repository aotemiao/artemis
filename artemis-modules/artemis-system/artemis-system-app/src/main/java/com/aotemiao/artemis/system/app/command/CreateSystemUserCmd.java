package com.aotemiao.artemis.system.app.command;

/** 新增系统用户命令。 */
public record CreateSystemUserCmd(String username, String displayName, String password) {}
