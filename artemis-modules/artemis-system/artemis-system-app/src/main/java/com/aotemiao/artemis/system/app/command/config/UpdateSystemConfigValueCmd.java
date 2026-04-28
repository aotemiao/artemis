package com.aotemiao.artemis.system.app.command.config;

import java.io.Serializable;

/** 按参数 key 更新参数值命令。 */
public record UpdateSystemConfigValueCmd(String configKey, String configValue) implements Serializable {}
