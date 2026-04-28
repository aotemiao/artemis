package com.aotemiao.artemis.system.app.query.notice;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import java.io.Serializable;

/** 分页查询系统通知公告。 */
public record SystemNoticePageQry(PageRequest pageRequest) implements Serializable {}
