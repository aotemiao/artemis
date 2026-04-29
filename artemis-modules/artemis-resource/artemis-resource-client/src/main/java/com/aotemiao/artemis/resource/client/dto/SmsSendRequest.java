package com.aotemiao.artemis.resource.client.dto;

public record SmsSendRequest(String phone, String content, String provider, String extJson) {}
