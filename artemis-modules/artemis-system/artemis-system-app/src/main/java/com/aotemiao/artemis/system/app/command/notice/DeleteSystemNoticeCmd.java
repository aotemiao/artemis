package com.aotemiao.artemis.system.app.command.notice;

import java.io.Serializable;

/** 删除系统通知公告命令。 */
public record DeleteSystemNoticeCmd(Long id) implements Serializable {}
