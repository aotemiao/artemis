package com.aotemiao.artemis.system.app.query.notice;

import java.io.Serializable;

/** 按 ID 查询系统通知公告。 */
public record FindSystemNoticeByIdQry(Long id) implements Serializable {}
