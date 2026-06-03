package com.aotemiao.artemis.resource.client.dto.notify;

public record EmailSendRequest(String to, String subject, String content, String provider, String extJson) {}
