package com.aotemiao.artemis.resource.client.dto;

public record EmailDeliveryResponse(String messageId, String to, String provider, String status) {}
