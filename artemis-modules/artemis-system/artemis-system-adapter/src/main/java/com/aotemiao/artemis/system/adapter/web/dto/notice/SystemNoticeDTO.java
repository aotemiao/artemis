package com.aotemiao.artemis.system.adapter.web.dto.notice;

import java.io.Serializable;

/** 系统通知公告响应 DTO。 */
public record SystemNoticeDTO(
        Long id, String noticeTitle, String noticeType, String noticeContent, String status, String remarks)
        implements Serializable {}
