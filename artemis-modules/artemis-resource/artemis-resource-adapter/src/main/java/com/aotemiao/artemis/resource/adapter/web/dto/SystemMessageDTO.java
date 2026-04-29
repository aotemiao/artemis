package com.aotemiao.artemis.resource.adapter.web.dto;

import java.time.LocalDateTime;

public record SystemMessageDTO(
        Long id,
        String title,
        String content,
        String sender,
        Long recipientUserId,
        Integer broadcastFlag,
        Integer readFlag,
        LocalDateTime readTime,
        String extJson) {}
