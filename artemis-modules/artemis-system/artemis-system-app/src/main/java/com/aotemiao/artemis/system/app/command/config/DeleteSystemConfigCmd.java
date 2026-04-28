package com.aotemiao.artemis.system.app.command.config;

import java.io.Serializable;

/** 删除系统参数命令。 */
public record DeleteSystemConfigCmd(Long id) implements Serializable {}
