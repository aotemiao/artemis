package com.aotemiao.artemis.resource.client.dto.notify;

public record SmsSendRequest(String phone, String content, String provider, String extJson) {}
