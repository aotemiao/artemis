package com.aotemiao.artemis.resource.domain.model.notify;

import java.io.Serializable;

public record DeliveryResult(String messageId, String target, String provider, String status) implements Serializable {}
