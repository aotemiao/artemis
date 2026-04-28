package com.aotemiao.artemis.system.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;

/** 系统通知公告创建或更新请求。 */
public record SystemNoticeRequest(
        @NotBlank String noticeTitle,
        @NotBlank String noticeType,
        @NotBlank String noticeContent,
        @NotBlank String status,
        String remarks) {}
