package com.aotemiao.artemis.system.app.command.notice;

import java.io.Serializable;

/** 新增系统通知公告命令。 */
public record CreateSystemNoticeCmd(
        String noticeTitle, String noticeType, String noticeContent, String status, String remarks)
        implements Serializable {}
