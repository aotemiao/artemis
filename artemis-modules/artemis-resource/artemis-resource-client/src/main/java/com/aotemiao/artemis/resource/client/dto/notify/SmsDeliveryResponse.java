package com.aotemiao.artemis.resource.client.dto.notify;

public record SmsDeliveryResponse(String messageId, String phone, String provider, String status) {}
