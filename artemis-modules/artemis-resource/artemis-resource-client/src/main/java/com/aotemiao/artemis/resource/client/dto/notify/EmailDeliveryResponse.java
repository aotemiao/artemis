package com.aotemiao.artemis.resource.client.dto.notify;

public record EmailDeliveryResponse(String messageId, String to, String provider, String status) {}
