package com.aotemiao.artemis.system.app.command.config;

import java.io.Serializable;

/** 新增系统参数命令。 */
public record CreateSystemConfigCmd(
        String configName, String configKey, String configValue, Boolean systemBuiltIn, String remarks)
        implements Serializable {}
