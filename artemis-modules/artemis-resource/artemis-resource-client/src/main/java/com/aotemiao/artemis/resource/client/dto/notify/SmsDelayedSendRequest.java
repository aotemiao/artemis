package com.aotemiao.artemis.resource.client.dto.notify;

import java.time.LocalDateTime;

public record SmsDelayedSendRequest(
        String phone, String content, LocalDateTime delayedAt, String provider, String extJson) {}
