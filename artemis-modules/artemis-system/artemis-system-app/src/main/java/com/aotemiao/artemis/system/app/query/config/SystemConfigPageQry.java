package com.aotemiao.artemis.system.app.query.config;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import java.io.Serializable;

/** 分页查询系统参数。 */
public record SystemConfigPageQry(PageRequest pageRequest) implements Serializable {}
