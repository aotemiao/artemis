package com.aotemiao.artemis.resource.domain.gateway.notify;

import com.aotemiao.artemis.resource.domain.model.notify.DeliveryResult;
import java.time.LocalDateTime;

public interface SmsProviderGateway {

    DeliveryResult sendText(String phone, String content, String provider, String extJson);

    DeliveryResult sendTemplate(
            String phone, String templateCode, String templateParams, String provider, String extJson);

    DeliveryResult sendAsync(String phone, String content, String provider, String extJson);

    DeliveryResult sendDelayed(String phone, String content, LocalDateTime delayedAt, String provider, String extJson);
}
