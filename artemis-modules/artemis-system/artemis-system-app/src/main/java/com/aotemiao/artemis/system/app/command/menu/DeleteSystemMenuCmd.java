package com.aotemiao.artemis.system.app.command.menu;

import java.io.Serializable;

/** 删除系统菜单命令。 */
public record DeleteSystemMenuCmd(Long id) implements Serializable {}
