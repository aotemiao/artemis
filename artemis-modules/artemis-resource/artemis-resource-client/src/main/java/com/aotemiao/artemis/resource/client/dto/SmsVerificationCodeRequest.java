package com.aotemiao.artemis.resource.client.dto;

public record SmsVerificationCodeRequest(String phone, String scene, String provider, String extJson) {}
