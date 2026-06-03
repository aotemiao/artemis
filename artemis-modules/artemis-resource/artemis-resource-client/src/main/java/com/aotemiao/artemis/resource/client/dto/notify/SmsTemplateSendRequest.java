package com.aotemiao.artemis.resource.client.dto.notify;

public record SmsTemplateSendRequest(
        String phone, String templateCode, String templateParams, String provider, String extJson) {}
