package com.aotemiao.artemis.resource.client.dto;

public record PublishedMessageResponse(Long id, Long recipientUserId, String title, Integer readFlag) {}
