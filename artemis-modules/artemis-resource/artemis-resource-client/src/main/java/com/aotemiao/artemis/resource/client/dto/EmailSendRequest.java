package com.aotemiao.artemis.resource.client.dto;

public record EmailSendRequest(String to, String subject, String content, String provider, String extJson) {}
