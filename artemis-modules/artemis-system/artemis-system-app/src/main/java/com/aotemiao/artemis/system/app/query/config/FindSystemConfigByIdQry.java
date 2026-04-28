package com.aotemiao.artemis.system.app.query.config;

import java.io.Serializable;

/** 按 ID 查询系统参数。 */
public record FindSystemConfigByIdQry(Long id) implements Serializable {}
