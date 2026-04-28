package com.aotemiao.artemis.system.app.command.notice;

import java.io.Serializable;

/** 更新系统通知公告命令。 */
public record UpdateSystemNoticeCmd(
        Long id, String noticeTitle, String noticeType, String noticeContent, String status, String remarks)
        implements Serializable {}
