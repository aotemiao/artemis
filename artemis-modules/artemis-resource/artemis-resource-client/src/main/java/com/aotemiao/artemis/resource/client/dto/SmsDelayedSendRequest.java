package com.aotemiao.artemis.resource.client.dto;

import java.time.LocalDateTime;

public record SmsDelayedSendRequest(
        String phone, String content, LocalDateTime delayedAt, String provider, String extJson) {}
