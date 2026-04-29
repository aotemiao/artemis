package com.aotemiao.artemis.resource.app.command.notify;

public record SendEmailCmd(String to, String subject, String content, String provider, String extJson) {}
