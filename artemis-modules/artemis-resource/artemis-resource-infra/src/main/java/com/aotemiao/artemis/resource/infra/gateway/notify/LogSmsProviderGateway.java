package com.aotemiao.artemis.resource.infra.gateway.notify;

import com.aotemiao.artemis.resource.domain.gateway.notify.SmsProviderGateway;
import com.aotemiao.artemis.resource.domain.model.notify.DeliveryResult;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LogSmsProviderGateway implements SmsProviderGateway {

    @Override
    public DeliveryResult sendText(String phone, String content, String provider, String extJson) {
        return accepted(phone, provider);
    }

    @Override
    public DeliveryResult sendTemplate(
            String phone, String templateCode, String templateParams, String provider, String extJson) {
        return accepted(phone, provider);
    }

    @Override
    public DeliveryResult sendAsync(String phone, String content, String provider, String extJson) {
        return accepted(phone, provider);
    }

    @Override
    public DeliveryResult sendDelayed(
            String phone, String content, LocalDateTime delayedAt, String provider, String extJson) {
        return accepted(phone, provider);
    }

    private DeliveryResult accepted(String phone, String provider) {
        return new DeliveryResult(UUID.randomUUID().toString(), phone, provider, "ACCEPTED");
    }
}
