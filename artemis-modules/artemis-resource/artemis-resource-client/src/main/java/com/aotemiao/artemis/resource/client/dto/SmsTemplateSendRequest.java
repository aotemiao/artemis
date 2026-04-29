package com.aotemiao.artemis.resource.client.dto;

public record SmsTemplateSendRequest(
        String phone, String templateCode, String templateParams, String provider, String extJson) {}
