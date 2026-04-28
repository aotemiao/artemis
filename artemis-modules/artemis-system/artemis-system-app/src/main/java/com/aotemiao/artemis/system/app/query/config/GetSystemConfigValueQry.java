package com.aotemiao.artemis.system.app.query.config;

import java.io.Serializable;

/** 按 key 查询系统参数值。 */
public record GetSystemConfigValueQry(String configKey) implements Serializable {}
