package com.aotemiao.artemis.system.app.command.config;

import java.io.Serializable;

/** 更新系统参数命令。 */
public record UpdateSystemConfigCmd(
        Long id, String configName, String configKey, String configValue, Boolean systemBuiltIn, String remarks)
        implements Serializable {}
