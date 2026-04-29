package com.aotemiao.artemis.resource.domain.gateway.notify;

import com.aotemiao.artemis.resource.domain.model.notify.DeliveryResult;

public interface EmailProviderGateway {

    DeliveryResult sendEmail(String to, String subject, String content, String provider, String extJson);
}
