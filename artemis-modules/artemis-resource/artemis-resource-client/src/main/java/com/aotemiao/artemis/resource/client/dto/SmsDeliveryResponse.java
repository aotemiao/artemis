package com.aotemiao.artemis.resource.client.dto;

public record SmsDeliveryResponse(String messageId, String phone, String provider, String status) {}
