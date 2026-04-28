package com.aotemiao.artemis.system.app.query.audit;

import com.aotemiao.artemis.framework.core.domain.PageRequest;

/** 后台操作日志分页查询。 */
public record OperLogPageQry(PageRequest pageRequest) {}
